package jp.co.flight.android.bluetooth.le;

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

    public BluetoothPeripheral(String deviceName, String deviceAddress) {
        this.mDeviceName = deviceName;
        this.mDeviceAddress = deviceAddress;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }
}
