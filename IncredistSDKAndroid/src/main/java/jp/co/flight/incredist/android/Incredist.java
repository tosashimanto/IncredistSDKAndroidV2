package jp.co.flight.incredist.android;

import android.support.annotation.Nullable;

import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.incredist.android.internal.controller.IncredistController;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.controller.result.SerialNumberResult;

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
     * BluetoothGattConnection インスタンス.
     */
    private BluetoothGattConnection mConenction;

    /**
     * コンストラクタ. IncredistManager によって呼び出されます.
     *
     * @param connection Bluetooth ペリフェラルとの接続オブジェクト
     */
    /* package */
    Incredist(IncredistManager manager, BluetoothGattConnection connection, String deviceName) {
        mManager = manager;
        mController = new IncredistController(connection, deviceName);
    }

    /**
     * Incredistとの接続を切断します.
     */
    public void disconnect(@Nullable OnSuccessFunction<Incredist> success, @Nullable OnFailureFunction<Incredist> failure) {
        mController.disconnect(result -> {
            if (result.status == IncredistResult.STATUS_SUCCESS) {
                mController.release();
                if (success != null) {
                    success.onSuccess(this);
                }
            } else {
                if (failure != null) {
                    failure.onFailure(result.status, this);
                }
            }
        });
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
    public int getConnectionState() {
        return mController.getConnectionState();
    }

    /**
     * シリアル番号を取得します.
     *
     * @param success 取得成功時の処理
     * @param failure 取得失敗時の処理
     */
    public void getSerialNumber(@Nullable OnSuccessFunction<String> success, @Nullable OnFailureFunction<Void> failure) {
        mController.getSerialNumber(result -> {
            if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof SerialNumberResult) {
                if (success != null) {
                    success.onSuccess(((SerialNumberResult) result).serialNumber);
                }
            } else {
                if (failure != null) {
                    failure.onFailure(result.status, null);
                }
            }
        });
    }

}
