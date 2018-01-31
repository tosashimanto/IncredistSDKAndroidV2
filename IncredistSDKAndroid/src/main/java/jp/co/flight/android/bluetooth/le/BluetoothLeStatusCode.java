package jp.co.flight.android.bluetooth.le;

/**
 * Bluetooth LE 処理のステータスコード
 */
public interface BluetoothLeStatusCode {
    /**
     * すでに Bluetooth LE Scan 実行中
     */
    int SCAN_ERROR_ALREADY_SCANNING = 4001;

    /**
     * Bluetooth LE Scan が開始できない
     */
    int SCAN_ERROR_CANT_START = 4002;

    /**
     * Bluetooth LE Scan を実行するための Permission が許可されていない
     */
    int SCAN_ERROR_NO_PERMISSION = 4003;

    /**
     * Notification の登録が行えなかった
     */
    int ERROR_REGISTER_NOTIFICATION = 4797;

    /**
     * Notification 登録処理中にタイムアウトした
     */
    int ERROR_REGISTER_NOTIFICATION_TIMEOUT = 4798;

    /**
     * Notification 登録処理中にスレッドが中断された
     */
    int ERROR_REGISTER_NOTIFICATION_INTERRUPTED = 4799;

    /**
     * Bluetooth データが送信できなかった
     */
    int ERROR_WRITE_FAILED = 4796;

    /**
     * 送信先の Characteristic が存在しない
     */
    int ERROR_NO_CHARACTERISTIC = 4990;
}
