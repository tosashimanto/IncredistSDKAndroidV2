package jp.co.flight.incredist.android.internal.controller.command;

import android.support.annotation.NonNull;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiResponse;
import jp.co.flight.incredist.android.model.EncryptionMode;

/**
 * 暗号化モード設定(sdm)コマンド
 */
public class MFiSetEncryptionModeCommand extends MFiCommand {
    private static final byte[] SDM_HEADER = {'s', 'd', 'm'};

    private static byte[] createPayload(EncryptionMode mode) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] payload = new byte[18];

        System.arraycopy(SDM_HEADER, 0, payload, 0, SDM_HEADER.length);
        payload[3] = mode.getKeyNumber();
        payload[4] = mode.getCipherMethod().getValue();
        payload[5] = mode.getBlockCipherMode().getValue();
        payload[6] = mode.getDSConstant().getValue();
        payload[7] = mode.getPaddingMode().getValue();
        payload[8] = mode.getPaddingValue();
        payload[17] = mode.isPin() ? (byte) 0x01 : (byte) 0x00;
        // CHECKSTYLE:ON MagicNumber

        return payload;
    }

    public MFiSetEncryptionModeCommand(EncryptionMode mode) {
        super(createPayload(mode));
    }

    @Override
    public long getResponseTimeout() {
        return 1000;   // SUPPRESS CHECKSTYLE MagicNumber
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] bytes = response.getData();
        if (bytes != null && bytes[0] == 'o' && bytes[1] == 'k') {
            return new IncredistResult(IncredistResult.STATUS_SUCCESS);
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
