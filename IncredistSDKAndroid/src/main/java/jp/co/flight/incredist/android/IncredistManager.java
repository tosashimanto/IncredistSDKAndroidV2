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
import jp.co.flight.incredist.android.model.StatusCode;

/**
 * Incredist 検索と接続の管理クラス.
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // for public API.
public class IncredistManager {
    private static final String TAG = "IncredistManager";

    private final BluetoothCentral mCentral;

    InternalConnectionListenerV1 mConnectionListenerV1;
    private IncredistConnectionListener mListener = null;

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
     * Incredist 接続/切断時の通知用インタフェース
     */
    public interface IncredistConnectionListener {
        void onConnectIncredist(Incredist incredist);

        void onConnectFailure(int errorCode);

        void onDisconnectIncredist(Incredist incredist);
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
    public void bleStartScan(@Nullable DeviceFilter filter, long scanTime, OnSuccessFunction<List<BluetoothPeripheral>> success, OnFailureFunction failure) {
        bleStartScanInternal(filter, scanTime, (peripheralMap) -> {
            success.onSuccess(new ArrayList<>(peripheralMap.values()));
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
     * 旧バージョンの connect / disconnect メソッドで利用する内部リスナクラス.
     * 処理を内部クラスに移譲
     */
    @Deprecated
    private static class InternalConnectionListenerV1 implements BluetoothGattConnection.ConnectionListener {
        private final IncredistManager mManager;
        private final Handler mHandler;

        /**
         * 接続時と切断時でリスナ実体を切り替えるためのインスタンス変数
         */
        BluetoothGattConnection.ConnectionListener mListener;
        BluetoothGattConnection mConnection;

        InternalConnectionListenerV1(IncredistManager manager, Handler handler) {
            mManager = manager;
            mHandler = handler;
        }

        @Override
        public void onDiscoveringServices(BluetoothGattConnection connection) {
            if (mListener != null) {
                mListener.onDiscoveringServices(connection);
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

        @Override
        public void onStartDisconnect(BluetoothGattConnection connection) {
            // 何もしない
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

                final BluetoothGattConnection connection = mManager.mCentral.connect(peripheral, InternalConnectionListenerV1.this);
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
                                mFailureFunction.onFailure(StatusCode.CONNECT_ERROR_TIMEOUT);
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
            public void onDiscoveringServices(BluetoothGattConnection connection) {
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

            @Override
            public void onStartDisconnect(BluetoothGattConnection connection) {
                // 何もしない
            }
        }

        /**
         * setupDisconnectV1 時のリスナ
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
            public void onDiscoveringServices(BluetoothGattConnection connection) {
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

            @Override
            public void onStartDisconnect(BluetoothGattConnection connection) {
                // 何もしない
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
    @Deprecated
    public void connect(@NonNull String deviceName, long scanTimeout, long connectTimeout, @Nullable OnSuccessFunction<Incredist> success, @Nullable OnFailureFunction failure) {
        FLog.i(TAG, String.format(Locale.JAPANESE, "connect device:%s scanTimeout:%d connectTimeout:%d", deviceName, scanTimeout, connectTimeout));

        // 接続中のペリフェラルの場合は直接 connectInternalV1 を呼び出す
        List<BluetoothPeripheral> peripherals = mCentral.getConnectedPeripherals();
        FLog.i(TAG, String.format(Locale.JAPANESE, "connected peripherals: %d", peripherals.size()));
        for (BluetoothPeripheral peripheral : peripherals) {
            if (peripheral.getDeviceName() != null && peripheral.getDeviceName().equals(deviceName)) {
                FLog.i(TAG, String.format("found connected %s", peripheral.getDeviceAddress()));
                connectInternalV1(peripheral, connectTimeout, success, failure);
                return;
            } else {
                FLog.i(TAG, String.format("found connected another device %s", peripheral.getDeviceAddress()));
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
                connectInternalV1(peripheral, connectTimeout, success, failure);
            } else {
                Handler handler = mCentral.getHandler();
                FLog.i(TAG, "connect device not found.");
                handler.post(() -> {
                    if (failure != null) {
                        failure.onFailure(StatusCode.CONNECT_ERROR_NOT_FOUND);
                    }
                });
            }
        }, failure);
    }

    private void connectInternalV1(BluetoothPeripheral peripheral, long timeout, @Nullable OnSuccessFunction<Incredist> success, @Nullable OnFailureFunction failure) {
        Handler handler = mCentral.getHandler();
        mConnectionListenerV1 = new InternalConnectionListenerV1(this, handler);
        mConnectionListenerV1.startConnect(peripheral, timeout, success, failure);
    }

    enum State {
        CONNECTING, WAIT, DISCOVERING, REDISCOVERING, CONNECTED, DISCONNECTING, DISCONNECTED
    }

    private class InternalConnectionListener implements BluetoothGattConnection.ConnectionListener {
        private final String TAG = "InternalConnectionListener";
        private State mState = State.CONNECTING;

        private Handler mHandler;

        private Incredist mIncredist;
        private BluetoothPeripheral mPeripheral;
        private BluetoothGattConnection mConnection = null;
        private int mRunnableCount;
        private int mPreviousStateCount = -1;
        private long mTimeoutTime;
        private long mInterval;

        private Runnable mConnectingRunnable = new Runnable() {

            @Override
            public void run() {
                mRunnableCount++;
                FLog.d(TAG, String.format(Locale.JAPANESE, "runnable called %d", mRunnableCount));
                switch (mState) {
                    case CONNECTING:
                    case REDISCOVERING:
                    case CONNECTED:
                        break;
                    case DISCOVERING:
                        if (mRunnableCount - mPreviousStateCount > 0) {
                            mConnection.discoverService();
                            updateState(State.REDISCOVERING);
                        }
                        break;
                    case DISCONNECTING:
                    case DISCONNECTED:
                        // 切断処理が開始された場合は runnable は停止する
                        return;
                }

                if (mState != State.CONNECTED) {
                    if (System.currentTimeMillis() < mTimeoutTime) {
                        // タイムアウト時刻に達していない場合 再postする (onDiscoverService で再postする処理との競合を考慮)
                        mHandler.removeCallbacks(this);
                        mHandler.postDelayed(this, mInterval);
                    } else {
                        // タイムアウトした場合
                        callOnConnectionFailure(StatusCode.CONNECT_ERROR_TIMEOUT);
                    }
                }
            }
        };

        InternalConnectionListener(Handler handler) {
            mHandler = handler;
        }

        private void updateState(State newState) {
            FLog.d(TAG, String.format(Locale.US, "updateState %s <- %s at %d", newState, mState, mRunnableCount));
            mState = newState;
            mPreviousStateCount = mRunnableCount;
        }

        @Override
        public void onDiscoveringServices(BluetoothGattConnection connection) {
            FLog.d(TAG, "onDiscoveringServices called");
            if (mState != State.REDISCOVERING) {
                updateState(State.DISCOVERING);
            }

            // タイムアウトしていない場合は onDiscoveringService が呼ばれたら一度 callback をキャンセルして再post
            if (System.currentTimeMillis() < mTimeoutTime) {
                if (mState != State.CONNECTED) {
                    // タイムアウト時刻に達していない場合 再postする (onDiscoverService で再postする処理との競合を考慮)
                    mHandler.removeCallbacks(mConnectingRunnable);
                    mHandler.postDelayed(mConnectingRunnable, mInterval);
                }
            }

        }

        @Override
        public void onConnect(BluetoothGattConnection connection) {
            FLog.d(TAG, "onConnect called");
            if (mState != State.CONNECTED) {
                updateState(State.CONNECTED);
                callOnConnected();
            }
        }

        @Override
        public void onDisconnect(BluetoothGattConnection connection) {
            FLog.d(TAG, "onDisconnect called");
            if (mState != State.DISCONNECTED) {
                if (mState != State.CONNECTED && mState != State.DISCONNECTING) {
                    // 接続処理中に切断された場合、再接続する
                    mConnection.reconnect();
                    updateState(State.CONNECTING);
                } else {
                    // 接続完了後または切断処理中
                    callOnDisconnected();
                    updateState(State.DISCONNECTED);
                }
            }
        }

        @Override
        public void onStartDisconnect(BluetoothGattConnection connection) {
            FLog.d(TAG, "onStartDisconnect called");
            updateState(State.DISCONNECTING);
        }

        void startConnect(BluetoothPeripheral peripheral, long timeout) {
            mPeripheral = peripheral;

            // すでに接続済みかどうかをチェック
            if (mConnection != null && mConnection.getConnectionState() == BluetoothGatt.STATE_CONNECTED) {
                callOnConnected();
            } else {
                mConnection = IncredistManager.this.mCentral.connect(peripheral, this);
                // 未接続の場合 接続処理を実行
                updateState(State.CONNECTING);

                if (timeout > 0) {
                    // タイムアウト処理を handler に登録
                    mTimeoutTime = System.currentTimeMillis() + timeout;
                    mRunnableCount = 0;
                    mInterval = timeout / 5;
                    mHandler.postDelayed(mConnectingRunnable, mInterval);
                }
            }
        }

        private void callOnConnected() {
            FLog.d(TAG, "call onConnected");
            mIncredist = new Incredist(IncredistManager.this, mConnection, mPeripheral.getDeviceName());
            if (mListener != null) {
                mListener.onConnectIncredist(mIncredist);
            }
        }

        private void callOnConnectionFailure(int errorCode) {
            FLog.d(TAG, "call onConnectionFailure");
            if (mConnection.getConnectionState() != BluetoothGatt.STATE_DISCONNECTED) {
                mConnection.disconnect();
            }

            if (mListener != null) {
                mListener.onConnectFailure(errorCode);
            }
            mIncredist = null;
        }

        private void callOnDisconnected() {
            FLog.d(TAG, "call onDisconnected");
            if (mListener != null && mIncredist != null) {
                mListener.onDisconnectIncredist(mIncredist);
            }
            mIncredist = null;
        }
    }

    /**
     * Incredistデバイスに接続します.
     *
     * @param deviceName     Incredistデバイス名
     * @param scanTimeout    BLEスキャン実行時のタイムアウト時間(単位
     * @param connectTimeout 接続処理タイムアウト時間(単位 msec)
     * @param listener       接続成功/失敗時処理
     */
    public void connect(@NonNull String deviceName, long scanTimeout, long connectTimeout, @Nullable IncredistConnectionListener listener) {
        mListener = listener;
        FLog.i(TAG, String.format(Locale.JAPANESE, "connect device:%s scanTimeout:%d connectTimeout:%d", deviceName, scanTimeout, connectTimeout));

        // 接続中のペリフェラルの場合は直接 connectInternal を呼び出す
        List<BluetoothPeripheral> peripherals = mCentral.getConnectedPeripherals();
        FLog.i(TAG, String.format(Locale.JAPANESE, "connected peripherals: %d", peripherals.size()));
        for (BluetoothPeripheral peripheral : peripherals) {
            if (peripheral.getDeviceName() != null && peripheral.getDeviceName().equals(deviceName)) {
                FLog.i(TAG, String.format("found connected %s", peripheral.getDeviceAddress()));
                connectInternal(peripheral, connectTimeout);
                return;
            } else {
                FLog.i(TAG, String.format("found connected another device %s", peripheral.getDeviceAddress()));
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
                connectInternal(peripheral, connectTimeout);
            } else {
                FLog.i(TAG, "connect device not found.");
            }
        }, (errorCode) -> {
            Handler handler = mCentral.getHandler();
            if (mListener != null) {
                handler.post(() -> {
                    mListener.onConnectFailure(errorCode);
                });
            }
        });
    }

    private void connectInternal(BluetoothPeripheral peripheral, long timeout) {
        Handler handler = mCentral.getHandler();
        InternalConnectionListener internalListener = new InternalConnectionListener(handler);
        internalListener.startConnect(peripheral, timeout);
    }

    public void connectToAddress(String address, long timeout) {
        BluetoothPeripheral peripheral = new BluetoothPeripheral("add", address);
        connectInternal(peripheral, timeout);
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
        mListener = null;
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
    @Deprecated
    void setupDisconnectV1(Incredist incredist, OnSuccessFunction<Incredist> success, OnFailureFunction failure) {
        mConnectionListenerV1.setupDisconnect(incredist, success, failure);
    }

    /**
     * ConnectionListener の設定をクリアします
     */
    void resetConnectionListener() {
        mConnectionListenerV1.clearListener();
    }
}
