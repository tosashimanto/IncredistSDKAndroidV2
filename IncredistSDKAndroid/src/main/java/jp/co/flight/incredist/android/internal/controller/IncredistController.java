package jp.co.flight.incredist.android.internal.controller;

import android.bluetooth.BluetoothGatt;
import android.os.Handler;
import android.os.HandlerThread;

import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
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

    private HandlerThread mHandlerThread = null;
    private Handler mHandler;

    private BluetoothGattConnection mConnection;

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
     * @param connection Bluetooth ペリフェラルとの接続オブジェクト
     */
    public IncredistController(BluetoothGattConnection connection, String deviceName) {
        mConnection = connection;
        mDeviceName = deviceName;

        mHandlerThread = new HandlerThread(String.format("%s:%s", TAG, deviceName)) {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                mHandler = new Handler(this.getLooper());
            }
        };
    }

    /**
     * HandlerThread で処理を実行します. 実行できなかった場合、
     * 実行失敗(STATUS_FAILED_EXECUTION) としてコールバックを呼び出します.
     *
     * @param r 処理内容の Runnable インスタンス
     */
    private void post(Runnable r, Callback callback) {
        Handler handler = mHandler;
        if (handler != null) {
            if (!handler.post(r)) {
                callback.onResult(new IncredistResult(STATUS_FAILED_EXECUTION));
            }
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
     * デバイスとの接続状態を取得します.
     *
     * @return 接続中: BluetoothGatt.STATE_CONNECTED(2), 切断中: BluetoothGatt.STATE_DISCONNECTED(0)
     */
    public int getConnectionState() {
        return mConnection.getConnectionState();
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
    public boolean release() {
        HandlerThread handlerThread = mHandlerThread;
        if (handlerThread != null) {
            if (handlerThread.quitSafely()) {
                mHandlerThread = null;
                return true;
            } else {
                return false;
            }
        }

        return true;
    }
}
