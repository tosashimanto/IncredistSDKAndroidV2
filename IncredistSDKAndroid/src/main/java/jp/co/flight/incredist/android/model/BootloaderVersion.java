package jp.co.flight.incredist.android.model;

/**
 * ブートローダ情報取得結果
 */
@SuppressWarnings("unused")
public class BootloaderVersion {
    private final String mBootloaderVersion;
    private final String mFirmwareRevision;

    /**
     * コンストラクタ.
     *
     * @param bootloaderVersion ブートローダのバージョン
     * @param firmwareRevision  ファームウェアのリビジョン
     */
    public BootloaderVersion(String bootloaderVersion, String firmwareRevision) {
        mBootloaderVersion = bootloaderVersion;
        mFirmwareRevision = firmwareRevision;
    }

    /**
     * ブートローダのバージョンを取得します
     *
     * @return ブートローダのバージョン名
     */
    public String getBootloaderVersion() {
        return mBootloaderVersion;
    }

    /**
     * ファームウェアのリビジョンを取得します
     *
     * @return リビジョン文字列
     */
    public String getFirmwareRevision() {
        return mFirmwareRevision;
    }
}
