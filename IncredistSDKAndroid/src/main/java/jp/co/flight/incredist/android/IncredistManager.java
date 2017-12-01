package jp.co.flight.incredist.android;

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

    private static final int CONNECT_ERROR_NOT_FOUND = 1798;
    private static final int CONNECT_ERROR_TIMEOUT = 1799;

    private final BluetoothCentral mCentral;

    IncredistConnectionListener mConnectionListener;

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
     */
    private class IncredistConnectionListener implements BluetoothGattConnection.ConnectionListener {
        private final Handler mHandler;

        BluetoothGattConnection.ConnectionListener mListener;
        BluetoothGattConnection mConnection;

        IncredistConnectionListener(Handler handler) {
            mHandler = handler;
        }

        @Override
        public void onConnect(BluetoothGattConnection connection) {
            if (mListener != null) {
                mListener.onConnect(connection);
            }
        }

        @Override
        public void onDisconnect(BluetoothGattConnection connection) {
            if (mListener != null) {
                mListener.onDisconnect(connection);
            }
        }

        void startConnect(BluetoothPeripheral peripheral, long timeout, OnSuccessFunction<Incredist> success, OnFailureFunction failure) {
            mListener = new OnConnectListener(peripheral, timeout, success, failure);
        }

        void setupDisconnect(Incredist incredist, OnSuccessFunction<Incredist> success, OnFailureFunction failure) {
            mListener = new OnDisconnectListener(incredist, success, failure);
        }

        public void clearListener() {
            mListener = null;
        }

        /**
         * connect 開始待ち処理
         */
        class OnConnectListener implements BluetoothGattConnection.ConnectionListener, Runnable {
            private final BluetoothPeripheral mPeripheral;
            private final OnSuccessFunction<Incredist> mSuccessFunction;
            private final OnFailureFunction mFailureFunction;

            boolean mHasSucceed = false;
            boolean mHasTimeout = false;

            public OnConnectListener(BluetoothPeripheral peripheral, long timeout, OnSuccessFunction<Incredist> success, OnFailureFunction failure) {
                mPeripheral = peripheral;
                mSuccessFunction = success;
                mFailureFunction = failure;

                final BluetoothGattConnection connection = mCentral.connect(peripheral, IncredistConnectionListener.this);
                if (timeout > 0) {
                    // タイムアウト処理を handler に登録
                    mConnection = connection;
                    mHandler.postDelayed(this, timeout);
                }
            }

            /**
             * タイムアウト時処理.
             */
            @Override
            public void run() {
                synchronized (this) {
                    if (!mHasSucceed) {
                        mHasTimeout = true;
                        mConnection.close();
                        FLog.i(TAG, "OnConnectListener connect timeout");
                        if (mFailureFunction != null) {
                            mFailureFunction.onFailure(CONNECT_ERROR_TIMEOUT);
                        }
                    }
                }
            }

            /**
             * 接続成功時処理.
             *
             * @param connection ペリフェラルとの接続オブジェクト
             */
            @Override
            public void onConnect(BluetoothGattConnection connection) {
                synchronized (this) {
                    FLog.i(TAG, "OnConnectListener connect succeed");
                    if (!mHasSucceed && !mHasTimeout && mSuccessFunction != null) {
                        final Incredist incredist = new Incredist(IncredistManager.this, connection, mPeripheral.getDeviceName());
                        mHasSucceed = true;
                        mHandler.removeCallbacks(this);
                        mSuccessFunction.onSuccess(incredist);
                    }
                }
            }

            @Override
            public void onDisconnect(BluetoothGattConnection connection) {
                FLog.i(TAG, "OnConnectListener disconnected");
                // do nothing.
            }
        }

        /**
         * setupDisconnect 時のリスナ
         */
        class OnDisconnectListener implements BluetoothGattConnection.ConnectionListener {
            private final Incredist mIncredist;
            private final OnSuccessFunction<Incredist> mSuccessFunction;
            private final OnFailureFunction mFailureFunction;

            boolean mHasSucceed = false;

            public OnDisconnectListener(Incredist incredist, OnSuccessFunction<Incredist> success, OnFailureFunction failure) {
                mIncredist = incredist;
                mSuccessFunction = success;
                mFailureFunction = failure;
            }

            @Override
            public void onConnect(BluetoothGattConnection connection) {
                FLog.i(TAG, "OnDisconnectListener connected");
                // do nothing.
            }

            @Override
            public void onDisconnect(BluetoothGattConnection connection) {
                FLog.i(TAG, "OnDisconnectListener disconnect succeed");
                if (mSuccessFunction != null) {
                    mSuccessFunction.onSuccess(mIncredist);
                }
            }
        }
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
            Handler handler = mCentral.getHandler();
            if (peripheral != null) {
                mConnectionListener = new IncredistConnectionListener(handler);
                mConnectionListener.startConnect(peripheral, timeout, success, failure);
            } else {
                FLog.i(TAG, "connect device not found.");
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

    /**
     * Incredist との接続を切断します
     */
    void setupDisconnect(Incredist incredist, OnSuccessFunction<Incredist> success, OnFailureFunction failure) {
        mConnectionListener.setupDisconnect(incredist, success, failure);
    }

    /**
     * ConnectionListener の設定をクリアします
     */
    void resetConnectionListener() {
        mConnectionListener.clearListener();
    }

}
