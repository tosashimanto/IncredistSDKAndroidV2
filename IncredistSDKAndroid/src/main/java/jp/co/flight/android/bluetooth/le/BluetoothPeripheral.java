package jp.co.flight.android.bluetooth.le;

import android.bluetooth.le.ScanResult;

/**
 * Bluetooth Peripheral クラス.
 */

/*
 * TODO 本来は Advertising の汎用データを扱えるように拡張するのがよい
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BluetoothPeripheral {
    private final String mDeviceName;
    private final String mDeviceAddress;
    private final int mRssi;

    public BluetoothPeripheral(String deviceName, ScanResult result) {
        this.mDeviceName = deviceName;
        this.mDeviceAddress = result.getDevice().getAddress();
        this.mRssi = result.getRssi();
    }

    public BluetoothPeripheral(String deviceName, String deviceAddress) {
        this.mDeviceName = deviceName;
        this.mDeviceAddress = deviceAddress;
        this.mRssi = 0;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    public int getRssi() {
        return mRssi;
    }
}
