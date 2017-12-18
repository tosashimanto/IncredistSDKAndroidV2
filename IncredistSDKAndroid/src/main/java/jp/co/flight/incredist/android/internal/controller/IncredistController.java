package jp.co.flight.incredist.android.internal.controller;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.util.concurrent.CountDownLatch;

import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.model.EncryptionMode;
import jp.co.flight.incredist.android.model.LedColor;
import jp.co.flight.incredist.android.model.PinEntry;

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

    private HandlerThread mCommandHandlerThread = null;
    private Handler mCommandHandler;
    private final HandlerThread mCallbackHandlerThread;
    private Handler mCallbackHandler;

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
        final CountDownLatch latch = new CountDownLatch(2);
        mCommandHandlerThread = new HandlerThread(String.format("%s:%s:command", TAG, deviceName)) {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                mCommandHandler = new Handler(this.getLooper());
                latch.countDown();
            }
        };
        mCommandHandlerThread.start();

        mCallbackHandlerThread = new HandlerThread(String.format("%s:%s:callback", TAG, deviceName)) {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                mCallbackHandler = new Handler(this.getLooper());
                latch.countDown();
            }
        };
        mCallbackHandlerThread.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            // ignore.
        }
    }

    /**
     * Command 送受信用の HandlerThread で処理を実行します. 実行できなかった場合、
     * 実行失敗(STATUS_FAILED_EXECUTION) としてコールバックを呼び出します.
     * TODO すでに他の処理が実行中の場合 STATUS_BUSY としてコールバックを呼び出します.
     *
     * @param r 処理内容の Runnable インスタンス
     */
    void postCommand(Runnable r, Callback callback) {
        Handler handler = mCommandHandler;
        if (handler != null) {
            if (handler.post(r)) {
                return;
            }
        }
        if (callback != null) {
            postCallback(() -> {
                callback.onResult(new IncredistResult(STATUS_FAILED_EXECUTION));
            });
        }
    }

    /**
     * callback 用の HandlerThread で処理を実行します。
     * @param runnable 処理内容の runnable インスタンス
     */
    public void postCallback(Runnable runnable) {
        Handler handler = mCallbackHandler;
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }

        handler.post(runnable);
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
     *
     * @param callback コールバック
     */
    public void getDeviceInfo(Callback callback) {
        mProtoController.getDeviceInfo(callback);
    }


    /**
     * EMV メッセージを表示します
     *
     * @param type メッセージ番号
     * @param message メッセージ文字列
     * @param callback コールバック
     */
    public void emvDisplaymessage(int type, @Nullable String message, Callback callback) {
        mProtoController.emvDisplaymessage(type, message, callback);
    }

    /**
     * TFP メッセージを表示します
     *
     * @param type メッセージ番号
     * @param message メッセージ文字列
     * @param callback コールバック
     */
    public void tfpDisplaymessage(int type, @Nullable String message, Callback callback) {
        mProtoController.tfpDisplaymessage(type, message, callback);
    }

    /**
     * 暗号化モードを設定します
     *
     * @param mode 暗号化モード
     * @param callback コールバック　
     */
    public void setEncryptionMode(EncryptionMode mode, Callback callback) {
        mProtoController.setEncryptionMode(mode, callback);
    }

    /**
     * PIN 入力を行います
     *
     * @param pinType PIN入力タイプ
     * @param pinMode PIN暗号化モード
     * @param mask 表示マスク
     * @param min 最小桁数
     * @param max 最大桁数
     * @param align 表示左右寄せ
     * @param line 表示行
     * @param timeout タイムアウト時間(msec)
     * @param callback コールバック
     */
    public void pinEntryD(PinEntry.Type pinType, PinEntry.Mode pinMode, PinEntry.MaskMode mask, int min, int max, PinEntry.Alignment align, int line, long timeout, Callback callback) {
        mProtoController.pinEntryD(pinType, pinMode, mask, min, max, align, line, timeout, callback);
    }

    /**
     * 磁気カードを読み取ります
     *
     * @param timeout タイムアウト時間(msec)
     * @param callback コールバック
     */
    public void scanMagneticCard(long timeout, Callback callback) {
        mProtoController.scanMagneticCard(timeout, callback);
    }

    /**
     * LED色を設定します。
     * @param color LED色
     * @param isOn true: 点灯 false: 消灯
     * @param callback コールバック
     */
    public void setLedColor(LedColor color, boolean isOn, IncredistController.Callback callback) {
        mProtoController.setLedColor(color, isOn, callback);
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
     * felica モード時のLED色を設定します。
     * @param color LED色
     * @param callback コールバック
     */
    public void felicaLedColor(LedColor color, IncredistController.Callback callback) {
        mProtoController.felicaLedColor(color, callback);
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
        postCommand(() -> {
            mConnection.disconnect();

            callback.onResult(new IncredistResult(IncredistResult.STATUS_SUCCESS));
        }, callback);
    }

    /**
     * Incredist デバイスとの接続を破棄します.
     */
    public boolean release() {
        mConnection.close();

        HandlerThread handlerThread = mCommandHandlerThread;
        if (handlerThread != null) {
            if (handlerThread.quitSafely()) {
                mCommandHandlerThread = null;
                return true;
            } else {
                return false;
            }
        }

        mConnection = null;
        mProtoController = null;

        return true;
    }
}
