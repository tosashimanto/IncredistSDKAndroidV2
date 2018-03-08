package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.model.LedColor;

/**
 * MFi 用 LED 設定コマンド (led)
 */
public class MFiSetLedColorCommand extends MFiCommand {
    public MFiSetLedColorCommand(LedColor color, boolean isOn) {
        super(new byte[]{'l', 'e', 'd', color.getValue(), (byte) (isOn ? 0x30 : 0x31)}); // SUPPRESS CHECKSTYLE MagicNumber
    }

    @Override
    public long getResponseTimeout() {
        return 1000;  // SUPPRESS CHECKSTYLE MagicNumber
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length == 4 && bytes[0] == 'l' && bytes[1] == 'e' && bytes[2] == 'd') {
            if (bytes[3] == 0x30) {
                return new IncredistResult(IncredistResult.STATUS_SUCCESS);
            } else {
                return new IncredistResult(IncredistResult.STATUS_FAILURE);
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
