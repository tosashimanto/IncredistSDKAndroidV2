package jp.co.flight.android.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

import jp.co.flight.incredist.android.internal.util.FLog;

/**
 * Bluetooth Central クラス.
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
public class BluetoothCentral {
    private static final String TAG = "BluetoothCentral";

    private static final int SCAN_ERROR_ALREADY_SCANNING = 801;
    private static final int SCAN_ERROR_CANT_START = 802;

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
    android.bluetooth.le.ScanCallback mAndroidScanCallback = new android.bluetooth.le.ScanCallback() {
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
            //TODO
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

    public BluetoothCentral(Context context) {
        this(context, null);
    }

    /**
     * BluetoothLE のスキャンを一定時間実行します.
     *
     * @param time スキャン時間(<= 0 の場合自動停止しない)
     * @param success スキャン成功時処理
     * @param failure スキャン失敗時処理
     * @param scan スキャン中処理
     */
    public void startScan(long time, OnSuccessFunction<Void> success, OnFailureFunction<Void> failure, OnProgressFunction<BluetoothPeripheral> scan) {
        mScanSuccessFunction = success;
        mScanFailureFunction = failure;
        mScanResultFunction = scan;
        if (mScanner != null) {
            // すでにスキャン中の場合
            callScanFailure(SCAN_ERROR_ALREADY_SCANNING);
            return;
        }

        if (mAdapter != null) {
            mScanner = mAdapter.getBluetoothLeScanner();
        } else {
            mScanner = null;
        }

        if (mScanner != null && (time <= 0 || mHandler != null)) {
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            mScanner.startScan(null, settings, mAndroidScanCallback);

            if (time > 0) {
                mHandler.postDelayed(this::stopScan, time);
            }
        } else {
            callScanFailure(SCAN_ERROR_CANT_START);
        }
    }

    /**
     * BluetoothLE のスキャンを停止します.
     */
    public void stopScan() {
        if (mScanner != null) {
            mScanner.stopScan(mAndroidScanCallback);
        }

        mScanner = null;
        final OnSuccessFunction<Void> successHandler = mScanSuccessFunction;
        synchronized (this) {
            mScanSuccessFunction = null;
            mScanResultFunction = null;
            mScanFailureFunction = null;
        }

        if (mHandler != null && successHandler != null) {
            mHandler.post(()->successHandler.onSuccess(null));
        }
    }

    /**
     * onScanResult を mHandler のスレッドで実行する.
     * 先に stopScan が呼び出されていた場合、onSuccess より後に onProgress が呼ばれることはない.
     *
     * @param peripheral スキャンで見つかった peripheral
     */
    private void callScanResult(@NonNull final BluetoothPeripheral peripheral) {
        if (mHandler != null && mScanResultFunction != null) {
            mHandler.post(()-> {
                OnProgressFunction<BluetoothPeripheral> handler;
                synchronized (BluetoothCentral.this) {
                    handler = mScanResultFunction;
                }
                if (handler != null) {
                    handler.onProgress(peripheral);
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
            mHandler.post(()-> {
                OnFailureFunction<Void> handler;
                synchronized (BluetoothCentral.this) {
                    handler = mScanFailureFunction;
                }
                if (handler != null) {
                    handler.onFailure(errorCode, null);
                }
            });
        }
    }

    /**
     * ペリフェラルに接続します.
     *
     * @param peripheral 接続先ペリフェラル
     * @param listener 接続状態のリスナ
     * @return ペリフェラルとの接続オブジェクト
     */
    public BluetoothGattConnection connect(BluetoothPeripheral peripheral, BluetoothGattConnection.ConnectionListener listener) {
        return new BluetoothGattConnection(this, peripheral, listener);
    }

    /**
     * ペリフェラル接続処理の実体.
     * BluetoothAdapter に関する処理を Central にまとめるためにメソッド化している
     *
     * @param peripheral 接続先ペリフェラル
     * @param gattCallback Android framework のコールバック
     * @return BluetoothGatt オブジェクト
     */
    /* package */
    @Nullable
    BluetoothGatt connectGatt(BluetoothPeripheral peripheral, BluetoothGattCallback gattCallback) {
        Context context = mContext.get();
        if (context != null) {
            BluetoothDevice device = mAdapter.getRemoteDevice(peripheral.deviceAddress);
            if (device != null) {
                return device.connectGatt(context, false, gattCallback);
            }
        }

        return null;
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
    /*package*/
    int getConnectionState(BluetoothDevice device) {
        return mManager.getConnectionState(device, BluetoothDevice.DEVICE_TYPE_LE);
    }

    /**
     * BluetoothCentral スレッドの Handler を取得します.
     *
     * @return Handler オブジェクト
     */
    @Nullable
    public Handler getHandler() {
        return mHandler;
    }
}
