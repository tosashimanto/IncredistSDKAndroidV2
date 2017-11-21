package jp.co.flight.incredist.android.internal.controller;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;

import java.util.concurrent.CountDownLatch;

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

    /**
     * 接続先Incredistデバイス名.
     */
    private final String mDeviceName;

    /**
     * 移譲先 IncredistController
     */
    private IncredistProtocolController mProtoController;

    private HandlerThread mHandlerThread = null;
    private Handler mHandler;

    private BluetoothGattConnection mConnection;

    /**
     * API 層へ結果を返却するコールバックインタフェース.
     */
    public interface Callback {
        /**
         * 処理結果を返却します.
         *
         * @param result 処理結果
         */
        void onResult(IncredistResult result);
    }

    /**
     * コンストラクタ.
     *
     * @param connection Bluetooth ペリフェラルとの接続オブジェクト
     */
    public IncredistController(BluetoothGattConnection connection, String deviceName) {
        mConnection = connection;
        mDeviceName = deviceName;

        // 最初は MFi のみ対応
        mProtoController = new IncredistMFiController(this, connection);
        final CountDownLatch latch = new CountDownLatch(1);
        mHandlerThread = new HandlerThread(String.format("%s:%s", TAG, deviceName)) {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                mHandler = new Handler(this.getLooper());
                latch.countDown();
            }
        };
        mHandlerThread.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            // ignore.
        }
    }

    /**
     * HandlerThread で処理を実行します. 実行できなかった場合、
     * 実行失敗(STATUS_FAILED_EXECUTION) としてコールバックを呼び出します.
     * TODO すでに他の処理が実行中の場合 STATUS_BUSY としてコールバックを呼び出します.
     *
     * @param r 処理内容の Runnable インスタンス
     */
    void post(Runnable r, Callback callback) {
        Handler handler = mHandler;
        if (handler != null) {
            if (handler.post(r)) {
                return;
            }
        }
        if (callback != null) {
            callback.onResult(new IncredistResult(STATUS_FAILED_EXECUTION));
        }
    }

    /**
     * 接続中のデバイス名を取得します.
     *
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
     * デバイス情報を取得します.
     * @param callback コールバック
     */
    public void getDeviceInfo(Callback callback) {
        mProtoController.getDeviceInfo(callback);
    }


    /**
     * EMV メッセージを表示します
     */
    public void emvDisplaymessage(int type, @Nullable String message, IncredistController.Callback callback) {
        mProtoController.emvDisplaymessage(type, message, callback);
    }

    /**
     * TFP メッセージを表示します
     */
    public void tfpDisplaymessage(int type, @Nullable String message, IncredistController.Callback callback) {
        mProtoController.tfpDisplaymessage(type, message, callback);
    }

    /**
     * FeliCa RF モードを開始します
     * @param callback コールバック
     */
    public void felicaOpen(boolean withLed, Callback callback) {
        mProtoController.felicaOpen(withLed, callback);
    }

    /**
     * felica コマンドを送信します.
     * @param command コマンド
     * @param callback コールバック
     */
    public void felicaSendCommand(byte[] command, IncredistController.Callback callback) {
        mProtoController.felicaSendCommand(command, callback);
    }

    /**
     * felica モードを終了します。
     * @param callback コールバック
     */
    public void felicaClose(IncredistController.Callback callback) {
        mProtoController.felicaClose(callback);
    }

    /**
     * Incredist デバイスから切断します.
     * @param callback コールバック
     */
    public void disconnect(final Callback callback) {
        post(() -> {
            mConnection.disconnect();

            callback.onResult(new IncredistResult(IncredistResult.STATUS_SUCCESS));
        }, callback);
    }

    /**
     * Incredist デバイスとの接続を破棄します.
     */
    public boolean release() {
        mConnection.close();

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
