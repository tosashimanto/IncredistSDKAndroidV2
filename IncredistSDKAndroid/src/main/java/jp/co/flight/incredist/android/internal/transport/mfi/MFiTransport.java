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
     */
    public MFiTransport(BluetoothGattConnection connection) {
        mConnection = connection;
    }

    /**
     * コマンドを送信し、レスポンスを受信して返却します.
     *
     * @param command 送信コマンド
     * @return レスポンスの MFiパケット
     */
    @WorkerThread
    public MFiResponse sendCommand(MFiCommand command) {
        findCharacteristics();

        // 送信コマンドの途中で割り込まれないように　synchronize で同期化
        synchronized (this) {
            mCommand = command;

            // 受信用パケットを初期化
            if (command.getResponseTimeout() > 0) {
                mResponse.clear();
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
                        latch.mErrorCode = IncredistResult.STATUS_TIMEOUT;
                    }
                } catch (InterruptedException e) {
                    latch.mErrorCode = IncredistResult.STATUS_INTERRUPTED;
                }

                if (latch.mErrorCode != IncredistResult.STATUS_SUCCESS) {
                    return new MFiInvalidResponse(latch.mErrorCode);
                }
            }
        }

        if (command.getResponseTimeout() > 0) {
            FLog.d(TAG, "recv packet(s)");

            try {
                synchronized (mResponse) {
                    do {
                        mResponse.wait(command.getResponseTimeout());

                        if (mCommand.cancelable() && !mResponse.hasData() && mCancelling != null) {
                            mCancelling.countDown();
                            mCommand = null;
                            return new MFiInvalidResponse(IncredistResult.STATUS_CANCELED);
                        }
                    } while (mResponse.needMoreData());

                    if (mResponse.isValid()) {
                        FLog.d(TAG, "recv valid packet: " + LogUtil.hexString(mResponse.getData()));
                        try {
                            Thread.sleep(command.getGuardWait());
                        } catch (InterruptedException ex) {
                            // ignore.
                        }
                        mCommand = null;
                        return mResponse.copyInstance();
                    }
                }
            } catch (InterruptedException ex) {
                // ignore.
                FLog.d(TAG, "exception", ex);
            }

            try {
                Thread.sleep(command.getGuardWait());
            } catch (InterruptedException ex) {
                // ignore.
            }

            mCommand = null;
            return new MFiInvalidResponse(IncredistResult.STATUS_TIMEOUT);
        }

        try {
            Thread.sleep(command.getGuardWait());
        } catch (InterruptedException ex) {
            // ignore.
        }

        mCommand = null;
        return new MFiNoResponse();
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
                FLog.d(TAG, String.format(Locale.JAPANESE, "receive notify %d", notify.getValue().length));
                mResponse.appendData(notify.getValue());
                if (!mResponse.needMoreData()) {
                    mResponse.notifyAll();
                }
            }
        });
    }

    /**
     * 受信待ち処理をキャンセル
     *
     * 送信 / 受信待ち処理には、
     *  - 送信前
     *  - 送信中
     *  - 受信待ち
     *  - 受信中
     *  - 受信完了後
     * の状態がある。送信前・送信中の場合は完了まで一旦待つ
     * 受信中(すでにMFiパケットの一部を受け取っている)場合には
     * 失敗として扱う(MFiパケットの残りを受信して通常処理を行う)
     *
     * sendCommand とは別のスレッドで実行する必要がある
     *
     * @return キャンセル成功した場合は True, 失敗した場合は False を返す　
     */
    @WorkerThread
    public boolean cancelReceive() {
        synchronized (mResponse) {
            mCancelling = new CountDownLatch(1);
            mResponse.notifyAll();

            try {
                mCancelling.await(1000, TimeUnit.MILLISECONDS);

                return true;
            } catch (InterruptedException e) {
                // ignore.
            }

            return false;
        }
    }

    public void release() {
        mConnection = null;
        mWriteCharacteristic = null;
        mNotifyCharacteristic = null;
    }
}
