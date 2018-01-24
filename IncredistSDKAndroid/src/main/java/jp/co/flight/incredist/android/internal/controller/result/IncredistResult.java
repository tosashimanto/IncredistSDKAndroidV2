package jp.co.flight.incredist.android.internal.controller.result;

/**
 * Incredist の処理結果の基底クラス.
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // for public API.
public class IncredistResult {
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILURE = 2;
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
    public static final int STATUS_MAG_TRACK_ERROR = 604;

    public static final int STATUS_CANCELED = 101;
    public static final int STATUS_CANCEL_FAILED = 102;
    public static final int STATUS_NOT_CANCELLABLE = 103;

    public static final int STATUS_INPUT_CANCEL_BUTTON = 202;
    public static final int STATUS_EMV_FALLBACK = 203;
    public static final int STATUS_ERROR = 204;
    public static final int STATUS_INPUT_TIMEOUT = 205;
    public static final int STATUS_CARD_BLOCK = 206;

    public static final int STATUS_CONTINUE_MULTIPLE_RESPONSE = 9; // SDK 内部でのみ使用

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
