package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

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
     * @return 応答パケットがないので 0 を返す
     */
    @Override
    public long getResponseTimeout() {
        return 0;
    }

    @Override
    public long getGuardWait() {
        return 400; // SUPPRESS CHECKSTYLE MagicNumber
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
