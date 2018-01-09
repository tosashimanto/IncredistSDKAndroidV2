package jp.co.flight.incredist.android;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
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
    public static class DeviceFilter {
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
        Map<String, BluetoothPeripheral> peripheralMap = new HashMap<>();

        FLog.i(TAG, String.format(Locale.JAPANESE, "bleStartScanInternal scanTime:%d", scanTime));
        mCentral.startScan(scanTime, (successValue) -> {
            if (success != null) {
                FLog.i(TAG, String.format(Locale.JAPANESE, "bleStartScanInternal call onSuccess mPeripheralMap:%d", peripheralMap.size()));
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
     * connect / disconnect メソッドで利用するリスナクラス.
     * 処理を内部クラスに移譲
     */
    private static class IncredistConnectionListener implements BluetoothGattConnection.ConnectionListener {
        private final IncredistManager mManager;
        private final Handler mHandler;

        /**
         * 接続時と切断時でリスナ実体を切り替えるためのインスタンス変数
         */
        BluetoothGattConnection.ConnectionListener mListener;
        BluetoothGattConnection mConnection;

        IncredistConnectionListener(IncredistManager manager, Handler handler) {
            mManager = manager;
            mHandler = handler;
        }

        @Override
        public void onDiscoverServices(BluetoothGattConnection connection) {
            if (mListener != null) {
                mListener.onDiscoverServices(connection);
            }
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

        /**
         * connect 処理を開始
         * OnConnectListener のコンストラクタで connect を呼び出し
         *
         * @param peripheral 接続先ペリフェラル
         * @param timeout    タイムアウト時間(msec)
         * @param success    接続成功時処理
         * @param failure    接続失敗時処理
         */
        void startConnect(BluetoothPeripheral peripheral, long timeout, OnSuccessFunction<Incredist> success, OnFailureFunction failure) {
            mListener = new OnConnectListener(peripheral, timeout, success, failure);
        }

        /**
         * disconnect 用のリスナを設定
         *
         * @param incredist 切断する incredist オブジェクト
         * @param success   切断成功時処理
         * @param failure   切断失敗時処理
         */
        void setupDisconnect(Incredist incredist, OnSuccessFunction<Incredist> success, OnFailureFunction failure) {
            mListener = new OnDisconnectListener(incredist, success, failure);
        }

        /**
         * リスナを解放
         */
        public void clearListener() {
            mListener = null;
        }

        /**
         * connect 開始待ち処理用リスナ
         * タイムアウト処理用の Runnable を実装している
         */
        class OnConnectListener implements BluetoothGattConnection.ConnectionListener, Runnable {
            /**
             * 接続先 Peripheral
             */
            private BluetoothPeripheral mPeripheral;

            /**
             * connect 処理のタイムアウト時間
             */
            private final long mTimeout;

            /**
             * 成功時コールバック
             */
            private OnSuccessFunction<Incredist> mSuccessFunction;

            /**
             * 失敗時コールバック
             */
            private OnFailureFunction mFailureFunction;

            /**
             * タイムアウト予定時刻
             */
            private long mTimeoutTime;

            /**
             * 接続成功フラグ
             */
            private boolean mHasSucceed = false;

            /**
             * タイムアウト時間経過フラグ
             */
            private boolean mHasTimeout = false;

            /**
             * discoverService 待ちフラグ
             */
            private boolean mHasDiscoverServices = false;

            /**
             * reconnect 呼び出しフラグ
             */
            private boolean mReconnect = false;

            /**
             * 接続用リスナのコンストラクタ
             * 接続処理も実行する
             *
             * @param peripheral 接続先ペリフェラル
             * @param timeout    タイムアウト時間(msec)
             * @param success    接続成功時処理
             * @param failure    接続失敗時処理
             */
            public OnConnectListener(BluetoothPeripheral peripheral, long timeout, OnSuccessFunction<Incredist> success, OnFailureFunction failure) {
                mPeripheral = peripheral;
                mTimeout = timeout;
                mSuccessFunction = success;
                mFailureFunction = failure;
                mHasSucceed = false;
                mHasDiscoverServices = false;
                mHasTimeout = false;
                mReconnect = false;

                FLog.d(TAG, "OnConnectListener constructor");

                final BluetoothGattConnection connection = mManager.mCentral.connect(peripheral, IncredistConnectionListener.this);
                if (timeout > 0) {
                    // タイムアウト処理を handler に登録
                    mConnection = connection;
                    mTimeoutTime = System.currentTimeMillis() + timeout;
                    mHandler.postDelayed(this, timeout / 5);
                }
            }

            /**
             * タイムアウト時処理.
             */
            @Override
            public void run() {
                synchronized (this) {
                    if (!mHasSucceed) {
                        if (mTimeoutTime < System.currentTimeMillis()) {
                            mHasTimeout = true;
                            mConnection.disconnect();
                            mConnection.close();
                            FLog.i(TAG, "OnConnectListener connect timeout");
                            if (mFailureFunction != null) {
                                mFailureFunction.onFailure(CONNECT_ERROR_TIMEOUT);
                            }
                            mSuccessFunction = null;
                            mFailureFunction = null;
                            mPeripheral = null;
                        } else {
                            int state = mConnection.getConnectionState();
                            FLog.i(TAG, String.format(Locale.JAPANESE, "OnConnectListener timeout check state:%d", state));
                            if (!mHasDiscoverServices && !mReconnect && System.currentTimeMillis() > mTimeoutTime - mTimeout / 2) {
                                if (state == BluetoothGatt.STATE_CONNECTED) {
                                    mConnection.discoverService();
                                } else {
                                    // タイムアウト時間の半分を経過してもdiscoverService 呼び出しまで到達しない場合
                                    mConnection.reconnect();
                                    mReconnect = true;
                                }
                            }

                            FLog.i(TAG, "OnConnectListener timeout handler restart");
                            // タイムアウト時刻に達していない場合 再postする (onDiscoverService で再postする処理との競合を考慮)
                            mHandler.removeCallbacks(this);
                            mHandler.postDelayed(this, mTimeout / 5);
                        }
                    }
                }
            }

            @Override
            public void onDiscoverServices(BluetoothGattConnection connection) {
                synchronized (this) {
                    FLog.d(TAG, "OnDiscoverServices called");
                    if (!mHasTimeout) {
                        mHasDiscoverServices = true;
                        // タイムアウトしていない場合は onDiscoverService が呼ばれたら一度 callback をキャンセルして再post
                        mTimeoutTime += mTimeout;
                        mHandler.removeCallbacks(this);
                        mHandler.postDelayed(this, mTimeout / 5);
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
                        final Incredist incredist = new Incredist(mManager, connection, mPeripheral.getDeviceName());
                        mHasSucceed = true;
                        mHandler.removeCallbacks(this);
                        mSuccessFunction.onSuccess(incredist);
                        mSuccessFunction = null;
                        mFailureFunction = null;
                        mPeripheral = null;
                    }

                    if (mHasTimeout) {
                        // タイムアウト通知後に onConnect が呼び出された場合切断する
                        connection.disconnect();
                    }
                }
            }

            @Override
            public void onDisconnect(BluetoothGattConnection connection) {
                FLog.i(TAG, "OnConnectListener disconnected");

                if (!mHasTimeout && !mHasSucceed && mConnection != null) {
                    mConnection.reconnect();
                }
            }
        }

        /**
         * setupDisconnect 時のリスナ
         */
        class OnDisconnectListener implements BluetoothGattConnection.ConnectionListener {
            private Incredist mIncredist;
            private OnSuccessFunction<Incredist> mSuccessFunction;
            private OnFailureFunction mFailureFunction;

            boolean mHasSucceed = false;

            public OnDisconnectListener(Incredist incredist, OnSuccessFunction<Incredist> success, OnFailureFunction failure) {
                mIncredist = incredist;
                mSuccessFunction = success;
                mFailureFunction = failure;
            }

            @Override
            public void onDiscoverServices(BluetoothGattConnection connection) {
                // 切断時は何もしない
            }

            @Override
            public void onConnect(BluetoothGattConnection connection) {
                FLog.i(TAG, "OnDisconnectListener connected");
                // 切断時は何もしない
            }

            @Override
            public void onDisconnect(BluetoothGattConnection connection) {
                FLog.i(TAG, "OnDisconnectListener disconnect succeed");
                if (mSuccessFunction != null) {
                    mSuccessFunction.onSuccess(mIncredist);
                }
                mIncredist = null;
                mSuccessFunction = null;
                mFailureFunction = null;
            }
        }
    }

    /**
     * Incredistデバイスに接続します.
     *
     * @param deviceName     Incredistデバイス名
     * @param scanTimeout    BLEスキャン実行時のタイムアウト時間(単位
     * @param connectTimeout 接続処理タイムアウト時間(単位 msec)
     * @param success        接続成功時処理
     * @param failure        接続失敗時処理
     */
    public void connect(@NonNull String deviceName, long scanTimeout, long connectTimeout, @Nullable OnSuccessFunction<Incredist> success, @Nullable OnFailureFunction failure) {
        FLog.i(TAG, String.format(Locale.JAPANESE, "connect device:%s scanTimeout:%d connectTimeout:%d", deviceName, scanTimeout, connectTimeout));

        // 接続中のペリフェラルの場合は直接 connectInternal を呼び出す
        List<BluetoothPeripheral> peripherals = mCentral.getConnectedPeripherals();
        FLog.i(TAG, String.format(Locale.JAPANESE, "connected peripherals: %d", peripherals.size()));
        for (BluetoothPeripheral peripheral : peripherals) {
            if (peripheral.getDeviceName().equals(deviceName)) {
                FLog.i(TAG, String.format("found connected %s", peripheral.getDeviceAddress()));
                connectInternal(peripheral, connectTimeout, success, failure);
                return;
            }
        }

        // デバイス名が一致したら停止するフィルタ
        DeviceFilter filter = new DeviceFilter() {
            @Override
            public boolean isValid(String devName) {
                boolean res = super.isValid(devName);
                if (res && deviceName.equals(devName)) {
                    mCentral.getHandler().post(() -> {
                        bleStopScan();
                    });
                }

                return res;
            }
        };

        // BLE スキャンを実行してデバイス名が一致したら接続する
        bleStartScanInternal(filter, scanTimeout, (peripheralMap) -> {
            BluetoothPeripheral peripheral = peripheralMap.get(deviceName);
            if (peripheral != null) {
                connectInternal(peripheral, connectTimeout, success, failure);
            } else {
                Handler handler = mCentral.getHandler();
                FLog.i(TAG, "connect device not found.");
                handler.post(() -> {
                    if (failure != null) {
                        failure.onFailure(CONNECT_ERROR_NOT_FOUND);
                    }
                });
            }
        }, failure);
    }

    private void connectInternal(BluetoothPeripheral peripheral, long timeout, @Nullable OnSuccessFunction<Incredist> success, @Nullable OnFailureFunction failure) {
        Handler handler = mCentral.getHandler();
        mConnectionListener = new IncredistConnectionListener(this, handler);
        mConnectionListener.startConnect(peripheral, timeout, success, failure);
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
     * Bluetooth Adapter を off -> on してリセットします
     *
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    public void restartAdapter(@Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        mCentral.restartAdapter((succ) -> {
            if (success != null) {
                success.onSuccess();
            }
        }, (errorCode, fail) -> {
            if (failure != null) {
                failure.onFailure(errorCode);
            }
        });
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
