package jp.co.flight.incredist.android;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.co.flight.android.bluetooth.le.BluetoothCentral;
import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.android.bluetooth.le.BluetoothPeripheral;
import jp.co.flight.incredist.android.internal.controller.IncredistConstants;
import jp.co.flight.incredist.android.internal.util.FLog;

/**
 * Incredist 検索と接続の管理クラス.
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // for public API.
public class IncredistManager {
    private static final String TAG = "IncredistManager";

    private static final int CONNECT_ERROR_NOT_FOUND = 798;
    private static final int CONNECT_ERROR_TIMEOUT = 799;

    private final BluetoothCentral mCentral;

    /**
     * デバイス名によるフィルタ.
     */
    public class DeviceFilter {
        /**
         * 標準の Incredist デバイスかどうかをチェックします.
         *
         * @param deviceName Incredist デバイス名
         * @return Incredist デバイスとして有効なデバイス名の場合: True
         */
        public boolean isValid(String deviceName) {
            if (deviceName.startsWith(IncredistConstants.FS_INCREDIST_SERVICE_PREFIX1)
                    || deviceName.startsWith(IncredistConstants.FS_INCREDIST_SERVICE_PREFIX2)) {
                return true;
            }

            return false;
        }
    }

    /**
     * コンストラクタ.
     */
    public IncredistManager(Context context) {
        FLog.i(TAG, String.format(Locale.JAPANESE, "new IncredistManager context:%s", context));
        mCentral = new BluetoothCentral(context);
    }

    /**
     * Bluetooth デバイスのスキャンを開始します.
     * 内部処理用に　success 時には BluetoothPeripheral を取得できます.
     *
     * @param filter   Incredistデバイス名によるフィルタ
     * @param scanTime スキャン実行時間
     * @param success  スキャン完了時処理
     * @param failure  スキャン失敗時処理
     */
    private void bleStartScanInternal(@Nullable DeviceFilter filter, long scanTime, OnSuccessFunction<Map<String, BluetoothPeripheral>> success, OnFailureFunction failure) {
        final DeviceFilter deviceFilter = filter != null ? filter : new DeviceFilter();
        final Map<String, BluetoothPeripheral> peripheralMap = new HashMap<>();

        FLog.i(TAG, String.format(Locale.JAPANESE, "bleStartScanInternal scanTime:%d", scanTime));
        mCentral.startScan(scanTime, (successValue) -> {
            if (success != null) {
                FLog.i(TAG, String.format(Locale.JAPANESE, "bleStartScanInternal call onSuccess peripheralMap:%d", peripheralMap.size()));
                success.onSuccess(peripheralMap);
            }
        }, (errorCode, failureValue) -> {
            if (failure != null) {
                FLog.i(TAG, String.format(Locale.JAPANESE, "bleStartScanInternal call onFailure errorCode:%d", errorCode));
                failure.onFailure(errorCode);
            }
        }, (scanResult) -> {
            FLog.d(TAG, String.format(Locale.JAPANESE, "startScanInternal check valid name %s %s", scanResult.getDeviceName(), scanResult.getDeviceAddress()));
            if (deviceFilter.isValid(scanResult.getDeviceName())) {
                FLog.i(TAG, String.format(Locale.JAPANESE, "bleStartScanInternal found %s %s", scanResult.getDeviceName(), scanResult.getDeviceAddress()));
                peripheralMap.put(scanResult.getDeviceName(), scanResult);
            }
            return true;
        });
    }

    /**
     * Bluetooth デバイスのスキャンを開始します.
     *
     * @param filter   Incredistデバイス名によるフィルタ
     * @param scanTime スキャン実行時間
     * @param success  スキャン完了時処理
     * @param failure  スキャン失敗時処理
     */
    public void bleStartScan(@Nullable DeviceFilter filter, long scanTime, OnSuccessFunction<List<String>> success, OnFailureFunction failure) {
        bleStartScanInternal(filter, scanTime, (peripheralMap) -> {
            success.onSuccess(new ArrayList<>(peripheralMap.keySet()));
        }, failure);
    }

    /**
     * Bluetooth デバイスのスキャンを終了します.
     */
    public void bleStopScan() {
        FLog.i(TAG, "bleStopScan");
        mCentral.stopScan();
    }

    /**
     * connect メソッドで利用するリスナクラス.
     * handler - connectionListener - mConnection で相互参照が発生するため mConnection 変数を持つ
     */
    private abstract class FirstConnectionListener implements BluetoothGattConnection.ConnectionListener, Runnable {
        BluetoothGattConnection mConnection;
    }

    /**
     * Incredistデバイスに接続します.
     *
     * @param deviceName Incredistデバイス名
     * @param timeout    タイムアウト時間(単位 msec)
     * @param success    接続成功時処理
     * @param failure    接続失敗時処理
     */
    public void connect(String deviceName, long timeout, @Nullable OnSuccessFunction<Incredist> success, @Nullable OnFailureFunction failure) {
        FLog.i(TAG, String.format(Locale.JAPANESE, "connect device:%s timeout:%d", deviceName, timeout));

        bleStartScanInternal(null, timeout, (peripheralMap) -> {
            BluetoothPeripheral peripheral = peripheralMap.get(deviceName);
            if (peripheral != null) {
                Handler handler = mCentral.getHandler();

                final FirstConnectionListener connectionListener = new FirstConnectionListener() {
                    boolean mHasSucceed = false;
                    boolean mHasTimeout = false;

                    /**
                     * タイムアウト時処理.
                     */
                    @Override
                    public void run() {
                        mHasTimeout = true;
                        mConnection.close();
                        FLog.i(TAG, "connect timeout");
                        if (failure != null) {
                            failure.onFailure(CONNECT_ERROR_TIMEOUT);
                        }
                    }

                    /**
                     * 接続成功時処理.
                     *
                     * @param connection ペリフェラルとの接続オブジェクト
                     */
                    @Override
                    public void onConnect(BluetoothGattConnection connection) {
                        FLog.i(TAG, "connect succeed");
                        if (!mHasSucceed && !mHasTimeout && success != null) {
                            final Incredist incredist = new Incredist(IncredistManager.this, connection, deviceName);
                            mHasSucceed = true;
                            handler.removeCallbacks(this);
                            success.onSuccess(incredist);
                        }
                    }

                    @Override
                    public void onDisconnect(BluetoothGattConnection connection) {
                        FLog.i(TAG, "connect disconnected");
                        // do nothing.
                    }
                };
                final BluetoothGattConnection connection = mCentral.connect(peripheral, connectionListener);

                if (connection.getConnectionState() == BluetoothGatt.STATE_CONNECTED) {
                    FLog.i(TAG, "connect already connected");
                    final Incredist incredist = new Incredist(IncredistManager.this, connection, deviceName);
                    handler.post(() -> {
                        if (success != null) {
                            success.onSuccess(incredist);
                        }
                    });
                } else if (timeout > 0) {
                    // タイムアウト処理を handler に登録
                    connectionListener.mConnection = connection;
                    handler.postDelayed(connectionListener, timeout);
                }
            } else {
                FLog.i(TAG, "connect device not found.");
                Handler handler = mCentral.getHandler();
                handler.post(() -> {
                    if (failure != null) {
                        failure.onFailure(CONNECT_ERROR_NOT_FOUND);
                    }
                });
            }
        }, failure);
    }

    /**
     * API バージョンを取得します.
     *
     * @return バージョン文字列
     */
    public String getApiVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * IncredistManager の利用を終了します.
     */
    public void release() {
        mCentral.release();
    }
}
