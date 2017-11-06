package jp.co.flight.incredist.android;

/**
 * 処理成功時のインタフェース.
 */
@SuppressWarnings("WeakerAccess")
public interface OnSuccessFunction<T> {
    void onSuccess(T result);
}
