package jp.co.flight.incredist.android;

import android.support.annotation.Nullable;

import java.util.Locale;

import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.incredist.android.internal.controller.IncredistController;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.controller.result.SerialNumberResult;
import jp.co.flight.incredist.android.internal.util.FLog;
import jp.co.flight.incredist.android.model.FelicaCommandResult;

/**
 * Incredist API クラス.
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // for public API.
public class Incredist {
    private static final String TAG = "Incredist";
    /**
     * 生成元の IncredistManager インスタンス.
     */
    private final IncredistManager mManager;

    /**
     * IncredistController インスタンス.
     */
    private final IncredistController mController;

    /**
     * BluetoothGattConnection インスタンス.
     */
    private BluetoothGattConnection mConnection;

    /**
     * コンストラクタ. IncredistManager によって呼び出されます.
     *
     * @param connection Bluetooth ペリフェラルとの接続オブジェクト
     */
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

    /**
     * FeliCa アクセスのため、デバイスを FeliCa RF モードにします.
     */
    public void felicaOpen(@Nullable OnSuccessFunction<Void> success, @Nullable OnFailureFunction<Void> failure) {
        mController.felicaOpen(result -> {
            if (result.status == IncredistResult.STATUS_SUCCESS) {
                if (success != null) {
                    success.onSuccess(null);
                }
            } else {
                if (failure != null) {
                    FLog.d(TAG, String.format(Locale.JAPANESE, "falicaOpen: onFailure:%d %s", result.status, result.message));

                    failure.onFailure(result.status, null);
                }
            }
        });
    }

    /**
     * FeliCa コマンドを送信します.
     */
    public void felicaSendCommand(byte[] felicaCommand, @Nullable OnSuccessFunction<FelicaCommandResult> success, @Nullable OnFailureFunction<Void> failure) {
        mController.felicaSendCommand(felicaCommand, (IncredistResult result) -> {
            if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof jp.co.flight.incredist.android.internal.controller.result.FelicaCommandResult) {
                jp.co.flight.incredist.android.internal.controller.result.FelicaCommandResult felicaResult = (jp.co.flight.incredist.android.internal.controller.result.FelicaCommandResult) result;
                if (success != null) {
                    success.onSuccess(new FelicaCommandResult(felicaResult.status1, felicaResult.status2, felicaResult.resultData));
                }
            } else {
                if (failure != null) {
                    failure.onFailure(result.status, null);
                }
            }
        });
    }

    /**
     * FeliCa RF モードを終了します.
     */
    public void felicaClose(@Nullable OnSuccessFunction<Void> success, @Nullable OnFailureFunction<Void> failure) {
        mController.felicaClose(result -> {
            if (result.status == IncredistResult.STATUS_SUCCESS) {
                if (success != null) {
                    success.onSuccess(null);
                }
            } else {
                if (failure != null) {
                    failure.onFailure(result.status, null);
                }
            }
        });
    }


}
