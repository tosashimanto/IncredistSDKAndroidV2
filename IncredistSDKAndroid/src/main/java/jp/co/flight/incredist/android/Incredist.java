package jp.co.flight.incredist.android;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.support.annotation.NonNull;

import jp.co.flight.incredist.android.internal.controller.IncredistController;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * Incredist API クラス.
 */

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

        mController.destroy();
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
    public void getSerialNumber(@NonNull OnSerialNumberListener listener) {
        mController.getSerialNumber(new IncredistController.Callback() {
            @Override
            public void onResult(IncredistResult result) {
                //TODO

            }
        });
    }
}
