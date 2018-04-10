package jp.co.flight.incredist.android.model;

/**
 * デバイス情報取得結果
 */
public class DeviceInfo {
    private final String mDeviceName;
    private final String mDeviceModel;
    private final String mFirmwareVersion;
    private final String mSerialNumber;
    private final String mHardwareVersion;

    /**
     * コンストラクタ.
     *
     * @param deviceName      デバイス名
     * @param deviceModel     モデル名
     * @param firmwareVersion ファームウェアのバージョン
     * @param serialNumber    シリアル番号
     * @param hardwareVersion ハードウェアのバージョン
     */
    public DeviceInfo(String deviceName, String deviceModel, String firmwareVersion, String serialNumber, String hardwareVersion) {
        mDeviceName = deviceName;
        mDeviceModel = deviceModel;
        mFirmwareVersion = firmwareVersion;
        mSerialNumber = serialNumber;
        mHardwareVersion = hardwareVersion;
    }

    /**
     * デバイス名を取得します
     *
     * @return デバイス名
     */
    public String getDeviceName() {
        return mDeviceName;
    }

    /**
     * デバイスのモデル名を取得します
     *
     * @return モデル名
     */
    public String getDeviceModel() {
        return mDeviceModel;
    }

    /**
     * ファームウェアのバージョンを取得します
     *
     * @return バージョン名
     */
    public String getFirmwareVersion() {
        return mFirmwareVersion;
    }

    /**
     * シリアル番号を取得します
     *
     * @return シリアル番号
     */
    public String getSerialNumber() {
        return mSerialNumber;
    }

    /**
     * ハードウェアのバージョンを取得します
     *
     * @return バージョン名
     */
    public String getHardwareVersion() {
        return mHardwareVersion;
    }
}
