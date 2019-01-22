package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.WorkerThread;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

public interface MFiTransport {
    /**
     * コマンドを送信し、レスポンスを受信して返却します.
     *
     * @param commandList 送信コマンド
     * @return レスポンスの MFiパケット
     */
    @WorkerThread
    IncredistResult sendCommand(MFiCommand... commandList);

    /**
     * コマンドを送信し、レスポンスを受信して返却します.
     *
     * @param commandList 送信コマンド
     * @return レスポンスの MFiパケット
     */
    @WorkerThread
    IncredistResult sendEmvKernelSetupCommand(MFiCommand... commandList);

    /**
     * 受信待ち処理をキャンセル
     *
     * キャンセルできるかどうかは MFiCommand によって異なる
     * キャンセルできないコマンドの場合は STATUS_NOT_CANCELLABLE を返す
     *
     * キャンセルできるコマンドであっても、送信 / 受信待ち処理には、
     * - 送信前
     * - 送信中
     * - 受信待ち
     * - 受信中
     * - 受信完了後
     * の状態がある。送信前の場合は mCancelling != null をチェックし、
     * 受信待ちの場合は mResponse の notify を受け取ってそれぞれ countdown するので
     * キャンセル成功する。
     *
     * 送信中の場合はコマンド途中で停止させることはせずに送信完了まで一旦待つ
     *
     * 受信中(すでにMFiパケットの一部を受け取っている)場合には
     * フラグを立てて、MFiパケットの残りを受信した際にキャンセル済みの
     * コマンドについてはコールバックを呼び出さない。
     *
     * このメソッドは sendCommand とは別のスレッドで実行する必要がある
     *
     * @return キャンセル成功した場合は STATUS_SUCCESS, 失敗した場合はエラー結果を含む IncredistResult オブジェクト
     */
    @WorkerThread
    IncredistResult cancel();

    /**
     * リソースを解放します
     */
    void release();

    /**
     * @return
     */
    boolean isBusy();
}
