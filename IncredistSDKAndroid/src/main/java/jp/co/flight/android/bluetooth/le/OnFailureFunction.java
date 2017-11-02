package jp.co.flight.android.bluetooth.le;

/**
 * 処理失敗時の closure インタフェース.
 */
@SuppressWarnings("WeakerAccess")
public interface OnFailureFunction<T> {
    void onFailure(int resultCode, T result);
}
