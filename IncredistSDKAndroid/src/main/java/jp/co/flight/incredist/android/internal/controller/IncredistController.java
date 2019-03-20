package jp.co.flight.incredist.android.internal.controller;

import android.bluetooth.BluetoothGatt;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;

import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.model.CreditCardType;
import jp.co.flight.incredist.android.model.EmvSetupDataType;
import jp.co.flight.incredist.android.model.EmvTagType;
import jp.co.flight.incredist.android.model.EmvTransactionType;
import jp.co.flight.incredist.android.model.EncryptionMode;
import jp.co.flight.incredist.android.model.LedColor;
import jp.co.flight.incredist.android.model.PinEntry;

import static jp.co.flight.incredist.android.internal.controller.result.IncredistResult.STATUS_BUSY;
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
     * TODO USB の時は?
     */
    private final String mDeviceName;

    /**
     * 移譲先 IncredistController
     */
    @NonNull
    private IncredistProtocolController mProtoController;

    private HandlerThread mCommandHandlerThread;
    private Handler mCommandHandler;
    private HandlerThread mCallbackHandlerThread;
    private Handler mCallbackHandler;
    private HandlerThread mCancelHandlerThread;
    private Handler mCancelHandler;
    private HandlerThread mStopHandlerThread;
    private Handler mStopHandler;

    //TODO mConnection に依存している処理は全部 protoController へ移動する
    @Nullable
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
     * BLE 用コンストラクタ.
     *
     * @param connection Bluetooth ペリフェラルとの接続オブジェクト
     * @param deviceName デバイス名
     */
    public IncredistController(@NonNull BluetoothGattConnection connection, String deviceName) {
        mConnection = connection;
        mDeviceName = deviceName;

        // 最初は MFi のみ対応
        mProtoController = new IncredistBleMFiController(this, connection);
        createThreads(mDeviceName);
    }

    /**
     * USB 用コンストラクタ
     *
     * @param connection   UsbDeviceConnnection オブジェクト
     * @param usbInterface UsbInterface オブジェクト
     */
    public IncredistController(UsbDeviceConnection connection, UsbInterface usbInterface) {
        mDeviceName = "USBIncredist";

        mProtoController = new IncredistUsbMFiController(this, connection, usbInterface);
        createThreads(mDeviceName);
    }

    private void createThreads(String deviceName) {
        final CountDownLatch latch = new CountDownLatch(4);
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

        mCancelHandlerThread = new HandlerThread(String.format("%s:%s:cancel", TAG, deviceName)) {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                mCancelHandler = new Handler(this.getLooper());
                latch.countDown();
            }
        };
        mCancelHandlerThread.start();

        mStopHandlerThread = new HandlerThread(String.format("%s:%s:stop", TAG, deviceName)) {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                mStopHandler = new Handler(this.getLooper());
                latch.countDown();
            }
        };
        mStopHandlerThread.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            // ignore.
        }
    }

    void postCommand(Runnable r, Callback callback) {
        postCommand(r, callback, false);
    }

    /**
     * Command 送受信用の HandlerThread で処理を実行します. 実行できなかった場合、
     * 実行失敗(STATUS_FAILED_EXECUTION) としてコールバックを呼び出します.
     * すでに他の処理が実行中の場合 STATUS_BUSY としてコールバックを呼び出します.
     *
     * @param r 処理内容の Runnable インスタンス
     * @param callback　コールバック
     * @param isStopCommand true:stopコマンド実行時
     *                      false:stopコマンド以外実行時
     */
    void postCommand(Runnable r, Callback callback, boolean isStopCommand) {

        // ANDROID_SDK_DEV-36 stopコマンドの場合にはstopコマンド用のスレッドで実行
        Handler handler = isStopCommand ? mStopHandler : mCommandHandler;

        if (handler != null) {
            if (mProtoController.isBusy()) {
                // すでに他の処理が実行中の場合
                postCallback(() -> {
                    callback.onResult(new IncredistResult(STATUS_BUSY));
                });
                return;
            }

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
     *
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
     * cancel 用の HandlerThread で処理を実行します。
     *
     * @param runnable 処理内容の runnable インスタンス
     */
    public void postCancel(Runnable runnable) {
        Handler handler = mCancelHandler;
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
        if (mConnection != null) {
            return mConnection.getConnectionState();
        } else {
            return BluetoothGatt.STATE_DISCONNECTED;
        }
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
     * ブートローダのバージョンを取得します
     *
     * @param callback コールバック
     */
    public void getBootloaderVersion(Callback callback) {
        mProtoController.getBootloaderVersion(callback);
    }

    /**
     * EMV メッセージを表示します
     *
     * @param type     メッセージ番号
     * @param message  メッセージ文字列
     * @param callback コールバック
     */
    public void emvDisplayMessage(int type, @Nullable String message, Callback callback) {
        mProtoController.emvDisplayMessage(type, message, callback);
    }

    /**
     * TFP メッセージを表示します
     *
     * @param type     メッセージ番号
     * @param message  メッセージ文字列
     * @param callback コールバック
     */
    public void tfpDisplayMessage(int type, @Nullable String message, Callback callback) {
        mProtoController.tfpDisplayMessage(type, message, callback);
    }

    /**
     * 暗号化モードを設定します
     *
     * @param mode     暗号化モード
     * @param callback コールバック
     */
    public void setEncryptionMode(EncryptionMode mode, Callback callback) {
        mProtoController.setEncryptionMode(mode, callback);
    }

    /**
     * PIN 入力を行います
     *
     * @param pinType  PIN入力タイプ
     * @param pinMode  PIN暗号化モード
     * @param mask     表示マスク
     * @param min      最小桁数
     * @param max      最大桁数
     * @param align    表示左右寄せ
     * @param line     表示行
     * @param timeout  タイムアウト時間(msec)
     * @param callback コールバック
     */
    public void pinEntryD(PinEntry.Type pinType, PinEntry.Mode pinMode, PinEntry.MaskMode mask, int min, int max, PinEntry.Alignment align, int line, long timeout, Callback callback) {
        mProtoController.pinEntryD(pinType, pinMode, mask, min, max, align, line, timeout, callback);
    }

    /**
     * 磁気カードを読み取ります
     *
     * @param timeout  タイムアウト時間(msec)
     * @param callback コールバック
     */
    public void scanMagneticCard(long timeout, Callback callback) {
        mProtoController.scanMagneticCard(timeout, callback);
    }

    /**
     * 決済用にクレジットカード(EMV 接触・非接触 と磁気カード)を読み取ります
     *
     * @param cardTypeSet     カード種別
     * @param amount          決済金額
     * @param tagType         タグ種別
     * @param aidSetting      AID設定
     * @param transactionType トランザクション種別
     * @param fallback        フォールバック処理を実行するかどうか
     * @param timeout         タイムアウト時間(msec)
     * @param callback        コールバック
     */
    public void scanCreditCard(EnumSet<CreditCardType> cardTypeSet, long amount, EmvTagType tagType, int aidSetting, EmvTransactionType transactionType, boolean fallback, long timeout, Callback callback) {
        mProtoController.scanCreditCard(cardTypeSet, amount, tagType, aidSetting, transactionType, fallback, timeout, callback);
    }

    /**
     * LED色を設定します。
     *
     * @param color    LED色
     * @param isOn     true: 点灯 false: 消灯
     * @param callback コールバック
     */
    public void setLedColor(LedColor color, boolean isOn, IncredistController.Callback callback) {
        mProtoController.setLedColor(color, isOn, callback);
    }

    /**
     * FeliCa RF モードを開始します
     *
     * @param callback コールバック
     */
    public void felicaOpen(boolean withLed, Callback callback) {
        mProtoController.felicaOpen(withLed, callback);
    }

    /**
     * felica コマンドを送信します.
     *
     * @param command  コマンド
     * @param wait     ウェイト(単位: msec)
     * @param callback コールバック
     */
    public void felicaSendCommand(byte[] command, int wait, IncredistController.Callback callback) {
        mProtoController.felicaSendCommand(command, wait, callback);
    }

    /**
     * felica モード時のLED色を設定します。
     *
     * @param color    LED色
     * @param callback コールバック
     */
    public void felicaLedColor(LedColor color, IncredistController.Callback callback) {
        mProtoController.felicaLedColor(color, callback);
    }

    /**
     * felica モードを終了します。
     *
     * @param callback コールバック
     */
    public void felicaClose(IncredistController.Callback callback) {
        mProtoController.felicaClose(callback);
    }

    /**
     * Incredistに設定されている時刻を取得します
     *
     * @param callback コールバック
     */
    public void rtcGetTime(IncredistController.Callback callback) {
        mProtoController.rtcGetTime(callback);
    }

    /**
     * Incredist に時刻を設定します
     *
     * @param cal      設定時刻
     * @param callback コールバック
     */
    public void rtcSetTime(Calendar cal, IncredistController.Callback callback) {
        mProtoController.rtcSetTime(cal, callback);
    }

    /**
     * Incredist に現在時刻を設定します
     *
     * @param callback コールバック
     */
    public void rtcSetCurrentTime(IncredistController.Callback callback) {
        mProtoController.rtcSetCurrentTime(callback);
    }

    /**
     * EMV kernel に setup データを送信します
     *
     * @param type      設定種別
     * @param setupData setupデータ
     * @param callback  コールバック
     */
    public void emvKernelSetup(EmvSetupDataType type, byte[] setupData, IncredistController.Callback callback) {
        mProtoController.emvKernelSetup(type, setupData, callback);
    }

    /**
     * Incredist の EMVカーネル設定情報をチェックします
     *
     * @param type     設定種別
     * @param hashData 設定値のハッシュデータ
     * @param callback コールバック
     */
    public void emvCheckKernelSetting(EmvSetupDataType type, byte[] hashData, IncredistController.Callback callback) {
        mProtoController.emvCheckKernelSetting(type, hashData, callback);
    }

    /**
     * EMV kernel に ARC データを送信します
     *
     * @param arcData  ARCデータ
     * @param callback コールバック
     */
    public void emvSendArc(byte[] arcData, IncredistController.Callback callback) {
        mProtoController.emvSendArc(arcData, callback);
    }

    /**
     * icカードの挿入状態をチェックします
     *
     * @param callback コールバック
     */
    public void emvCheckCardStatus(Callback callback) {
        mProtoController.emvCheckCardStatus(callback);
    }


    /**
     * 電子マネー向けの画面・LED点滅します
     *
     * @param isBlink  画面点滅開始の場合 true, 点滅停止の場合 false を指定
     * @param color    LEDの点灯時の色
     * @param duration 点灯時間(msec)
     */
    public void emoneyBlink(boolean isBlink, LedColor color, int duration, Callback callback) {
        mProtoController.emoneyBlink(isBlink, color, duration, callback);
    }

    /**
     * 現在処理中のコマンドをキャンセルします
     *
     * @param callback コールバック
     */
    public void cancel(Callback callback) {
        mProtoController.cancel(callback);
    }

    /**
     * Incredist を停止します。
     *
     * @param callback コールバック
     */
    public void stop(IncredistController.Callback callback) {
        mProtoController.stop(callback);
    }

    /**
     * Incredist デバイスから切断します.
     */
    public void disconnect(IncredistController.Callback callback) {
        mProtoController.disconnect(callback);
    }

    /**
     * Incredist デバイスとの接続を破棄します.
     */
    public boolean release() {
        HandlerThread handlerThread = mCommandHandlerThread;
        if (handlerThread != null) {
            if (handlerThread.quitSafely()) {
                mCommandHandlerThread = null;
            } else {
                return false;
            }
        }

        handlerThread = mCallbackHandlerThread;
        if (handlerThread != null) {
            if (handlerThread.quitSafely()) {
                mCallbackHandlerThread = null;
            } else {
                return false;
            }
        }

        handlerThread = mCancelHandlerThread;
        if (handlerThread != null) {
            if (handlerThread.quitSafely()) {
                mCancelHandlerThread = null;
            } else {
                return false;
            }
        }

        handlerThread = mStopHandlerThread;
        if (handlerThread != null) {
            if (handlerThread.quitSafely()) {
                mStopHandlerThread = null;
            } else {
                return false;
            }
        }

        mCommandHandler = null;
        mCancelHandler = null;
        mCancelHandler = null;
        mStopHandler = null;

        mProtoController.release();
        mConnection = null;

        return true;
    }
}
