package jp.co.flight.android.bluetooth.le;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Bluetooth デバイスとの接続クラス.
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
public class BluetoothGattConnection {
    @Nullable
    private BluetoothGatt mGatt;
    private final BluetoothCentral mCentral;

    @Nullable
    private ConnectionListener mListener;

    private long mTimeout = 1000;

    public interface ConnectionListener {
        void onConnect(BluetoothGattConnection connection);
        void onDisconnect(BluetoothGattConnection connection);
    }

    BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            ConnectionListener listener = mListener;
            if (listener != null) {
                switch (newState) {
                    case BluetoothGatt.STATE_CONNECTED:
                        listener.onConnect(BluetoothGattConnection.this);
                        break;
                    case BluetoothGatt.STATE_DISCONNECTED:
                        listener.onDisconnect(BluetoothGattConnection.this);
                        break;
                    default:
                        break;
                }
            }
        }
    };

    /* package */
    BluetoothGattConnection(BluetoothCentral central, BluetoothPeripheral peripheral, ConnectionListener listener) {
        mGatt = central.connectGatt(peripheral, mGattCallback);
        mCentral = central;
        mListener = listener;
    }

    public int getConnectionState() {
        if (mGatt == null) {
            return BluetoothGatt.STATE_DISCONNECTED;
        }
        return mCentral.getConnectionState(mGatt.getDevice());
    }

    @NonNull
    public List<BluetoothGattService> getServiceList() {
        if (mGatt != null) {
            return mGatt.getServices();
        }

        return new ArrayList<>();
    }

    /**
     * Bluetooth フレームワークとのタイムアウト時間を設定します.
     * @param timeout タイムアウト時間(単位 msec)
     */
    public void setTimeout(long timeout) {
        mTimeout = timeout;
    }

    /**
     * 指定した Characteristic へデータを書き込みます.
     *
     * @param characteristic 書き込み先 Characterstic
     * @param value 書き込みデータ
     * @param success 書き込み成功時処理
     * @param failure 書き込み失敗時処理
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value, OnSuccessFunction<Void> success, OnFailureFunction<Void> failure) {
        //TODO
    }

    /**
     * 指定した Characteristic からデータを読み込みます.
     *
     * @param characteristic 読み込み先 Characteristic
     * @param success 読み込み成功時処理
     * @param failure 読み込み失敗時処理
     */
    void readCharacteristic(BluetoothGattCharacteristic characteristic, OnSuccessFunction<BluetoothGattCharacteristic> success, OnFailureFunction<Void> failure) {
        //TODO
    }

    /**
     * BluetoothLE デバイスとの接続を切断します.
     */
    void disconnect() {
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
    }

}
