package jp.co.flight.incredist.android;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jp.co.flight.android.bluetooth.le.BluetoothCentral;
import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.android.bluetooth.le.BluetoothPeripheral;
import jp.co.flight.incredist.android.internal.util.FLog;

/**
 * Incredist 検索と接続の管理クラス.
 */
@SuppressWarnings({ "WeakerAccess", "unused" }) // for public API.
public class IncredistManager {
    private static final String TAG = "IncredistManager";
    private final BluetoothCentral mCentral;
    private Map<String, BluetoothPeripheral> mPeripheralMap = null;

    /**
     * デバイス名によるフィルタ.
     */
    public class DeviceFilter {
        /**
         * 標準の Incredist デバイスかどうかをチェックします.
         * @param deviceName Incredist デバイス名
         * @return Incredist デバイスとして有効なデバイス名の場合: True
         */
        public boolean isValid(String deviceName) {
            if (deviceName.startsWith("Samil") ||
                    deviceName.startsWith("FLT")) {
                return true;
            }

            return false;
        }
    }

    /**
     * コンストラクタ.
     */
    public IncredistManager(Context context) {
        mCentral = new BluetoothCentral(context);
    }

    /**
     * Bluetooth デバイスのスキャンを開始します.
     *
     * @param filter Incredistデバイス名によるフィルタ
     * @param scanTime スキャン実行時間
     * @param success スキャン完了時処理
     * @param failure スキャン失敗時処理
     */
    public void startScan(@Nullable DeviceFilter filter, long scanTime, OnSuccessFunction<List<String>> success, OnFailureFunction<Void> failure) {
        final Set<String> deviceSet = new HashSet<>();
        final DeviceFilter deviceFilter = filter != null ? filter : new DeviceFilter();
        mPeripheralMap = new HashMap<>();

        mCentral.startScan(scanTime, (_void)->{
            if (success != null) {
                success.onSuccess(new ArrayList<>(deviceSet));
            }
        }, (errorCode, _void)-> {
            if (failure != null) {
                failure.onFailure(errorCode, null);
            }
        }, (scanResult)-> {
            FLog.d(TAG, String.format(Locale.JAPANESE, "check valid name %s %s", scanResult.deviceName, scanResult.deviceAddress));
            if (deviceFilter.isValid(scanResult.deviceName)) {
                FLog.d(TAG, String.format(Locale.JAPANESE, "found %s %s", scanResult.deviceName, scanResult.deviceAddress));
                deviceSet.add(scanResult.deviceName);
                mPeripheralMap.put(scanResult.deviceName, scanResult);
            }
            return true;
        });
    }

    /**
     * Bluetooth デバイスのスキャンを終了します.
     */
    public void stopScan() {
        mCentral.stopScan();
    }

    /**
     * Incredistデバイスに接続します.
     *
     * @param deviceName Incredistデバイス名
     * @param timeout タイムアウト時間(単位 msec)
     * @param success 接続成功時処理
     * @param failure 接続失敗時処理
     */
    public void connect(String deviceName, long timeout, @Nullable OnSuccessFunction<Incredist> success, @Nullable OnFailureFunction<Void> failure) {
        BluetoothPeripheral peripheral = mPeripheralMap.get(deviceName);
        if (peripheral != null) {
            final BluetoothGattConnection connection = mCentral.connect(peripheral, new BluetoothGattConnection.ConnectionListener() {
                boolean hasSucceed = false;

                @Override
                public void onConnect(BluetoothGattConnection connection) {
                    if (!hasSucceed && success != null) {
                        final Incredist incredist = new Incredist(IncredistManager.this, connection, deviceName);
                        hasSucceed = true;
                        success.onSuccess(incredist);
                    }
                }

                @Override
                public void onDisconnect(BluetoothGattConnection connection) {
                    // do nothing.
                }
            });

            if (timeout > 0) {
                Handler handler = mCentral.getHandler();
                if (handler != null) {
                    handler.postDelayed(connection::close, timeout);
                }
            }
        }
    }

    /**
     * ログ出力を設定します.
     *
     * @param logLevel 出力レベル
     * @param logStream 出力先ストリーム
     */
    public void setLogLevel(int logLevel, @Nullable OutputStream logStream) {
        //TODO
    }

    /**
     * API バージョンを取得します.
     *
     * @return バージョン文字列
     */
    public String getAPIVersion() {
        return "0.0.1-alpha";
    }

    /**
     * IncredistManager の利用を終了します.
     */
    public void release() {
        mCentral.release();
    }
}
