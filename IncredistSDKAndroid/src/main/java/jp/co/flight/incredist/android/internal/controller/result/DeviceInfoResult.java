package jp.co.flight.incredist.android.internal.controller.result;

import android.support.annotation.Nullable;

import jp.co.flight.incredist.android.model.DeviceInfo;

/**
 * デバイス情報取得結果クラス.
 */
@SuppressWarnings("WeakerAccess")
public class DeviceInfoResult extends IncredistResult {
    public final String deviceName;
    public final String deviceModel;
    public final String firmwareVersion;
    public final String serialNumber;
    @Nullable
    public final String hardwareVersion;

    /**
     * コンストラクタ.
     *
     * @param deviceName      Incredistデバイス名
     * @param deviceModel     Incredistデバイスモデル名
     * @param firmwareVersion Incredistファームウェアバージョン
     * @param serialNumber    Incredistシリアル番号
     */
    public DeviceInfoResult(String deviceName, String deviceModel, String firmwareVersion, String serialNumber, @Nullable String hardwareVersion) {
        super(IncredistResult.STATUS_SUCCESS);

        this.deviceName = deviceName;
        this.deviceModel = deviceModel;
        this.firmwareVersion = firmwareVersion;
        this.serialNumber = serialNumber;
        this.hardwareVersion = hardwareVersion;
    }

    public DeviceInfo toDeviceInfo() {
        return new DeviceInfo(deviceName, deviceModel, firmwareVersion, serialNumber, hardwareVersion);
    }
}
