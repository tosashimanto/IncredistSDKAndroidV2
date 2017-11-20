package jp.co.flight.incredist.android.internal.controller;

import android.support.annotation.NonNull;

import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaCloseCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaOpenCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaOpenWithoutLedCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaSendCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiSerialNumberCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiTransport;

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
    public void getSerialNumber(final IncredistController.Callback callback) {
        postMFiCommand(new MFiSerialNumberCommand(), callback);
    }

    /**
     * FeliCa RF モードを開始します
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
     * @param command 送信するFeliCaコマンド
     * @param callback コールバック
     */
    @Override
    public void felicaSendCommand(byte[] command, IncredistController.Callback callback) {
        postMFiCommand(new MFiFelicaSendCommand(command), callback);
    }

    /**
     * FeliCa RF モードを終了します
     * @param callback コールバック
     */
    @Override
    public void felicaClose(IncredistController.Callback callback) {
        postMFiCommand(new MFiFelicaCloseCommand(), callback);
    }
}
