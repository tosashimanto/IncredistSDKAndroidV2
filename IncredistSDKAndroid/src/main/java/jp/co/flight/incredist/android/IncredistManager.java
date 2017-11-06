package jp.co.flight.incredist.android;

import android.content.Context;
import android.os.HandlerThread;
import android.support.annotation.Nullable;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jp.co.flight.android.bluetooth.le.BluetoothCentral;

/**
 * Incredist 検索と接続の管理クラス.
 */
@SuppressWarnings({ "WeakerAccess", "unused" }) // for public API.
public class IncredistManager {
    private static final String TAG = "IncredistManager";
    private final BluetoothCentral mCentral;

    /**
     * デバイス名によるフィルタ.
     */
    public class DeviceFilter {
        /**
         * 標準の Incredist デバイスかどうかをチェックします.
         * @param deviceName Incredist デバイス名
         * @return Incredist デバイスとして有効なデバイス名の場合: True
         */
        public boolean isValid(String deviceName) {
            if (deviceName.startsWith("Samil") ||
                    deviceName.startsWith("FLT")) {
                return true;
            }

            return false;
        }
    }

    /**
     * コンストラクタ.
     */
    public IncredistManager(Context context) {
        mCentral = new BluetoothCentral(context);
    }

    /**
     * Bluetooth デバイスのスキャンを開始します.
     *
     * @param filter Incredistデバイス名によるフィルタ
     * @param scanTime スキャン実行時間
     * @param success スキャン完了時処理
     * @param failure スキャン失敗時処理
     */
    public void startScan(@Nullable DeviceFilter filter, long scanTime, OnSuccessFunction<List<String>> success, OnFailureFunction<Void> failure) {
        final List<String> deviceList = new ArrayList<>();
        final DeviceFilter deviceFilter = filter != null ? filter : new DeviceFilter();
        mCentral.startScan(scanTime, (_void)->{
            if (success != null) {
                success.onSuccess(deviceList);
            }
        }, (errorCode, _void)-> {
            if (failure != null) {
                failure.onFailure(errorCode, null);
            }
        }, (scanResult)-> {
            if (deviceFilter.isValid(scanResult.deviceName)) {
                deviceList.add(scanResult.deviceName);
            }
            return true;
        });
    }

    /**
     * Bluetooth デバイスのスキャンを終了します.
     */
    public void stopScan() {
        mCentral.stopScan();
    }

    /**
     * Incredistデバイスに接続します.
     *
     * @param deviceName Incredistデバイス名
     * @param timeout タイムアウト時間(単位 msec)
     * @param success 接続成功時処理
     * @param failure 接続失敗時処理
     */
    public void connect(String deviceName, long timeout, @Nullable OnSuccessFunction<Incredist> success, @Nullable OnFailureFunction<Void> failure) {
        //TODO
    }

    /**
     * ログ出力を設定します.
     *
     * @param logLevel 出力レベル
     * @param logStream 出力先ストリーム
     */
    public void setLogLevel(int logLevel, @Nullable OutputStream logStream) {
        //TODO
    }

    /**
     * API バージョンを取得します.
     *
     * @return バージョン文字列
     */
    public String getAPIVersion() {
        return "0.0.1-alpha";
    }

    /**
     * IncredistManager の利用を終了します.
     */
    public void release() {
        mCentral.release();
    }
}
