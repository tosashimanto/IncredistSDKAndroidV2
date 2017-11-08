package jp.co.flight.incredist.android.internal.controller.result;

/**
 * シリアル番号取得結果クラス.
 */
@SuppressWarnings("WeakerAccess")
public class SerialNumberResult extends IncredistResult {
    public final String deviceName;
    public final String devoceModel;
    public final String firmwareVersion;
    public final String serialNumber;

    public SerialNumberResult(String deviceName, String deviceModel, String firmwareVersion, String serialNumber) {
        super(IncredistResult.STATUS_SUCCESS);

        this.deviceName = deviceName;
        this.devoceModel = deviceModel;
        this.firmwareVersion = firmwareVersion;
        this.serialNumber = serialNumber;
    }
}
