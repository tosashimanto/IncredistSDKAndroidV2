package jp.co.flight.android.bluetooth.le;

/**
 * Bluetooth Peripheral クラス.
 */

/*
 * TODO 本来は Advertising の汎用データを扱えるように拡張するのがよい
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
public class BluetoothPeripheral {
    public String deviceName;
    public String deviceAddress;

    public BluetoothPeripheral(String deviceName, String deviceAddress) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }
}
