package jp.co.flight.incredist.android;

/**
 * 処理失敗時のインタフェース.
 */
@SuppressWarnings("WeakerAccess")
public interface OnFailureFunction<T> {
    void onFailure(int resultCode, T result);
}
