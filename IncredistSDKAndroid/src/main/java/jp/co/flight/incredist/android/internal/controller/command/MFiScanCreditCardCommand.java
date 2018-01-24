package jp.co.flight.incredist.android.internal.controller.command;

import android.support.annotation.NonNull;

import java.util.Locale;

import jp.co.flight.incredist.android.internal.controller.result.EmvResult;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiResponse;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiTransport;
import jp.co.flight.incredist.android.internal.util.FLog;
import jp.co.flight.incredist.android.model.CreditCardType;
import jp.co.flight.incredist.android.model.EmvTagType;

/**
 * クレジットカード決済コマンド(pt)
 */
public class MFiScanCreditCardCommand extends MFiCommand {
    private static final String TAG = "MFiScanCreditCardCommand";

    private static final byte[] PT_HEADER = new byte[] {'p', 't'};

    private static final byte RESPONSE_SUCCESS = (byte) 0x00;
    private static final byte RESPONSE_CANCEL_BUTTON_PUSHED = (byte) 0x10;
    private static final byte RESPONSE_EMV_FALLBACK = (byte) 0x20;
    private static final byte RESPONSE_ERROR = (byte) 0xff;
    private static final byte RESPONSE_INPUT_TIMEOUT = (byte) 0xf0;
    private static final byte RESPONSE_CARD_BLOCK = (byte) 0xf1;

    private EmvResult mEmvResult = null;
    private final long mTimeout;

    private static byte[] createPayload(CreditCardType cardType, long amount, EmvTagType tagType) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] payload = new byte[10];

        System.arraycopy(PT_HEADER, 0, payload, 0, PT_HEADER.length);
        payload[2] = cardType.getValue();
        payload[3] = tagType.getValue();

        long value = amount;
        // BCD 12桁を下から詰める
        for (int i = 5; i >= 0; i--) {
            byte b  = (byte) ((value % 10) & 0x0f);
            value = (value - value % 10) / 10;
            b |= (byte) (((value % 10) & 0x0f) << 4);
            value = (value - value % 10) / 10;

            payload[4 + i] = b;
        }
        // CHECKSTYLE:ON MagicNumber

        return payload;
    }

    public MFiScanCreditCardCommand(CreditCardType cardType, long amount, EmvTagType tagType, long timeout) {
        super(createPayload(cardType, amount, tagType));
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

        // CHECKSTYLE:OFF MagicNumber
        if (data != null && data.length > 1) {
            if (mEmvResult == null) {
                // 第1パケット
                switch (data[0]) {
                    case RESPONSE_SUCCESS:
                        int countPackets = data[1] & 0xff;
                        int index = data[2] & 0xff;
                        int totalLength = ((data[3] & 0xff) << 8) | (data[4] & 0xff);
                        int length = data[5] & 0xff;
                        FLog.d(TAG, String.format(Locale.JAPANESE,"success datalen:%d count:%d total:%d length:%d", data.length, countPackets, totalLength, length));

                        if (index == 1 && data.length == length + 6) {
                            mEmvResult = new EmvResult(countPackets, totalLength);
                            if (mEmvResult.appendBytes(index, data, 6, length)) {
                                // 1パケットで完結している場合
                                if (mEmvResult.isValidLength()) {
                                    return mEmvResult;
                                } else {
                                    return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
                                }
                            } else {
                                return new IncredistResult(IncredistResult.STATUS_CONTINUE_MULTIPLE_RESPONSE);
                            }
                        }
                        break;

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

                    default:
                        //TODO MJ3 の解析処理
                }
            } else {
                // 第2パケット以降
                if (data.length > 2) {
                    int index = data[0];
                    int length = data[1];

                    if (data.length == length + 2) {
                        if (mEmvResult.appendBytes(index, data, 2, length)) {
                            if (mEmvResult.isValidLength()) {
                                return mEmvResult;
                            } else {
                                return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
                            }
                        } else {
                            return new IncredistResult(IncredistResult.STATUS_CONTINUE_MULTIPLE_RESPONSE);
                        }
                    }
                }
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
