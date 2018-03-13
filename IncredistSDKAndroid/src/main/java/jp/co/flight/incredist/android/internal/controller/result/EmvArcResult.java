package jp.co.flight.incredist.android.internal.controller.result;

import jp.co.flight.incredist.android.model.EmvPacket;

/**
 * EMV kernel へ ARC を送信した際の結果クラス
 */
public class EmvArcResult extends IncredistResult {
    private final byte[] mResult;

    public EmvArcResult(byte[] result) {
        super(IncredistResult.STATUS_SUCCESS);
        mResult = result;
    }

    public EmvPacket toEmvPacket() {
        return new EmvPacket(mResult);
    }
}
