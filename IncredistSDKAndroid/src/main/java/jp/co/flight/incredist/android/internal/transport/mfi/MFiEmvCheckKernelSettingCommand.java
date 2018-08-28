package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.util.LogUtil;
import jp.co.flight.incredist.android.model.EmvSetupDataType;

/**
 * EMV kernel の設定データチェックコマンド
 */
public final class MFiEmvCheckKernelSettingCommand extends MFiCommand {
    private static final byte[] ISHA_HEADER = new byte[]{'i', 's', 'h', 'a'};

    private static byte[] createPayload(EmvSetupDataType type, byte[] hashData) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] payload = new byte[ISHA_HEADER.length + 21];
        System.arraycopy(ISHA_HEADER, 0, payload, 0, ISHA_HEADER.length);
        payload[4] = type.getValue();
        System.arraycopy(hashData, 0, payload, 5, 20);
        // CHECKSTYLE:ON MagicNumber

        return payload;
    }

    /**
     * コンストラクタ
     *
     * @param type     設定種別
     * @param hashData ハッシュ値 データ
     */
    public MFiEmvCheckKernelSettingCommand(EmvSetupDataType type, byte[] hashData) {
        super(createPayload(type, hashData));
    }

    @Override
    public long getResponseTimeout() {
        return 3000; // SUPPRESS CHECKSTYLE MagicNumber
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length == 1) {
            if (bytes[0] == 0) {
                return new IncredistResult(IncredistResult.STATUS_SUCCESS);
            } else {
                return new IncredistResult(IncredistResult.STATUS_FAILURE);
            }
        }

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE, LogUtil.hexString(bytes));
    }
}
