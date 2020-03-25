package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;

import jp.co.flight.incredist.android.internal.controller.result.EmvResult;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.controller.result.MagCardResult;
import jp.co.flight.incredist.android.internal.util.BytesUtils;
import jp.co.flight.incredist.android.internal.util.FLog;
import jp.co.flight.incredist.android.model.CreditCardType;
import jp.co.flight.incredist.android.model.EmvTagType;
import jp.co.flight.incredist.android.model.EmvTransactionType;
import jp.co.flight.incredist.android.model.MagCard;

/**
 * クレジットカード決済コマンド(pt)
 */
public class MFiScanCreditCardCommand extends MFiCommand {
    private static final String TAG = "MFiScanCreditCardCommand";

    private static final byte[] PT_HEADER = new byte[]{'p', 't'};

    private static final byte RESPONSE_SUCCESS = (byte) 0x00;
    private static final byte RESPONSE_MAG = (byte) 0x02;
    private static final byte RESPONSE_DECLINE = (byte) 0x07;
    private static final byte RESPONSE_CANCEL_BUTTON_PUSHED = (byte) 0x10;
    private static final byte RESPONSE_EMV_FALLBACK = (byte) 0x20;
    private static final byte RESPONSE_ERROR = (byte) 0xff;
    private static final byte RESPONSE_INPUT_TIMEOUT = (byte) 0xf0;
    private static final byte RESPONSE_CARD_BLOCK = (byte) 0xf1;

    private EmvResult mEmvResult = null;
    private final long mTimeout;

    private static byte[] createPayload(EnumSet<CreditCardType> cardTypeSet, long amount, EmvTagType tagType, int aidSetting, EmvTransactionType transactionType, boolean fallback) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] payload = new byte[13];

        System.arraycopy(PT_HEADER, 0, payload, 0, PT_HEADER.length);

        byte cardType = 0;
        for (CreditCardType type : cardTypeSet) {
            cardType |= type.getValue();
        }

        payload[2] = cardType;
        payload[3] = tagType.getValue();
        payload[4] = (byte) (aidSetting & 0xff);
        payload[5] = transactionType.getValue();
        payload[6] = fallback ? (byte) 0x00 : (byte) 0x01;

        long value = amount;
        // BCD 12桁を下から詰める
        for (int i = 5; i >= 0; i--) {
            byte b = (byte) ((value % 10) & 0x0f);
            value = (value - value % 10) / 10;
            b |= (byte) (((value % 10) & 0x0f) << 4);
            value = (value - value % 10) / 10;

            payload[7 + i] = b;
        }
        // CHECKSTYLE:ON MagicNumber

        return payload;
    }

    public MFiScanCreditCardCommand(EnumSet<CreditCardType> cardTypeSet, long amount, EmvTagType tagType, int aidSetting, EmvTransactionType transactionType, boolean fallback, long timeout) {
        super(createPayload(cardTypeSet, amount, tagType, aidSetting, transactionType, fallback));
        mTimeout = timeout;
    }

    @Override
    public long getResponseTimeout() {
        return mTimeout;
    }

    @Override
    public boolean cancelable() {
        return true;
    }

    @Override
    public void onCancelled(MFiTransport transport) {
        onCommonCancelled(transport);
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        byte[] data = response.getData();
        IncredistResult result;

        if (data != null && data.length >= 1) {
            if (mEmvResult == null) {
                // 第1パケット
                switch (data[0]) {
                    case RESPONSE_SUCCESS:
                        result = parse1stEmvResponse(data);

                        if (result != null) {
                            return result;
                        }
                        break;

                    case RESPONSE_DECLINE:
                        return new IncredistResult(IncredistResult.STATUS_DECLINE);

                    case RESPONSE_CANCEL_BUTTON_PUSHED:
                        return new IncredistResult(IncredistResult.STATUS_INPUT_CANCEL_BUTTON);

                    case RESPONSE_EMV_FALLBACK:
                        return new IncredistResult(IncredistResult.STATUS_EMV_FALLBACK);

                    case RESPONSE_ERROR:
                        return new IncredistResult(IncredistResult.STATUS_ERROR);

                    case RESPONSE_INPUT_TIMEOUT:
                        return new IncredistResult(IncredistResult.STATUS_INPUT_TIMEOUT);

                    case RESPONSE_CARD_BLOCK:
                        return new IncredistResult(IncredistResult.STATUS_CARD_BLOCK);

                    case RESPONSE_MAG:
                        // MJ3 の解析処理
                        result = parseMJ3Response(data);
                        if (result != null) {
                            return result;
                        }
                        break;

                    default:
                        break;
                }
            } else {
                result = parseLastEmvResponse(data);
                if (result != null) {
                    return result;
                }
            }
        }

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }

    /**
     * EMV 第1パケットの解析
     *
     * @param data 受信データ
     * @return 解析結果の IncredistResult オブジェクト
     */
    private IncredistResult parse1stEmvResponse(byte[] data) {
        //ANDROID_GMO-839
        if (data.length < 6) {
            return null;
        }
        // CHECKSTYLE:OFF MagicNumber
        int countPackets = data[1] & 0xff;
        int index = data[2] & 0xff;
        int totalLength = ((data[3] & 0xff) << 8) | (data[4] & 0xff);
        int length = data[5] & 0xff;
        FLog.d(TAG, String.format(Locale.JAPANESE, "parse1stEmvResponse datalen:%d count:%d total:%d length:%d", data.length, countPackets, totalLength, length));

        if (index == 1 && data.length == length + 6) {
            mEmvResult = new EmvResult(countPackets, totalLength);
            if (mEmvResult.appendBytes(index, data, 6, length)) {
                // 1パケットで完結している場合
                if (mEmvResult.isValidLength()) {
                    FLog.d(TAG, "parse EmvResult Success");
                    return mEmvResult;
                } else {
                    FLog.d(TAG, "parse EmvResult Failed");
                    return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
                }
            } else {
                FLog.d(TAG, "parse EmvResult continue multiple");
                return new IncredistResult(IncredistResult.STATUS_CONTINUE_MULTIPLE_RESPONSE);
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return null;
    }

    /**
     * EMV 第2パケット以降の解析
     *
     * @param data 受信データ
     * @return 解析結果の IncredistResult オブジェクト
     */
    private IncredistResult parseLastEmvResponse(byte[] data) {
        // 第2パケット以降
        // CHECKSTYLE:OFF MagicNumber
        if (data.length > 2) {
            int index = data[0] & 0xff;
            int length = data[1] & 0xff;

            FLog.d(TAG, String.format(Locale.JAPANESE, "parseLastEmvResponse datalen:%d count:%d length:%d", data.length, index, length));
            if (data.length == length + 2) {
                if (mEmvResult.appendBytes(index, data, 2, length)) {
                    if (mEmvResult.isValidLength()) {
                        FLog.d(TAG, "parse EmvResult Success");
                        return mEmvResult;
                    } else {
                        FLog.d(TAG, "parse EmvResult Failed");
                        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
                    }
                } else {
                    FLog.d(TAG, "parse EmvResult continue multiple");
                    return new IncredistResult(IncredistResult.STATUS_CONTINUE_MULTIPLE_RESPONSE);
                }
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return null;
    }

    /**
     * 磁気カードパケットの解析
     *
     * @param data 受信データ
     * @return 解析結果の IncredistResult オブジェクト
     */
    private IncredistResult parseMJ3Response(byte[] data) {
        // CHECKSTYLE:OFF MagicNumber
        if (data.length > 3) {
            int totalLength = ((data[2] & 0xff) << 8) + (data[1] & 0xff);
            if (data.length == totalLength && totalLength > 15) {
                int cardType = data[3] & 0xff;
                MagCard.Type magCardType;
                if (cardType == 'j') {
                    magCardType = MagCard.Type.JIS2;
                } else if (cardType == 'c') {
                    magCardType = MagCard.Type.JIS1;
                } else {
                    return null;
                }

                byte[] ksn = Arrays.copyOfRange(data, 4, 14);
                int track1Length = data[14] & 0xff;
                int track2Length = data[15] & 0xff;
                int track3Length = data[16] & 0xff;  // 通常 0のはず

                if (data.length >= 17 + track1Length + track2Length + 18) {
                    int offset = 17;

                    byte[] track1 = Arrays.copyOfRange(data, offset, offset + track1Length);
                    offset += track1Length;

                    byte[] track2 = Arrays.copyOfRange(data, offset, offset + track2Length);
                    offset += track2Length;

                    // 一応 track3Length != 0 の場合に読み飛ばす
                    offset += track3Length;

                    byte[] reducedPanValidity = Arrays.copyOfRange(data, offset, offset + 14);
                    offset += 14;
                    byte[] serviceCode = Arrays.copyOfRange(data, offset, offset + 3);
                    offset += 3;

                    int nameLength = data[offset] & 0xff;
                    offset += 1;

                    if (data.length == offset + nameLength) {
                        byte[] cardHolderName = Arrays.copyOfRange(data, offset, offset + nameLength);

                        MagCardResult result = new MagCardResult();

                        result.cardType = magCardType;
                        result.ksn = ksn;
                        result.track1 = track1;
                        result.track2 = track2;

                        if (magCardType == MagCard.Type.JIS1) {
                            result.maskedCardNo = BytesUtils.getMaskedCardNo(reducedPanValidity, 0, reducedPanValidity.length);
                            result.expirationDate = BytesUtils.getExpirationDate(reducedPanValidity, 0, reducedPanValidity.length);
                            result.serviceCode = BytesUtils.toAsciiString(serviceCode, 0, serviceCode.length);
                            // 銀聯処理の場合JIS1が存在しないためcardHolderNameが0になるため場合分け
                            if(cardHolderName.length > 0) {
                                result.cardHolderName = BytesUtils.toAsciiString(cardHolderName, 1, cardHolderName.length - 2);
                            } else{
                                result.cardHolderName = BytesUtils.toAsciiString(cardHolderName, 0, 0);
                            }
                        }

                        return result;
                    }
                }
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return null;
    }

}
