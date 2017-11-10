package jp.co.flight.incredist.android.internal.controller.result;

/**
 * シリアル番号取得結果クラス.
 */
@SuppressWarnings("WeakerAccess")
public class SerialNumberResult extends IncredistResult {
    private final String mDeviceName;
    private final String mDeviceModel;
    private final String mFirmwareVersion;
    private final String mSerialNumber;

    /**
     * コンストラクタ.
     * @param deviceName Incredistデバイス名
     * @param deviceModel Incredistデバイスモデル名
     * @param firmwareVersion Incredistファームウェアバージョン
     * @param serialNumber Incredistシリアル番号
     */
    public SerialNumberResult(String deviceName, String deviceModel, String firmwareVersion, String serialNumber) {
        super(IncredistResult.STATUS_SUCCESS);

        this.mDeviceName = deviceName;
        this.mDeviceModel = deviceModel;
        this.mFirmwareVersion = firmwareVersion;
        this.mSerialNumber = serialNumber;
    }

    public String getSerialNumber() {
        return mSerialNumber;
    }
}
