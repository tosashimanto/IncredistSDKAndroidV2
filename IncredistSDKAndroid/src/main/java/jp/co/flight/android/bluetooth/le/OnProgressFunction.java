package jp.co.flight.android.bluetooth.le;

/**
 * 処理進行状況のインタフェース.
 */
@SuppressWarnings("WeakerAccess")
public interface OnProgressFunction<T> {
    boolean onProgress(T progress);
}
