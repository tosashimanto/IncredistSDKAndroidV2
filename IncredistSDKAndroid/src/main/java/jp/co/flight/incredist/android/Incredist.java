package jp.co.flight.incredist.android;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.support.annotation.NonNull;

/**
 * Incredist API クラス.
 */

public class Incredist {
    private IncredistManager manager;
    private String deviceName;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice bluetoothDevice;

    /**
     * Incredistとの接続を切断します.
     */
    void disconnect() {
        //TODO
    }

    /**
     * 接続中のIncredistデバイス名を取得します.
     *
     * @return Incredistデバイス名
     */
    String getDeviceName() {
        return deviceName;
    }

    /**
     * Bluetooth の接続状態を取得します.
     *
     * @return 接続状態(BluetoothGatt クラスの定数)
     */
    int getConnectionStatus() {
        //TODO
        return BluetoothGatt.STATE_DISCONNECTED;
    }

    /**
     * シリアル番号取得結果のリスナ.
     */
    interface OnSerialNumberListener {
        /**
         * 正常取得時
         * @param incredist APIオブジェクト
         * @param serialNumber シリアル番号
         */
        void onSerialNumber(Incredist incredist, String serialNumber);

        /**
         * 取得失敗時
         * @param incredist APIオブジェクト
         * @param errorCode エラー番号
         */
        void onSerialNumberFailure(Incredist incredist, int errorCode);
    }

    /**
     * シリアル番号を取得します.
     * @param listener 結果取得用リスナ
     */
    void getSerialNumber(@NonNull OnSerialNumberListener listener) {
        //TODO
    }
}
