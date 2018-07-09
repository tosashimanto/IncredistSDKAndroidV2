package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.model.LedColor;

/**
 * MFi 用 FeliCa モード LED 設定コマンド (setc)
 */
public class MFiFelicaLedColorCommand extends MFiCommand {
    public MFiFelicaLedColorCommand(LedColor color) {
        super(new byte[]{'s', 'e', 't', 'c', color.getValue()});
    }

    @Override
    public long getGuardWait() {
        return 0;
    }

    @Override
    public long getResponseTimeout() {
        return 900; // SUPPRESS CHECKSTYLE MagicNumber
    }
    
    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length == 5 && bytes[0] == 's' && bytes[1] == 'e' && bytes[2] == 't' && bytes[3] == 'c') {
            if (bytes[4] == 0x00) {
                return new IncredistResult(IncredistResult.STATUS_SUCCESS);
            } else {
                return new IncredistResult(IncredistResult.STATUS_FAILURE);
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
