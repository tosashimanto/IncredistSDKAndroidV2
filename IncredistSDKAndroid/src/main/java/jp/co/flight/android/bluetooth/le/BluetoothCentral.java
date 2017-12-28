package jp.co.flight.android.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.co.flight.incredist.android.internal.util.FLog;

/**
 * Bluetooth Central クラス.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BluetoothCentral {
    private static final String TAG = "BluetoothCentral";

    private static final int SCAN_ERROR_ALREADY_SCANNING = 801;
    private static final int SCAN_ERROR_CANT_START = 802;
    private static final int SCAN_ERROR_NO_PERMISSION = 803;

    private final WeakReference<Context> mContext;
    private final BluetoothManager mManager;

    @Nullable
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private BluetoothAdapter mAdapter;
    private BluetoothLeScanner mScanner;

    private OnSuccessFunction<Void> mScanSuccessFunction;
    private OnProgressFunction<BluetoothPeripheral> mScanResultFunction;
    private OnFailureFunction<Void> mScanFailureFunction;

    /**
     * Android BluetoothLeScanner のコールバック.
     */
    final android.bluetooth.le.ScanCallback mAndroidScanCallback = new android.bluetooth.le.ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            ScanRecord record = result.getScanRecord();
            FLog.d(TAG, String.format("onScanResult address:%s name:%s record:%s",
                    result.getDevice().getAddress(),
                    record != null ? record.getDeviceName() : "(record is null)",
                    record != null ? record.toString() : ""));

            if (record != null && record.getDeviceName() != null) {
                callScanResult(new BluetoothPeripheral(record.getDeviceName(), result.getDevice().getAddress()));
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            FLog.d(TAG, String.format(Locale.JAPANESE, "onScanFailed %d", errorCode));
            callScanFailure(errorCode);
            stopScan();
        }
    };

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param handler Service で使う場合などにコールバックの呼び出し元となる Handler
     */
    public BluetoothCentral(Context context, Handler handler) {
        mContext = new WeakReference<>(context);

        if (handler == null) {
            mHandlerThread = new HandlerThread(TAG) {
                @Override
                protected void onLooperPrepared() {
                    super.onLooperPrepared();
                    mHandler = new Handler(this.getLooper());
                }
            };

            mHandlerThread.start();
            mHandler = null;
        } else {
            mHandlerThread = null;
            mHandler = handler;
        }

        mManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mManager != null) {
            mAdapter = mManager.getAdapter();
        }
    }

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    public BluetoothCentral(Context context) {
        this(context, null);
    }

    /**
     * BluetoothLE のスキャンを一定時間実行します.
     *
     * @param time    スキャン時間(<= 0 の場合自動停止しない)
     * @param success スキャン成功時処理
     * @param failure スキャン失敗時処理
     * @param scan    スキャン中処理
     */
    public void startScan(long time, OnSuccessFunction<Void> success, OnFailureFunction<Void> failure, OnProgressFunction<BluetoothPeripheral> scan) {
        mScanSuccessFunction = success;
        mScanFailureFunction = failure;
        mScanResultFunction = scan;
        if (mScanner != null) {
            // すでにスキャン中の場合
            FLog.d(TAG, "bleStartScan already scanning");
            callScanFailure(SCAN_ERROR_ALREADY_SCANNING);
            return;
        }

        if (mAdapter != null) {
            mScanner = mAdapter.getBluetoothLeScanner();
        } else {
            FLog.d(TAG, "adapter not found");
            mScanner = null;
        }

        if (mScanner != null && (time <= 0 || mHandler != null)) {
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            try {
                FLog.d(TAG, "BluetoothLeScanner.bleStartScan");
                mScanner.startScan(null, settings, mAndroidScanCallback);
                if (time > 0) {
                    mHandler.postDelayed(this::stopScan, time);
                }
            } catch (IllegalStateException ex) {
                callScanFailure(SCAN_ERROR_CANT_START);
            } catch (SecurityException ex) {
                callScanFailure(SCAN_ERROR_NO_PERMISSION);
            }
        } else {
            FLog.d(TAG, "bleStartScan can't start");
            callScanFailure(SCAN_ERROR_CANT_START);
        }
    }

    /**
     * BluetoothLE のスキャンを停止します.
     */
    public void stopScan() {
        if (mScanner != null) {
            try {
                mScanner.stopScan(mAndroidScanCallback);
            } catch (IllegalStateException ex) {
                // ignore.
            }
        }

        mScanner = null;
        final OnSuccessFunction<Void> successHandler = mScanSuccessFunction;
        synchronized (this) {
            mScanSuccessFunction = null;
            mScanResultFunction = null;
            mScanFailureFunction = null;
        }

        if (mHandler != null && successHandler != null) {
            mHandler.post(() -> successHandler.onSuccess(null));
        }
    }

    /**
     * onScanResult を mHandler のスレッドで実行する.
     * 先に bleStopScan が呼び出されていた場合、onSuccess より後に onProgress が呼ばれることはない.
     *
     * @param peripheral スキャンで見つかった peripheral
     */
    private void callScanResult(@NonNull final BluetoothPeripheral peripheral) {
        if (mHandler != null && mScanResultFunction != null) {
            mHandler.post(() -> {
                OnProgressFunction<BluetoothPeripheral> progress;
                synchronized (BluetoothCentral.this) {
                    progress = mScanResultFunction;
                }
                if (progress != null) {
                    progress.onProgress(peripheral);
                }
            });
        }
    }

    /**
     * onScanFailure を mHandler のスレッドで実行.
     *
     * @param errorCode エラーコード値
     */
    private void callScanFailure(final int errorCode) {
        if (mHandler != null && mScanFailureFunction != null) {
            mHandler.post(() -> {
                OnFailureFunction<Void> failure;
                synchronized (BluetoothCentral.this) {
                    failure = mScanFailureFunction;
                    mScanSuccessFunction = null;
                    mScanResultFunction = null;
                    mScanFailureFunction = null;
                }
                if (failure != null) {
                    failure.onFailure(errorCode, null);
                }
            });
        }
    }

    /**
     * ペリフェラルに接続します.
     *
     * @param peripheral 接続先ペリフェラル
     * @param listener   接続状態のリスナ
     * @return ペリフェラルとの接続オブジェクト
     */
    @NonNull
    public BluetoothGattConnection connect(@NonNull BluetoothPeripheral peripheral, @Nullable BluetoothGattConnection.ConnectionListener listener) {
        return new BluetoothGattConnection(this, peripheral, listener);
    }

    /**
     * ペリフェラル接続処理の実体.
     * BluetoothAdapter に関する処理を Central にまとめるためにメソッド化している
     *
     * @param peripheral   接続先ペリフェラル
     * @param gattCallback Android framework のコールバック
     * @return BluetoothGatt オブジェクト
     */
    @Nullable
    BluetoothGatt connectGatt(@NonNull BluetoothPeripheral peripheral, @NonNull BluetoothGattCallback gattCallback) {
        Context context = mContext.get();
        if (context != null) {
            BluetoothDevice device = mAdapter.getRemoteDevice(peripheral.getDeviceAddress());
            if (device != null) {
                BluetoothGatt gatt = device.connectGatt(context, false, gattCallback);
                FLog.d(TAG, String.format(Locale.JAPANESE, "call BluetoothGatt#connectGatt for %x", System.identityHashCode(gatt)));

                return gatt;
            }
        }

        return null;
    }

    /**
     * 接続中だった場合に強制切断します
     *
     * @param peripheral 切断するペリフェラル
     */
    void disconnectGatt(@NonNull BluetoothPeripheral peripheral) {
        Context context = mContext.get();
        if (context != null) {
            BluetoothDevice device = mAdapter.getRemoteDevice(peripheral.getDeviceAddress());
            if (device != null) {
                BluetoothGatt gatt = device.connectGatt(context, false, null);
                gatt.disconnect();
            }
        }
    }

    /**
     * BluetoothCentral クラスの使用リソースを解放します.
     *
     * @return 解放に成功した場合 true, 失敗した場合 false.
     */
    public boolean release() {
        HandlerThread handlerThread = mHandlerThread;
        if (handlerThread != null) {
            if (handlerThread.quitSafely()) {
                mHandlerThread = null;
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * デバイスの接続状態を取得します.
     *
     * @param device デバイス
     * @return 接続状態
     */
    int getConnectionState(BluetoothDevice device) {
        if (device != null) {
            return mManager.getConnectionState(device, BluetoothProfile.GATT);
        } else {
            return BluetoothGatt.STATE_DISCONNECTED;
        }
    }

    /**
     * デバイスの接続状態を取得します.
     *
     * @param peripheral Bluetoothペリフェラル
     */
    int getConnectionState(BluetoothPeripheral peripheral) {
        BluetoothDevice device = mAdapter.getRemoteDevice(peripheral.getDeviceAddress());
        return getConnectionState(device);
    }

    /**
     * 接続中のデバイスの一覧を取得します
     *
     * @return BluetoothPeripheral のリスト
     */
    public @NonNull List<BluetoothPeripheral> getConnectedPeripherals() {
        List<BluetoothDevice> devices = mManager.getConnectedDevices(BluetoothGatt.GATT);

        List<BluetoothPeripheral> peripherals = new ArrayList<>();
        for (BluetoothDevice device : devices) {
            peripherals.add(new BluetoothPeripheral(device.getName(), device.getAddress()));
        }

        return peripherals;
    }

    /**
     * BluetoothCentral スレッドの Handler を取得します.
     * 取得できない場合、UIスレッドの Handler を返却します.
     *
     * @return Handler オブジェクト
     */
    @NonNull
    public Handler getHandler() {
        if (mHandler != null) {
            return mHandler;
        } else {
            return new Handler(Looper.getMainLooper());
        }
    }

    /**
     * Bluetooth Adapter を off -> on してリセットします
     *
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    public void restartAdapter(OnSuccessFunction<Void> success, OnFailureFunction<Void> failure) {
        Context context = mContext.get();

        if (context == null) {
            if (failure != null) {
                failure.onFailure(-1, null);
            }
            return;
        }
        BroadcastReceiver receiver = new BroadcastReceiver() {
            final String TAG = "BroarcastReceiver";
            boolean mDisabling = true;

            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_DISCONNECTED);
                FLog.i(TAG, String.format(Locale.JAPANESE, "onReceive action:%s state:%d", intent.getAction(), state));

                if (mDisabling && state == BluetoothAdapter.STATE_OFF) {
                    mDisabling = false;
                    mAdapter.enable();
                }

                if (state == BluetoothAdapter.STATE_ON) {
                    if (success != null) {
                        success.onSuccess(null);
                    }
                    context.unregisterReceiver(this);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(receiver, intentFilter);

        if (mAdapter.getState() == BluetoothAdapter.STATE_ON) {
            mAdapter.disable();
        } else if (mAdapter.getState() == BluetoothAdapter.STATE_OFF) {
            mAdapter.enable();
        }
    }

}
