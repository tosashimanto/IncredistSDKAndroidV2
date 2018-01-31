package jp.co.flight.incredist.android.internal.transport.mfi;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.WorkerThread;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.incredist.android.internal.controller.IncredistConstants;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.util.FLog;
import jp.co.flight.incredist.android.internal.util.LogUtil;

/**
 * MFi 版 Incredist との MFi パケット通信を行うユーティリティクラス.
 * このクラスのメソッドはバックグラウンドスレッドで実行される前提なので同期処理として実装する.
 */
public class MFiTransport {
    private static final String TAG = "MFiTransport";

    private static final int MFI_TRANSPORT_TIMEOUT = 500;

    private BluetoothGattConnection mConnection;
    private BluetoothGattCharacteristic mWriteCharacteristic = null;
    private BluetoothGattCharacteristic mNotifyCharacteristic = null;

    /**
     * 送信中のコマンド
     */
    private MFiCommand mCommand = null;

    /**
     * 受信用パケット.
     */
    private final MFiResponse mResponse = new MFiResponse();

    /**
     * キャンセルフラグ
     */
    private CountDownLatch mCancelling = null;

    class ErrorLatch extends CountDownLatch {
        int mErrorCode;

        ErrorLatch() {
            super(1);
        }
    }

    /**
     * コンストラクタ.
     *
     * @param connection BluetoothGattConnection オブジェクト
     */
    public MFiTransport(BluetoothGattConnection connection) {
        mConnection = connection;
    }

    /**
     * 現在コマンド送受信中かどうかを取得します。
     *
     * @return 送受信処理中の場合 true
     */
    public boolean isBusy() {
        return false;
    }

    /**
     * コマンドを送信し、レスポンスを受信して返却します.
     *
     * @param command 送信コマンド
     * @return レスポンスの MFiパケット
     */
    @WorkerThread
    public IncredistResult sendCommand(MFiCommand command) {
        findCharacteristics();

        // 送信コマンドの途中で割り込まれないように　synchronize で同期化
        synchronized (this) {
            mCommand = command;

            FLog.d(TAG, String.format("sendCommand %s", command.getClass().getSimpleName()));

            // 受信用パケットを初期化
            if (command.getResponseTimeout() > 0) {
                mResponse.clear();
            }

            if (mCancelling != null) {
                mCancelling.countDown();
                return new IncredistResult(IncredistResult.STATUS_CANCELED);
            }

            int count = command.getPacketCount();
            FLog.d(TAG, String.format(Locale.JAPANESE, "send %d packet(s)", count));
            for (int i = 0; i < count; i++) {
                ErrorLatch latch = new ErrorLatch();
                mConnection.writeCharacteristic(mWriteCharacteristic, command.getValueData(i), success -> {
                    latch.mErrorCode = IncredistResult.STATUS_SUCCESS;
                    latch.countDown();
                }, (errorCode, failure) -> {
                    latch.mErrorCode = errorCode;
                    latch.countDown();
                });

                try {
                    if (!latch.await(MFI_TRANSPORT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                        FLog.d(TAG, "send timeout");
                        latch.mErrorCode = IncredistResult.STATUS_TIMEOUT;
                    }
                } catch (InterruptedException e) {
                    FLog.d(TAG, "send interrupted");
                    latch.mErrorCode = IncredistResult.STATUS_INTERRUPTED;
                }

                if (latch.mErrorCode != IncredistResult.STATUS_SUCCESS) {
                    FLog.d(TAG, String.format(Locale.JAPANESE, "send error %d", latch.mErrorCode));
                    return new IncredistResult(latch.mErrorCode);
                }
            }
        }

        if (command.getResponseTimeout() > 0) {
            FLog.d(TAG, "recv packet(s)");

            try {
                synchronized (mResponse) {
                    do {
                        long timeout = command.getResponseTimeout();
                        FLog.d(TAG, String.format(Locale.JAPANESE, "recv wait %dmsec", timeout));
                        mResponse.wait(timeout);

                        if (mCommand.cancelable() && !mResponse.hasData() && mCancelling != null) {
                            FLog.d(TAG, String.format("command canceled: %s", mCommand.getClass().getSimpleName()));
                            mCommand = null;
                            mCancelling.countDown();
                            return new IncredistResult(IncredistResult.STATUS_CANCELED);
                        }
                    } while (mResponse.needMoreData());

                    if (mResponse.isValid()) {
                        FLog.d(TAG, "recv valid packet: " + LogUtil.hexString(mResponse.getData()));
                        try {
                            Thread.sleep(command.getGuardWait());
                        } catch (InterruptedException ex) {
                            // ignore.
                        }
                        IncredistResult result = command.parseResponse(mResponse.copyInstance());
                        if (result.status == IncredistResult.STATUS_CONTINUE_MULTIPLE_RESPONSE) {
                            // 継続するパケットがある場合はパケット情報をクリアして次のデータを待つ
                            mResponse.clear();
                        } else {
                            mCommand = null;
                            return result;
                        }
                    }
                }
            } catch (InterruptedException ex) {
                // ignore.
            }

            try {
                Thread.sleep(command.getGuardWait());
            } catch (InterruptedException ex) {
                // ignore.
            }

            mCommand = null;
            FLog.d(TAG, "recv timeout: " + LogUtil.hexString(mResponse.getData()));
            return new IncredistResult(IncredistResult.STATUS_TIMEOUT);
        }

        try {
            Thread.sleep(command.getGuardWait());
        } catch (InterruptedException ex) {
            // ignore.
        }

        mCommand = null;
        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }

    /**
     * 送受信用の characteristic を取得.
     * 取得済みの場合はすぐに return する
     */
    private void findCharacteristics() {
        if (mWriteCharacteristic != null && mNotifyCharacteristic != null) {
            return;
        }

        BluetoothGattService sendService = mConnection.findService(IncredistConstants.FS_INCREDIST_SEND_SERVICE_UUID_FULL);
        FLog.d(TAG, String.format(Locale.JAPANESE, "sendService : %s", sendService != null ? sendService.getUuid().toString() : "(null)"));
        if (sendService != null) {
            mWriteCharacteristic = sendService.getCharacteristic(UUID.fromString(IncredistConstants.FS_INCREDIST_FFB2_CHARACTERISTICS_UUID_FULL));
            FLog.d(TAG, String.format(Locale.JAPANESE, "mWriteCharacteristic : %s %d", mWriteCharacteristic != null ? mWriteCharacteristic.getUuid().toString() : "(null)",
                    mWriteCharacteristic != null ? mWriteCharacteristic.getProperties() : -1));
        }

        BluetoothGattService recvService = mConnection.findService(IncredistConstants.FS_INCREDIST_RECEIVE_SERVICE_UUID_FULL);
        FLog.d(TAG, String.format(Locale.JAPANESE, "recvService : %s", recvService != null ? recvService.getUuid().toString() : "(null)"));
        if (recvService != null) {
            mNotifyCharacteristic = recvService.getCharacteristic(UUID.fromString(IncredistConstants.FS_INCREDIST_FFA3_CHARACTERISTICS_UUID_FULL));
            FLog.d(TAG, String.format(Locale.JAPANESE, "mNotifyCharacteristic : %s %d", mNotifyCharacteristic != null ? mNotifyCharacteristic.getUuid().toString() : "(null)",
                    mNotifyCharacteristic != null ? mNotifyCharacteristic.getProperties() : -1));
        }

        mConnection.registerNotify(mNotifyCharacteristic, (success) -> {
        }, (errorCode, failure) -> {
        }, (notify) -> {
            synchronized (mResponse) {
                // BLE notify 受信時処理 : mResponse に append する
                FLog.d(TAG, String.format(Locale.JAPANESE, "receive notify %d %s", notify.getValue().length, LogUtil.hexString(notify.getValue())));
                mResponse.appendData(notify.getValue());
                if (!mResponse.needMoreData()) {
                    byte[] data = mResponse.getData();
                    if (data != null) {
                        FLog.d(TAG, String.format(Locale.JAPANESE, "recv notify MFi packet: %d %s", data.length, LogUtil.hexString(data)));
                    } else {
                        FLog.d(TAG, "recv buffer error..");
                    }
                    mResponse.notifyAll();
                }
            }
        });
    }

    /**
     * 受信待ち処理をキャンセル
     *
     * キャンセルできるかどうかは MFiCommand によって異なる
     * キャンセルできないコマンドの場合は false を返す
     *
     * キャンセルできるコマンドであっても、送信 / 受信待ち処理には、
     * - 送信前
     * - 送信中
     * - 受信待ち
     * - 受信中
     * - 受信完了後
     * の状態がある。送信前の場合は mCancelling != null をチェックし、
     * 受信待ちの場合は mResponse の notify を受け取ってそれぞれ countdown するので
     * キャンセル成功する。
     *
     * 送信中の場合はコマンド途中で停止させることはせずに送信完了まで一旦待つ
     *
     * 受信中(すでにMFiパケットの一部を受け取っている)場合には
     * フラグを立てて、MFiパケットの残りを受信した際にキャンセル済みの
     * コマンドについてはコールバックを呼び出さない。
     *
     * このメソッドは sendCommand とは別のスレッドで実行する必要がある
     *
     * @return キャンセル成功した場合は STATUS_SUCCESS, 失敗した場合はエラー結果を含む IncredistResult オブジェクト
     */
    @WorkerThread
    public IncredistResult cancel() {
        synchronized (mResponse) {
            if (mCommand == null || !mCommand.cancelable()) {
                return new IncredistResult(IncredistResult.STATUS_NOT_CANCELLABLE);
            }

            mCancelling = new CountDownLatch(1);
            mResponse.notifyAll();
        }

        try {
            boolean res = mCancelling.await(3000, TimeUnit.MILLISECONDS);

            if (res) {
                return new IncredistResult(IncredistResult.STATUS_SUCCESS);
            }
        } catch (InterruptedException e) {
            // ignore.
        } finally {
            mCancelling = null;
        }

        return new IncredistResult(IncredistResult.STATUS_CANCEL_FAILED);
    }

    /**
     * リソースを解放します
     */
    public void release() {
        mConnection = null;
        mWriteCharacteristic = null;
        mNotifyCharacteristic = null;
    }
}
