package jp.co.flight.incredist.android;

import android.hardware.usb.UsbDevice;

import java.util.Objects;

public class IncredistDevice {
    public enum ConnectionType {
        Ble,
        Usb
    }

    private final ConnectionType mConnectionType;
    private final String mBluetoothDeviceName;
    private final UsbDevice mUsbDevice;

    private IncredistDevice(String deviceName) {
        mConnectionType = ConnectionType.Ble;
        mBluetoothDeviceName = deviceName;
        mUsbDevice = null;
    }

    private IncredistDevice(UsbDevice usbDevice) {
        mConnectionType = ConnectionType.Usb;
        mUsbDevice = usbDevice;
        mBluetoothDeviceName = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncredistDevice that = (IncredistDevice) o;
        return mConnectionType == that.mConnectionType &&
                Objects.equals(mBluetoothDeviceName, that.mBluetoothDeviceName) &&
                Objects.equals(mUsbDevice, that.mUsbDevice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mConnectionType, mBluetoothDeviceName, mUsbDevice);
    }

    public static IncredistDevice bleDevice(String deviceName) {
        return new IncredistDevice(deviceName);
    }

    public static IncredistDevice usbDevice(UsbDevice usbDevice) {
        return new IncredistDevice(usbDevice);
    }
}
