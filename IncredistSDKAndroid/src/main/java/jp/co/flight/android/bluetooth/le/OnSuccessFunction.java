package jp.co.flight.android.bluetooth.le;

/**
 * 処理成功時のインタフェース.
 */
@SuppressWarnings("WeakerAccess")
public interface OnSuccessFunction<T> {
    void onSuccess(T result);
}
