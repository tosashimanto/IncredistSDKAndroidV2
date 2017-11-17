package jp.co.flight.incredist.android.internal.controller;

import android.support.annotation.NonNull;

import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaCloseCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaOpenCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiFelicaSendCommand;
import jp.co.flight.incredist.android.internal.controller.command.MFiSerialNumberCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiResponse;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiTransport;

/**
 * MFi版 Incredist 用 Controller.
 */
public class IncredistMFiController implements IncredistProtocolController {

    @NonNull
    private final IncredistController mController;

    @NonNull
    private final MFiTransport mMFiTransport;

    IncredistMFiController(@NonNull IncredistController controller, @NonNull BluetoothGattConnection connection) {
        mController = controller;
        mMFiTransport = new MFiTransport(connection);
    }

    /**
     * シリアル番号を取得します.
     */
    public void getSerialNumber(final IncredistController.Callback callback) {
        mController.post(() -> {
            MFiSerialNumberCommand serialNumberCommand = new MFiSerialNumberCommand();
            MFiResponse response = mMFiTransport.sendCommand(serialNumberCommand);

            callback.onResult(serialNumberCommand.parseResponse(response));
        }, callback);
    }

    /**
     * FeliCa RF モードを開始します
     */
    public void felicaOpen(final IncredistController.Callback callback) {
        mController.post(() -> {
            MFiFelicaOpenCommand felicaOpenCommand = new MFiFelicaOpenCommand();
            MFiResponse response = mMFiTransport.sendCommand(felicaOpenCommand);

            callback.onResult(felicaOpenCommand.parseResponse(response));
        }, callback);
    }

    @Override
    public void felicaSendCommand(byte[] command, IncredistController.Callback callback) {
        mController.post(() -> {
            MFiFelicaSendCommand felicaSendCommand = new MFiFelicaSendCommand(command);
            MFiResponse response = mMFiTransport.sendCommand(felicaSendCommand);

            callback.onResult(felicaSendCommand.parseResponse(response));
        }, callback);
    }

    @Override
    public void felicaClose(IncredistController.Callback callback) {
        mController.post(() -> {
            MFiFelicaCloseCommand felicaCloseCommand = new MFiFelicaCloseCommand();
            MFiResponse response = mMFiTransport.sendCommand(felicaCloseCommand);

            callback.onResult(felicaCloseCommand.parseResponse(response));
        }, callback);
    }
}
