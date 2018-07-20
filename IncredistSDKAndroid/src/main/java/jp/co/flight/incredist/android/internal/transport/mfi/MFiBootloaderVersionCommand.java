package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import java.nio.charset.Charset;

import jp.co.flight.incredist.android.internal.controller.result.BootloaderVersionResult;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * MFi版 ブートローダバージョン取得(gbl)コマンド.
 */
public class MFiBootloaderVersionCommand extends MFiCommand {
    /**
     * コンストラクタ.
     */
    public MFiBootloaderVersionCommand() {
        super(new byte[]{'g', 'b', 'l'});
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length > 4 &&
                bytes[0] == 'g' && bytes[1] == 'b' && bytes[2] == 'l') {
            String str = new String(bytes, 3, bytes.length - 3, Charset.forName("UTF-8"));
            String[] values = str.split("\n");

            if (values.length >= 2) {
                return new BootloaderVersionResult(values[0], values[1]);
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
