package jp.co.flight.incredist.android;

import android.bluetooth.BluetoothGatt;
import android.support.annotation.Nullable;

import jp.co.flight.incredist.android.internal.controller.IncredistController;

/**
 * Incredist API クラス.
 */
@SuppressWarnings({ "WeakerAccess", "unused" }) // for public API.
public class Incredist {
    /**
     * 生成元の IncredistManager インスタンス
     */
    private final IncredistManager mManager;

    /**
     * IncredistController インスタンス.
     */
    private final IncredistController mController;

    /**
     * コンストラクタ. IncredistManager によって呼び出されます.
     *
     * @param deviceName デバイス名
     * @param deviceAddress Bluetoothアドレス
     */
    /* package */
    Incredist(IncredistManager manager, String deviceName, String deviceAddress) {
        mManager = manager;
        mController = new IncredistController(deviceName, deviceAddress);
    }

    /**
     * Incredistとの接続を切断します.
     */
    public void disconnect() {
        //TODO

        mController.release();
    }

    /**
     * 接続中のIncredistデバイス名を取得します.
     *
     * @return Incredistデバイス名
     */
    public String getDeviceName() {
        return mController.getDeviceName();
    }

    /**
     * Bluetooth の接続状態を取得します.
     *
     * @return 接続状態(BluetoothGatt クラスの定数)
     */
    public int getConnectionStatus() {
        //TODO
        return BluetoothGatt.STATE_DISCONNECTED;
    }

    /**
     * シリアル番号を取得します.
     *
     * @param success 取得成功時の処理
     * @param failure 取得失敗時の処理
     */
    public void getSerialNumber(@Nullable OnSuccessFunction<String> success, @Nullable OnFailureFunction<Void> failure) {
        mController.getSerialNumber(result -> {
            //TODO
        });
    }
}
