package jp.co.flight.incredist.android;

import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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

    public static final int INCREDIST_PREMIUM_VENDOR_ID = 0x2925;
    public static final int INCREDIST_PREMIUM_PRODUCT_ID = 0x7008;

    private BluetoothCentral mCentral;
    private UsbManager mUsbManager;
    private final Context mAppContext;

    private IncredistConnectionListener mListener = null;

    private Map<IncredistDevice, Incredist> mConnectedDevices = new HashMap<>();

    private UsbDeviceConnection mUsbDeviceConnection;

    // USB デバイス切断時のレシーバ
    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                IncredistDevice incredistDevice = IncredistDevice.usbDevice(device);
                Incredist incredist = mConnectedDevices.get(incredistDevice);
                if (incredist != null) {
                    incredist.notifyDisconnect();
                    incredist.release();
                    mConnectedDevices.remove(incredistDevice);
                }
            }
        }
    };
    private boolean mUsbReceiverRegistered = false;

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
        mAppContext = context.getApplicationContext();
    }

    public void createBluetoothCentral() {
        if (mCentral == null) {
            mCentral = new BluetoothCentral(mAppContext);
        }
    }

    public void createUsbManager() {
        if (mUsbManager == null) {
            mUsbManager = (UsbManager) mAppContext.getSystemService(Context.USB_SERVICE);
        }

        // ANDROID_SDK_DEV-40
        if (!mUsbReceiverRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

            mAppContext.registerReceiver(mUsbReceiver, intentFilter);
            mUsbReceiverRegistered = true;
        }
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

        createBluetoothCentral();

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
        if (mCentral != null) {
            mCentral.stopScan();
        }
    }

    /**
     * USB 接続の Incredist を検索して返却します。
     *
     * 現状は、アプリケーションから単にこのメソッドを呼んだ場合は null が返却されるので、
     * AndroidManifest に device_filter.xml を記述して Activity を起動させて UsbDevice を
     * 取得させる必要がありそうです。ただ、一度取得した後ならば、本メソッドで再取得することは
     * 可能であると考えられます(OS 仕様のため曖昧な記載となっていますが今後調査します)。
     *
     * @return UsbDevice オブジェクト
     */
    @Nullable
    public UsbDevice findUsbIncredist() {
        createUsbManager();

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        for (Map.Entry<String, UsbDevice> deviceEntry : deviceList.entrySet()) {
            UsbDevice device = deviceEntry.getValue();
            if (device.getVendorId() == INCREDIST_PREMIUM_VENDOR_ID && device.getProductId() == INCREDIST_PREMIUM_PRODUCT_ID) {
                return device;
            }
        }

        return null;
    }

    enum State {
        CONNECTING,         // 接続開始
        DISCOVERING,        // discoverService 開始
        REDISCOVERING,      // discoverService の応答がない場合に再実行
        SEND_S_COMMAND,     // sコマンド送信
        CONNECTED,          // 接続完了
        DISCONNECTING,      // 切断開始
        DISCONNECTED        // 切断完了
    }

    private class InternalBleConnectionListener implements BluetoothGattConnection.ConnectionListener {
        private final String TAG = "InternalBleConnectionListener";
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

        InternalBleConnectionListener(Handler handler) {
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
                afterOnConnect();
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
                afterOnConnect();
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

        private void afterOnConnect() {
            // 接続確立後の処理
            mIncredist = new Incredist(IncredistManager.this, mConnection, mPeripheral.getDeviceName());
            IncredistDevice incredistDevice = IncredistDevice.bleDevice(mPeripheral.getDeviceName());
            mConnectedDevices.put(incredistDevice, mIncredist);

            // まず sコマンドを送信する
            mIncredist.stop(() -> {
                callOnConnected();
            }, (errorCode) -> {
                callOnConnectionFailure(errorCode);
            });
        }

        private void callOnConnected() {
            FLog.d(TAG, "call onConnected");
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

                mConnectedDevices.remove(IncredistDevice.bleDevice(mIncredist.getDeviceName()));
            } else {
                FLog.w(TAG, String.format("don't call onDisconnected, mListener=%x mIncredist=%x", System.identityHashCode(mListener), System.identityHashCode(mIncredist)));
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

        createBluetoothCentral();

        if (!mCentral.isBluetoothEnabled()) {
            Handler handler = mCentral.getHandler();
            if (mListener != null) {
                handler.post(() -> {
                    mListener.onConnectFailure(StatusCode.CONNECT_ERROR_NOT_FOUND);
                });
            }

            return;
        }

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
                Handler handler = mCentral.getHandler();
                if (mListener != null) {
                    handler.post(() -> {
                        mListener.onConnectFailure(StatusCode.CONNECT_ERROR_NOT_FOUND);
                    });
                }
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
        InternalBleConnectionListener internalListener = new InternalBleConnectionListener(handler);
        internalListener.startConnect(peripheral, timeout);
    }

    public void connectToAddress(String address, long timeout) {
        BluetoothPeripheral peripheral = new BluetoothPeripheral("add", address);
        connectInternal(peripheral, timeout);
    }

    /**
     * USB デバイスへ接続します
     *
     * @param device   USBデバイスオブジェクト
     * @param listener リスナ
     */
    public void connect(UsbDevice device, @Nullable IncredistConnectionListener listener) {

        Handler handler = new Handler(Looper.getMainLooper());
        if (device != null) {
            createUsbManager();
            UsbDeviceConnection connection = mUsbManager.openDevice(device);

            if (connection != null) {
                UsbInterface usbInterface = device.getInterface(0);

                handler.post(() -> {
                    Incredist incredist = new Incredist(this, connection, usbInterface, listener);
                    mConnectedDevices.put(IncredistDevice.usbDevice(device), incredist);
                    listener.onConnectIncredist(incredist);
                });
            } else {
                handler.post(() -> {
                    listener.onConnectFailure(StatusCode.CONNECT_ERROR_NOT_FOUND);
                });
            }

        } else {
            handler.post(() -> {
                listener.onConnectFailure(StatusCode.CONNECT_ERROR_NOT_FOUND);
            });
        }
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
        if (mCentral != null) {
            mCentral.release();
            mCentral = null;
        }

        if (mUsbReceiverRegistered) {
            mAppContext.unregisterReceiver(mUsbReceiver);
            mUsbReceiverRegistered = false;
        }
        mListener = null;
    }

    /**
     * Bluetooth Adapter を off -> on してリセットします
     *
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    public void restartAdapter(@Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        if (mCentral != null) {
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
    }

}
