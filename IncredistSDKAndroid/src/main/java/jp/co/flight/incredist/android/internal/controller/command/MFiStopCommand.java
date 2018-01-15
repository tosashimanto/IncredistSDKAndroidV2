package jp.co.flight.incredist.android.internal.controller.command;

import android.support.annotation.NonNull;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiNoResponse;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiResponse;

/**
 * MFi版 停止(s)コマンド.
 */
public class MFiStopCommand extends MFiCommand {
    private static final byte[] S_HEADER = {'s'};

    /**
     * コンストラクタ.
     */
    public MFiStopCommand() {
        super(S_HEADER);
    }

    /**
     * 最大応答待ち時間.
     *
     * @return 応答パケットがないので -1 を返す
     */
    @Override
    public long getResponseTimeout() {
        return -1;
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        if (response instanceof MFiNoResponse) {
            return new IncredistResult(IncredistResult.STATUS_SUCCESS);
        } else {
            return new IncredistResult(response.getErrorCode());
        }
    }
}
