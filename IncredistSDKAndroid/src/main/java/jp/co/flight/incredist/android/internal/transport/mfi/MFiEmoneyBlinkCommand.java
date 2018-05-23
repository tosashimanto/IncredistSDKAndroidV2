package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import jp.co.flight.incredist.android.internal.controller.result.BlinkResult;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.util.BytesUtils;
import jp.co.flight.incredist.android.model.LedColor;

/**
 * MFi 用 電子マネー向け点滅コマンド (line)
 */
public class MFiEmoneyBlinkCommand extends MFiCommand {
    private static final byte[] LINE_HEADER = new byte[]{'l', 'i', 'n', 'e'};

    private static byte[] createPayload(boolean isBlink, LedColor color, int duration) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] payload = new byte[LINE_HEADER.length + 3];

        System.arraycopy(LINE_HEADER, 0, payload, 0, LINE_HEADER.length);
        payload[4] = isBlink ? (byte) 0x31 : (byte) 0x30;
        payload[5] = color.getValue();
        payload[6] = (byte) duration;
        // CHECKSTYLE:ON MagicNumber

        return payload;
    }

    public MFiEmoneyBlinkCommand(boolean isBlink, LedColor color, int duration) {
        super(createPayload(isBlink, color, duration));
    }

    @Override
    public long getGuardWait() {
        return 0;
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length == 5 && BytesUtils.startsWith(bytes, LINE_HEADER)
                && (bytes[4] == 0x30 || bytes[4] == 0x31)) {
            return new BlinkResult(bytes[4] == 0x31);
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
