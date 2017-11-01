package jp.co.flight.incredist.android.internal.controller;

import android.os.Handler;
import android.os.HandlerThread;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

import static jp.co.flight.incredist.android.internal.controller.result.IncredistResult.STATUS_FAILED_EXECUTION;

/**
 * Incredist 制御クラス.
 *
 * Incredist への送受信制御を行う実体クラス. HandlerThread を内部で保持していて
 * 内部処理及び API のコールバックは HandlerThread コンテキストで呼び出す.
 */

public class IncredistController {
    private static final String TAG = "IncredistController";

    private final String mDeviceName;
    private final String mDeviceAddress;

    private HandlerThread mThread = null;

    /**
     * API 層へ結果を返却するコールバックインタフェース.
     */
    public interface Callback {
        /**
         * 処理結果を返却します.
         * @param result 処理結果
         */
        void onResult(IncredistResult result);
    }

    /**
     * コンストラクタ.
     * @param deviceName Incredistデバイス名
     * @param deviceAddress IncredistデバイスのBluetoothアドレス
     */
    public IncredistController(String deviceName, String deviceAddress) {
        mDeviceName = deviceName;
        mDeviceAddress = deviceAddress;

        mThread = new HandlerThread(String.format("%s:%s", TAG, deviceName));
    }

    /**
     * HandlerThread で処理を実行します. 実行できなかった場合、
     * 実行失敗(STATUS_FAILED_EXECUTION) としてコールバックを呼び出します.
     *
     * @param r 処理内容の Runnable インスタンス
     */
    private void post(Runnable r, Callback callback) {
        Handler handler = new Handler(mThread.getLooper());
        if (!handler.post(r)) {
            callback.onResult(new IncredistResult(STATUS_FAILED_EXECUTION));
        }
    }

    /**
     * 接続中のデバイス名を取得します.
     * @return デバイス名
     */
    public String getDeviceName() {
        return mDeviceName;
    }

    /**
     * シリアル番号を取得します.
     */
    public void getSerialNumber(final Callback callback) {
        post(new Runnable() {
            @Override
            public void run() {
                // TODO
            }
        }, callback);
    }

    /**
     * Incredist デバイスとの接続を破棄します.
     */
    public void destroy() {
        if (mThread.quitSafely()) {
            mThread = null;
        }
    }
}
