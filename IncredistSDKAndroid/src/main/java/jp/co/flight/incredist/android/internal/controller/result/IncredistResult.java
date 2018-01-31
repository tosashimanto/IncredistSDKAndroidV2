package jp.co.flight.incredist.android.internal.controller.result;

import jp.co.flight.incredist.android.model.StatusCode;

/**
 * Incredist の処理結果の基底クラス.
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // for public API.
public class IncredistResult implements StatusCode {
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
