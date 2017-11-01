package jp.co.flight.incredist.android.internal.controller.result;

/**
 * Incredist の処理結果の基底クラス.
 */
public class IncredistResult {
    public final static int STATUS_SUCCESS = 1;
    public final static int STATUS_FAILED_EXECUTION = 900;

    private final int mStatus;

    public IncredistResult(int status) {
        mStatus = status;
    }

}
