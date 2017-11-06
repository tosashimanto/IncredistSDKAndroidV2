package jp.co.flight.incredist.android.internal.controller.result;

/**
 * Incredist の処理結果の基底クラス.
 */
@SuppressWarnings({ "WeakerAccess", "unused" }) // for public API.
public class IncredistResult {
    public final static int STATUS_SUCCESS = 1;
    public final static int STATUS_FAILED_EXECUTION = 900;

    public final int status;

    public IncredistResult(int status) {
        this.status = status;
    }

}
