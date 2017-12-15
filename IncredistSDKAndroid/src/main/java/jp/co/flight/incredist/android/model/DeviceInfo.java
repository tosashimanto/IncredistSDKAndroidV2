package jp.co.flight.incredist.android.model;

import jp.co.flight.incredist.android.internal.controller.result.DeviceInfoResult;

/**
 * デバイス情報取得結果
 */
public class DeviceInfo {
    private final String mDeviceName;
    private final String mDeviceModel;
    private final String mFirmwareVersion;
    private final String mSerialNumber;

    /**
     * コンストラクタ.
     * @param result デバイスからの取得結果
     */
    public DeviceInfo(DeviceInfoResult result) {
        this.mDeviceName = result.deviceName;
        this.mDeviceModel = result.deviceModel;
        this.mFirmwareVersion = result.firmwareVersion;
        this.mSerialNumber = result.serialNumber;
    }

    /**
     * デバイス名を取得します
     * @return デバイス名
     */
    public String getDeviceName() {
        return mDeviceName;
    }

    /**
     * デバイスのモデル名を取得します
     * @return モデル名
     */
    public String getDeviceModel() {
        return mDeviceModel;
    }

    /**
     * ファームウェアのバージョンを取得します
     * @return バージョン名
     */
    public String getFirmwareVersion() {
        return mFirmwareVersion;
    }

    /**
     * シリアル番号を取得します
     * @return シリアル番号
     */
    public String getSerialNumber() {
        return mSerialNumber;
    }
}
