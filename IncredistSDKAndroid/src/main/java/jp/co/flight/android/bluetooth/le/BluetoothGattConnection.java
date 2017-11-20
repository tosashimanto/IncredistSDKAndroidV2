package jp.co.flight.android.bluetooth.le;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jp.co.flight.incredist.android.internal.util.FLog;

/**
 * Bluetooth デバイスとの接続クラス.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BluetoothGattConnection {
    private static final String TAG = "BluetoothGattConnection";

    public static final int ERROR_REGISTER_NOTIFICATION = 797;
    public static final int ERROR_REGISTER_NOTIFICATION_TIMEOUT = 798;
    public static final int ERROR_REGISTER_NOTIFICATION_INTERRUPTED = 799;
    public static final int ERROR_WRITE_FAILED = 796;
    public static final int ERROR_NO_CHARACTERISTIC = 990;

    @NonNull
    private final HandlerThread mHandlerThread;

    @Nullable
    private Handler mHandler;

    @Nullable
    private BluetoothGatt mGatt;

    @NonNull
    private final BluetoothCentral mCentral;

    @Nullable
    private ConnectionListener mListener;

    private long mTimeout = 1000;  // SUPPRESS CHECKSTYLE MagicNumber

    /**
     * 接続状態が変化した時のリスナ.
     */
    public interface ConnectionListener {
        /**
         * 接続時に呼び出されます.
         *
         * @param connection 接続オブジェクト
         */
        void onConnect(BluetoothGattConnection connection);

        /**
         * 切断時に呼び出されます.
         *
         * @param connection 接続オブジェクト
         */
        void onDisconnect(BluetoothGattConnection connection);
    }

    /**
     * Android BluetoothGattCallback にコールバック用のメンバ変数を追加したサブクラス.
     */
    class ConnectionGattCallback extends BluetoothGattCallback {
        /**
         * callback function for write succeed.
         */
        protected OnSuccessFunction<Void> mWriteSuccessFunction;

        /**
         * callback function for write failed.
         */
        protected OnFailureFunction<Void> mWriteFailureFunction;

        /**
         * callback function for notify.
         */
        protected OnSuccessFunction<CharacteristicValue> mNotifyFunction;

        /**
         * callback function for writeDescriptor.
         */
        protected OnSuccessFunction<Void> mWriteDescriptorSuccessFunction;

        /**
         * callback function for writeDescriptor failed.
         */
        protected OnFailureFunction<Void> mWriteDescriptorFailureFunction;
    }

    /**
     * Android BluetoothGattCallback のインスタンス.
     */
    final ConnectionGattCallback mGattCallback = new ConnectionGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            FLog.d(TAG, String.format(Locale.JAPANESE, "onConnectionStateChange: from %d status %d", status, newState));

            final ConnectionListener listener = mListener;
            if (listener != null) {
                switch (newState) {
                    case BluetoothGatt.STATE_CONNECTED:
                        post(() -> {
                            listener.onConnect(BluetoothGattConnection.this);
                            gatt.discoverServices();
                        });
                        break;
                    case BluetoothGatt.STATE_DISCONNECTED:
                        post(() -> {
                            listener.onDisconnect(BluetoothGattConnection.this);
                        });
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            FLog.d(TAG, String.format(Locale.JAPANESE, "onCharacteristicWrite: %s status %d", characteristic.getUuid().toString(), status));
            synchronized (this) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (mWriteSuccessFunction != null) {
                        final OnSuccessFunction<Void> successFunction = mWriteSuccessFunction;
                        post(() -> successFunction.onSuccess(null));
                    }
                } else {
                    if (mWriteFailureFunction != null) {
                        final OnFailureFunction<Void> failureFunction = mWriteFailureFunction;
                        post(() -> failureFunction.onFailure(status, null));
                    }
                }
                mWriteSuccessFunction = null;
                mWriteFailureFunction = null;
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            FLog.d(TAG, String.format(Locale.JAPANESE, "onCharacteristicChanged: %s length:%d", characteristic.getUuid().toString(), characteristic.getValue().length));
            synchronized (this) {
                if (mNotifyFunction != null) {
                    CharacteristicValue characteristicValue = new CharacteristicValue(characteristic.getUuid(), characteristic.getValue());
                    post(() -> {
                        if (mNotifyFunction != null) {
                            mNotifyFunction.onSuccess(characteristicValue);
                        }
                    });
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            FLog.d(TAG, String.format(Locale.JAPANESE, "onDescriptorWrite: %s %d", descriptor.getUuid().toString(), status));
            synchronized (this) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (mWriteDescriptorSuccessFunction != null) {
                        final OnSuccessFunction<Void> successFunction = mWriteDescriptorSuccessFunction;
                        post(() -> successFunction.onSuccess(null));
                    }
                } else {
                    if (mWriteDescriptorFailureFunction != null) {
                        final OnFailureFunction<Void> failureFunction = mWriteDescriptorFailureFunction;
                        post(() -> failureFunction.onFailure(status, null));
                    }
                }
                mWriteDescriptorSuccessFunction = null;
                mWriteDescriptorFailureFunction = null;
            }
        }
    };

    /**
     * コンストラクタ.
     *
     * @param central    BluetoothCentral オブジェクト
     * @param peripheral 接続先ペリフェラル
     * @param listener   接続状態リスナ
     */
    BluetoothGattConnection(@NonNull BluetoothCentral central, @NonNull BluetoothPeripheral peripheral, @Nullable ConnectionListener listener) {
        mGatt = central.connectGatt(peripheral, mGattCallback);
        mCentral = central;
        mListener = listener;
        mHandlerThread = new HandlerThread(String.format(Locale.JAPANESE, "%s:%s:%s", TAG, peripheral.getDeviceName(), peripheral.getDeviceAddress())) {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                mHandler = new Handler(getLooper());
            }
        };
        mHandlerThread.start();
    }

    /**
     * BluetoothGatt レベルで接続中かどうかを返却します.
     *
     * @return 接続中: STATE_CONNECTED, 切断中: STATE_DISCONNECTED
     */
    public int getConnectionState() {
        if (mGatt == null) {
            return BluetoothGatt.STATE_DISCONNECTED;
        }
        return mCentral.getConnectionState(mGatt.getDevice());
    }

    /**
     * 接続中のデバイスが提供する BluetoothGattService のリストを取得します.
     *
     * @return BluetoothGattService のリスト
     */
    @NonNull
    public List<BluetoothGattService> getServiceList() {
        if (mGatt != null) {
            return mGatt.getServices();
        }

        return new ArrayList<>();
    }

    /**
     * 指定された UUID の BluetoothGattService を取得します.
     *
     * @param serviceUuid GATTサービスの UUID
     * @return BluetoothGattService, 見つからない場合 null
     */
    @Nullable
    public BluetoothGattService findService(String serviceUuid) {
        if (mGatt != null) {
            return mGatt.getService(UUID.fromString(serviceUuid));
        }

        return null;
    }

    /**
     * Bluetooth フレームワークとのタイムアウト時間を設定します.
     *
     * @param timeout タイムアウト時間(単位 msec)
     */
    public void setTimeout(long timeout) {
        mTimeout = timeout;
    }

    /**
     * 指定した Characteristic へデータを書き込みます.
     *
     * @param characteristic 書き込み先 Characteristic
     * @param value          書き込みデータ
     * @param success        書き込み成功時処理
     * @param failure        書き込み失敗時処理
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value, OnSuccessFunction<Void> success, OnFailureFunction<Void> failure) {
        if (mGatt != null) {
            if (characteristic == null) {
                post(() -> failure.onFailure(ERROR_NO_CHARACTERISTIC, null));
                return;
            }

            synchronized (mGattCallback) {
                FLog.d(TAG, String.format(Locale.JAPANESE, "writeCharacteristic: %s length %d", characteristic.getUuid().toString(), value.length));

                characteristic.setValue(value);
                if (mGatt.writeCharacteristic(characteristic)) {
                    FLog.v(TAG, "writeCharacteristic succeed");
                    mGattCallback.mWriteSuccessFunction = success;
                    mGattCallback.mWriteFailureFunction = failure;
                } else {
                    FLog.w(TAG, "writeCharacteristic failed");
                    post(() -> failure.onFailure(ERROR_WRITE_FAILED, null));
                }
            }
        }
    }

    /**
     * 指定した Characteristic からの通知受信を設定します.
     *
     * @param characteristic 通知元 Characteristic
     * @param success        通知登録成功時処理
     * @param failure        通知登録失敗時処理
     * @param notify         通知受信時処理
     */
    public void registerNotify(BluetoothGattCharacteristic characteristic, OnSuccessFunction<Void> success, OnFailureFunction<Void> failure, OnSuccessFunction<CharacteristicValue> notify) {
        if (mGatt != null) {
            FLog.d(TAG, String.format(Locale.JAPANESE, "registerNotifyCharacteristic: %s", characteristic != null ? characteristic.getUuid().toString() : "(null)"));

            if (characteristic == null) {
                if (failure != null) {
                    post(() -> failure.onFailure(ERROR_NO_CHARACTERISTIC, null));
                }
                return;
            }

            synchronized (mGattCallback) {
                if (mGatt.setCharacteristicNotification(characteristic, true)) {
                    mGattCallback.mNotifyFunction = notify;

                    List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                    for (BluetoothGattDescriptor descriptor : descriptors) {
                        final CountDownLatch latch = new CountDownLatch(1);
                        final Boolean[] successFlag = new Boolean[1];

                        mGattCallback.mWriteDescriptorSuccessFunction = (successDescriptor) -> {
                            successFlag[0] = true;
                            latch.countDown();
                        };
                        mGattCallback.mWriteDescriptorFailureFunction = (errorCode, failureDescriptor) -> {
                            successFlag[0] = false;
                            latch.countDown();
                        };

                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        if (mGatt.writeDescriptor(descriptor)) {
                            FLog.v(TAG, String.format(Locale.JAPANESE, "writeDescriptor %s succeed", descriptor.getUuid().toString()));
                        } else {
                            FLog.w(TAG, String.format(Locale.JAPANESE, "writeDescriptor %s failed", descriptor.getUuid().toString()));
                        }

                        try {
                            if (latch.await(mTimeout, TimeUnit.MILLISECONDS)) {
                                if (!successFlag[0]) {
                                    post(() -> failure.onFailure(ERROR_REGISTER_NOTIFICATION, null));
                                    return;
                                }
                            } else {
                                // timeout
                                post(() -> failure.onFailure(ERROR_REGISTER_NOTIFICATION_TIMEOUT, null));
                                return;
                            }
                        } catch (InterruptedException e) {
                            // ignore.
                            post(() -> failure.onFailure(ERROR_REGISTER_NOTIFICATION_INTERRUPTED, null));
                            return;
                        }
                    }

                    FLog.v(TAG, "registerNotify success");
                    //登録成功処理
                    if (success != null) {
                        post(() -> success.onSuccess(null));
                    }
                } else {
                    //登録失敗
                    FLog.w(TAG, "registerNotify failed");
                    if (failure != null) {
                        post(() -> failure.onFailure(ERROR_REGISTER_NOTIFICATION, null));
                    }
                }
            }
        }
    }

    /**
     * BluetoothLE デバイスとの接続を切断します.
     */
    public void disconnect() {
        if (mGatt != null) {
            mGatt.disconnect();
        }
    }

    /**
     * BluetoothLE デバイスと再接続します.
     */
    public void reconnect() {
        if (mGatt != null) {
            mGatt.connect();
        }
    }

    /**
     * BluetoothLE デバイスを close します.
     */
    public void close() {
        mListener = null;

        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }

        mHandlerThread.quitSafely();
    }

    /**
     * HandlerThread もしくは準備ができていない場合は MainLooper で Runnable を実行します.
     *
     * @param r 実行する処理.
     */
    private void post(Runnable r) {
        Handler handler = mHandler;
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }

        handler.post(r);
    }

}
