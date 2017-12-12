package jp.co.flight.incredist.android.internal.controller.result;

/**
 * Incredist の処理結果の基底クラス.
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // for public API.
public class IncredistResult {
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED_EXECUTION = 901;
    public static final int STATUS_BUSY = 902;
    public static final int STATUS_INTERRUPTED = 801;
    public static final int STATUS_TIMEOUT = 802;
    public static final int STATUS_INVALID_RESPONSE = 701;
    public static final int STATUS_TOO_LARGE_RESPONSE = 702;
    public static final int STATUS_INVALID_RESPONSE_HEADER = 703;

    public static final int STATUS_PARAMETER_ERROR = 300;
    public static final int STATUS_PIN_TIMEOUT = 600;
    public static final int STATUS_PIN_CANCEL = 602;
    public static final int STATUS_PIN_SKIP = 603;
    public static final int STATUS_CANCELED = 101;
    public static final int STATUS_MAG_TRACK_ERROR = 604;

    public final int status;
    public final String message;

    public IncredistResult(int status) {
        this.status = status;
        this.message = "";
    }

    public IncredistResult(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
