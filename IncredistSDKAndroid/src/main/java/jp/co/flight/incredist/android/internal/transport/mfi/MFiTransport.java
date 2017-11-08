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

/**
 * MFi 版 Incredist との MFi パケット通信を行うユーティリティクラス.
 * このクラスのメソッドはバックグラウンドスレッドで実行される前提なので同期処理として実装する.
 */

public class MFiTransport {
    private static final String TAG ="MFiTransport";

    private static final int MFI_TRANSPORT_TIMEOUT = 500;

    private final BluetoothGattConnection mConnection;
    private BluetoothGattCharacteristic mWriteCharacteristic = null;
    private BluetoothGattCharacteristic mNotifyCharacteristic = null;

    /**
     * 受信用パケット
     */
    private final MFiResponse mResponse = new MFiResponse();

    class ErrorLatch extends CountDownLatch {
        int errorCode;

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
     * @param command 送信コマンド
     * @return レスポンスの MFiパケット
     */
    @WorkerThread
    public MFiResponse sendCommand(MFiCommand command) {
        findCharacteristics();

        // 送信コマンドの途中で割り込まれないように　synchronize で同期化
        synchronized (this) {
            // 受信用パケットを初期化
            if (command.getResponseTimeout() > 0) {
                mResponse.clear();
            }

            int count = command.getPacketCount();
            FLog.d(TAG, String.format(Locale.JAPANESE,"send %d packet(s)", count));
            for (int i = 0; i < count; i++) {
                ErrorLatch latch = new ErrorLatch();
                mConnection.writeCharacteristic(mWriteCharacteristic, command.getValueData(i), success -> {
                    latch.errorCode = IncredistResult.STATUS_SUCCESS;
                    latch.countDown();
                }, (errorCode, failure) -> {
                    latch.errorCode = errorCode;
                    latch.countDown();
                });

                try {
                    if (!latch.await(MFI_TRANSPORT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                        latch.errorCode = IncredistResult.STATUS_TIMEOUT;
                    }
                } catch (InterruptedException e) {
                    latch.errorCode = IncredistResult.STATUS_INTERRUPTED;
                }

                if (latch.errorCode != IncredistResult.STATUS_SUCCESS) {
                    return new MFiInvalidResponse(latch.errorCode);
                }
            }
        }

        if (command.getResponseTimeout() > 0) {
            FLog.d(TAG, "recv packet(s)");

            try {
                synchronized (mResponse) {
                    do {
                        mResponse.wait(command.getResponseTimeout());
                    } while (mResponse.needMoreData());

                    if (mResponse.isValid()) {
                        try {
                            Thread.sleep(command.getGuardWait());
                        } catch (InterruptedException ex) {
                            // ignore.
                        }
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

            return new MFiInvalidResponse(IncredistResult.STATUS_INVALID_RESPONSE);
        }

        try {
            Thread.sleep(command.getGuardWait());
        } catch (InterruptedException ex) {
            // ignore.
        }

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
        FLog.d(TAG, String.format(Locale.JAPANESE, "sendServive : %s", sendService != null ? sendService.getUuid().toString() : "(null)"));
        if (sendService != null) {
            mWriteCharacteristic = sendService.getCharacteristic(UUID.fromString(IncredistConstants.FS_INCREDIST_FFB2_CHARACTERISTICS_UUID_FULL));
            FLog.d(TAG, String.format(Locale.JAPANESE, "mWriteCharacteristic : %s %d", mWriteCharacteristic != null ? mWriteCharacteristic.getUuid().toString() : "(null)",
                    mWriteCharacteristic != null ? mWriteCharacteristic.getProperties() : -1));
        }

        BluetoothGattService recvService = mConnection.findService(IncredistConstants.FS_INCREDIST_RECEIVE_SERVICE_UUID_FULL);
        FLog.d(TAG, String.format(Locale.JAPANESE, "recvServive : %s", recvService != null ? recvService.getUuid().toString() : "(null)"));
        if (recvService != null) {
            mNotifyCharacteristic = recvService.getCharacteristic(UUID.fromString(IncredistConstants.FS_INCREDIST_FFA3_CHARACTERISTICS_UUID_FULL));
            FLog.d(TAG, String.format(Locale.JAPANESE, "mNotifyCharacteristic : %s %d", mNotifyCharacteristic != null ? mNotifyCharacteristic.getUuid().toString() : "(null)",
                    mNotifyCharacteristic != null ? mNotifyCharacteristic.getProperties() : -1));
        }

        mConnection.registerNotify(mNotifyCharacteristic, (success)->{}, (errorCode, failure)->{}, (notify)-> {
            synchronized (mResponse) {
                FLog.d(TAG, String.format(Locale.JAPANESE, "receive notify %d", notify.getValue().length));
                mResponse.appendData(notify.getValue());
                if (!mResponse.needMoreData()) {
                    mResponse.notifyAll();
                }
            }
        });
    }
}
