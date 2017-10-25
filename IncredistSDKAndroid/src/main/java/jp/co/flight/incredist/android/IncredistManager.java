package jp.co.flight.incredist.android;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.OutputStream;
import java.util.List;

/**
 * Incredist 検索と接続の管理クラス.
 */

public class IncredistManager {
    /**
     * デバイス名によるフィルタ実装用インタフェース.
     */
    interface DeviceFilter {
        /**
         * 発見した Incredist デバイスが有効なデバイスかどうかを返却します.
         *
         * @param incredistManager IncredistManager オブジェクト
         * @param deviceName Incredist デバイス名
         * @return True: 有効 False: 無効
         */
        boolean contains(IncredistManager incredistManager, String deviceName);
    }

    /**
     * 標準デバイスフィルタクラス.
     */
    public class IncredistStandardDeviceFilter implements DeviceFilter {
        /**
         * 標準の Incredist デバイスかどうかをチェックします.
         * @param incredistManager IncredistManager オブジェクト
         * @param deviceName Incredist デバイス名
         * @return Incredist デバイスとして有効なデバイス名の場合: True
         */
        @Override
        public boolean contains(IncredistManager incredistManager, String deviceName) {
            if (deviceName.startsWith("Samil") ||
                    deviceName.startsWith("FLT")) {
                return true;
            }

            return false;
        }
    }

    /**
     * Bluetooth デバイススキャン時の結果取得用リスナ
     */
    interface ScanDeviceListener {
        /**
         * スキャン完了時に呼び出されます.
         * @param incredistManager IncredistManager インスタンス
         * @param deviceNameList 検出されたデバイス名のリスト
         */
        void onScanFinished(IncredistManager incredistManager, List<String> deviceNameList);

        /**
         * スキャンに失敗した場合に呼び出されます.
         */
        void onScanFailed();
    }

    /**
     * Incredistデバイスとの接続・切断を通知するリスナ
     */
    interface ConnectionListener {
        /**
         * 接続完了時に呼び出されます.
         *
         * @param incredist Incredist オブジェクト
         */
        void onConnect(Incredist incredist);

        /**
         * 切断時に呼び出されます.
         *
         * @param incredist Incredist オブジェクト
         */
        void onDisconnect(Incredist incredist);
    }

    /**
     * Bluetooth デバイスのスキャンを開始します.
     *
     * @param listener スキャン結果取得用リスナ
     * @param filter Incredistデバイス名によるフィルタ
     * @param scanTime スキャン実行時間
     */
    void startScan(@NonNull ScanDeviceListener listener, @Nullable DeviceFilter filter, long scanTime) {
        //TODO
    }

    /**
     * Bluetooth デバイスのスキャンを終了します.
     */
    void stopScan() {
        //TODO
    }

    /**
     * Incredistデバイスに接続します.
     *
     * @param deviceName Incredistデバイス名
     * @param listener 接続処理リスナ
     */
    void connect(String deviceName, @NonNull ConnectionListener listener) {
        //TODO
    }

    /**
     * ログ出力を設定します.
     *
     * @param logLevel 出力レベル
     * @param logStream 出力先ストリーム
     */
    void setLogLevel(int logLevel, @Nullable OutputStream logStream) {
        //TODO
    }

    /**
     * API バージョンを取得します.
     *
     * @return バージョン文字列
     */
    String getAPIVersion() {
        return "2.0.0pre-alpha";
    }
}
