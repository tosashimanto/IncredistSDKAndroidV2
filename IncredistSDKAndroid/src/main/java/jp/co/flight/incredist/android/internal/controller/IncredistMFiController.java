package jp.co.flight.incredist.android.internal.controller;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;

import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.incredist.android.internal.controller.command.MFiDeviceInfoCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiEmvDisplayMessageCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaCloseCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaLedColorCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaOpenCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaOpenWithoutLedCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaSendCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiGetRealTimeCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiPinEntryDCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiScanMagneticCard2Command;
import jp.co.flight.incredist.android.internal.controller.command.MFiSetEncryptionModeCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiSetLedColorCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiSetRealTimeCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiStopCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiTfpDisplayMessageCommand;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.exception.ParameterException;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiTransport;
import jp.co.flight.incredist.android.model.EncryptionMode;
import jp.co.flight.incredist.android.model.LedColor;
import jp.co.flight.incredist.android.model.PinEntry;

/**
 * MFi版 Incredist 用 Controller.
 */
public class IncredistMFiController implements IncredistProtocolController {

    private IncredistController mController;

    private MFiTransport mMFiTransport;

    /**
     * コンストラクタ
     * @param controller IncredistController オブジェクト
     * @param connection BluetoothGattConnection オブジェクt
     */
    IncredistMFiController(@NonNull IncredistController controller, @NonNull BluetoothGattConnection connection) {
        mController = controller;
        mMFiTransport = new MFiTransport(connection);
    }

    /**
     * MFi コマンドを送信します
     * @param command 送信コマンド
     * @param callback コールバック
     */
    private void postMFiCommand(final MFiCommand command, final IncredistController.Callback callback) {
        mController.postCommand(command, () -> {
            callback.onResult(command.parseResponse(mMFiTransport.sendCommand(command)));
        }, callback);
    }

    @Override
    public boolean isBusy() {
        return mMFiTransport.isBusy();
    }

    /**
     * シリアル番号を取得します.
     * @param callback コールバック
     */
    public void getDeviceInfo(final IncredistController.Callback callback) {
        postMFiCommand(new MFiDeviceInfoCommand(), callback);
    }

    /**
     * EMV メッセージを表示します
     *
     * @param type メッセージ番号
     * @param message メッセージ文字列
     * @param callback コールバック
     */
    @Override
    public void emvDisplaymessage(int type, @Nullable String message, IncredistController.Callback callback) {
        postMFiCommand(new MFiEmvDisplayMessageCommand(type, message), callback);
    }

    /**
     * TFP メッセージを表示します
     *
     * @param type メッセージ番号
     * @param message メッセージ文字列
     * @param callback コールバック
     */
    @Override
    public void tfpDisplaymessage(int type, @Nullable String message, IncredistController.Callback callback) {
        postMFiCommand(new MFiTfpDisplayMessageCommand(type, message), callback);
    }

    /**
     * 暗号化モードを設定します
     *
     * @param mode 暗号化モード
     * @param callback コールバック
     */
    @Override
    public void setEncryptionMode(EncryptionMode mode, IncredistController.Callback callback) {
        postMFiCommand(new MFiSetEncryptionModeCommand(mode), callback);
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
    @Override
    public void pinEntryD(PinEntry.Type pinType, PinEntry.Mode pinMode, PinEntry.MaskMode mask, int min, int max, PinEntry.Alignment align, int line, long timeout, IncredistController.Callback callback) {
        try {
            postMFiCommand(new MFiPinEntryDCommand(pinType, pinMode, mask, min, max, align, line, timeout), callback);
        } catch (ParameterException ex) {
            mController.postCallback(() -> {
                callback.onResult(new IncredistResult(IncredistResult.STATUS_PARAMETER_ERROR));
            });
        }
    }

    /**
     * 磁気カードを読み取ります
     *
     * @param timeout タイムアウト時間(msec)
     * @param callback コールバック
     */
    @Override
    public void scanMagneticCard(long timeout, IncredistController.Callback callback) {
        postMFiCommand(new MFiScanMagneticCard2Command(timeout), callback);
    }

    /**
     * LED色を設定します。
     * @param color LED色
     * @param isOn true: 点灯 false: 消灯
     * @param callback コールバック
     */
    @Override
    public void setLedColor(LedColor color, boolean isOn, IncredistController.Callback callback) {
        postMFiCommand(new MFiSetLedColorCommand(color, isOn), callback);
    }

    /**
     * FeliCa RF モードを開始します
     *
     * @param withLed LED を点灯するかどうか
     * @param callback コールバック
     */
    public void felicaOpen(boolean withLed, final IncredistController.Callback callback) {
        MFiCommand command;
        if (withLed) {
            command = new MFiFelicaOpenCommand();
        } else {
            command = new MFiFelicaOpenWithoutLedCommand();
        }

        postMFiCommand(command, callback);
    }

    /**
     * FeliCa コマンドを送信します
     *
     * @param command 送信するFeliCaコマンド
     * @param callback コールバック
     */
    @Override
    public void felicaSendCommand(byte[] command, IncredistController.Callback callback) {
        postMFiCommand(new MFiFelicaSendCommand(command), callback);
    }

    /**
     * felica モード時のLED色を設定します。
     * @param color LED色
     * @param callback コールバック
     */
    @Override
    public void felicaLedColor(LedColor color, IncredistController.Callback callback) {
        postMFiCommand(new MFiFelicaLedColorCommand(color), callback);
    }

    /**
     * FeliCa RF モードを終了します
     *
     * @param callback コールバック
     */
    @Override
    public void felicaClose(IncredistController.Callback callback) {
        postMFiCommand(new MFiFelicaCloseCommand(), callback);
    }

    /**
     * Incredistに設定されている時刻を取得します
     *
     * @param callback コールバック
     */
    @Override
    public void rtcGetTime(IncredistController.Callback callback) {
        postMFiCommand(new MFiGetRealTimeCommand(), callback);
    }

    /**
     * Incredist に時刻を設定します
     *
     * @param cal 設定時刻
     * @param callback コールバック
     */
    @Override
    public void rtcSetTime(Calendar cal, IncredistController.Callback callback) {
        postMFiCommand(new MFiSetRealTimeCommand(cal), callback);
    }

    /**
     * Incredist に現在時刻を設定します
     *
     * @param callback コールバック
     */
    @Override
    public void rtcSetCurrentTime(IncredistController.Callback callback) {
        postMFiCommand(new MFiSetRealTimeCommand(), callback);
    }

    /**
     * Incredist を停止します。
     *
     * @param callback コールバック
     */
    @Override
    public void stop(IncredistController.Callback callback) {
        postMFiCommand(new MFiStopCommand(), callback);
    }

    /**
     * オブジェクトを解放します
     */
    @Override
    public void release() {
        mMFiTransport.release();
        mController = null;
        mMFiTransport = null;
    }

}
