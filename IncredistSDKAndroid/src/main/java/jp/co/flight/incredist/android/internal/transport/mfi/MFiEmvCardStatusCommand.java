package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import jp.co.flight.incredist.android.internal.controller.result.CardStatusResult;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * MFi 用 icカード挿入チェックコマンド(icw)
 */
public class MFiEmvCardStatusCommand extends MFiCommand {
    private static final byte[] ICW_HEADER = new byte[]{'i', 'c', 'w'};

    public MFiEmvCardStatusCommand() {
        super(ICW_HEADER);
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length == 1 && (bytes[0] == 0x00 || bytes[0] == (byte) 0xff)) {
            return new CardStatusResult(bytes[0] == 0x00);
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
