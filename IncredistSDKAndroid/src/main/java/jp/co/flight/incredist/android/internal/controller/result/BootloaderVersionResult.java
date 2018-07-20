package jp.co.flight.incredist.android.internal.controller.result;

import jp.co.flight.incredist.android.model.BootloaderVersion;

/**
 * デバイス情報取得結果クラス.
 */
@SuppressWarnings("WeakerAccess")
public class BootloaderVersionResult extends IncredistResult {
    public final String bootloaderVersion;
    public final String firmwareRevision;

    /**
     * コンストラクタ.
     *
     * @param bootloaderVersion Incredist ブートローダのバージョン
     * @param firmwareRevision  Incredistファームウェアリビジョン
     */
    public BootloaderVersionResult(String bootloaderVersion, String firmwareRevision) {
        super(IncredistResult.STATUS_SUCCESS);

        this.bootloaderVersion = bootloaderVersion;
        this.firmwareRevision = firmwareRevision;
    }

    public BootloaderVersion toBootloaderVersion() {
        return new BootloaderVersion(bootloaderVersion, firmwareRevision);
    }
}
