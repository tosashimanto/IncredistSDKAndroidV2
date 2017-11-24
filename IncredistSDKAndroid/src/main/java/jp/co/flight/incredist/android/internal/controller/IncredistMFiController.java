package jp.co.flight.incredist.android.internal.controller;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.incredist.android.internal.controller.command.MFiDeviceInfoCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiEmvDisplayMessageCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaCloseCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaOpenCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaOpenWithoutLedCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaSendCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiSetEncryptionModeCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiTfpDisplayMessageCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiTransport;
import jp.co.flight.incredist.android.model.EncryptionMode;

/**
 * MFi版 Incredist 用 Controller.
 */
public class IncredistMFiController implements IncredistProtocolController {

    @NonNull
    private final IncredistController mController;

    @NonNull
    private final MFiTransport mMFiTransport;

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
        mController.post(() -> {
            callback.onResult(command.parseResponse(mMFiTransport.sendCommand(command)));
        }, callback);
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
     * FeliCa RF モードを終了します
     *
     * @param callback コールバック
     */
    @Override
    public void felicaClose(IncredistController.Callback callback) {
        postMFiCommand(new MFiFelicaCloseCommand(), callback);
    }
}
