package jp.co.flight.incredist.android.internal.controller.command;

import android.support.annotation.NonNull;

import java.nio.charset.Charset;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.controller.result.SerialNumberResult;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiResponse;

/**
 * MFi版 dコマンド.
 */
public class MFiDCommand extends MFiCommand {
    /**
     * コンストラクタ.
     */
    public MFiDCommand() {
        super(new byte[] { 'd', 0x00 });
    }

    /**
     * 最大応答待ち時間
     * @return 1000msec
     */
    @Override
    public long getResponseTimeout() {
        return 1000;
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        byte[] bytes = response.getData();
        if (bytes != null) {
            String str = new String(bytes, Charset.forName("UTF-8"));
            String[] values = str.split("\n");

            if (values.length >= 4) {
                return new SerialNumberResult(values[0], values[1], values[2], values[3]);
            }
        }
        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
