package jp.co.flight.incredist.android.internal.transport.mfi;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
 * BLE - MFi 版 Incredist との MFi パケット通信を行うユーティリティクラス.
 * このクラスのメソッドはバックグラウンドスレッドで実行される前提なので同期処理として実装する.
 */
public class BleMFiTransport implements MFiTransport {
    private static final String TAG = "BleMFiTransport";

    private static final int MFI_TRANSPORT_TIMEOUT = 1000;
    private static final long CANCEL_TIMEOUT = 3000;

    @Nullable
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
    public BleMFiTransport(@NonNull BluetoothGattConnection connection) {
        mConnection = connection;
    }

    /**
     * 現在コマンド送受信中かどうかを取得します。
     * TODO コマンド実行可能な状態かどうかを返すように中身を実装
     *
     * @return 送受信処理中の場合 true
     */
    @Override
    public boolean isBusy() {
        return false;
    }

    /**
     * 複数コマンドを送信し、複数レスポンスを受信して返却します.
     *
     * @param commandList 送信コマンド
     * @return レスポンスの MFiパケット
     */
    @WorkerThread
    @Override
    public ArrayList<IncredistResult> sendCommands(MFiCommand... commandList) {
        // ANDROID_GMO-726でUSB側に追加
        // Ble側は何もしない
        return new ArrayList<IncredistResult>();
    }

    /**
     * コマンドを送信し、レスポンスを受信して返却します.
     *
     * @param commandList 送信コマンド
     * @return レスポンスの MFiパケット
     */
    @Override
    @WorkerThread
    public IncredistResult sendCommand(MFiCommand... commandList) {
        findCharacteristics();

        if (commandList == null || commandList.length == 0) {
            return new IncredistResult(IncredistResult.STATUS_INVALID_COMMAND);
        }

        // ANDROID_SDK_DEV-34
        // EMVカーネル設定の場合は別処理で対応する
        if (commandList[0] instanceof MFiEmvKernelSetupCommand) {
            return sendEmvKernelSetupCommand(commandList);
        }

        Iterator<MFiCommand> iterator = Arrays.asList(commandList).iterator();

        // レスポンスの解析などは最初の引数のオブジェクトで実行する
        MFiCommand firstCommand = commandList[0];

        long startTime = System.currentTimeMillis();
        FLog.d(TAG, String.format("sendCommand %s", firstCommand.getClass().getSimpleName()));

        // 送信コマンドの途中で割り込まれないように　synchronize で同期化
        synchronized (this) {
            mCommand = firstCommand;

            FLog.d(TAG, String.format("sendCommand start %s", firstCommand.getClass().getSimpleName()));

            // 受信用パケットを初期化
            if (firstCommand.getResponseTimeout() > 0) {
                mResponse.clear();
            }

            if (mCancelling != null) {
                mCancelling.countDown();

                FLog.d(TAG, String.format(Locale.US, "sendCommand cancelled(%d) %s", IncredistResult.STATUS_CANCELED, firstCommand.getClass().getSimpleName()));

                return new IncredistResult(IncredistResult.STATUS_CANCELED);
            }

            do {
                MFiCommand command = iterator.next();
                int count = command.getPacketCount();
                FLog.d(TAG, String.format(Locale.JAPANESE, "send %d packet(s)", count));
                for (int i = 0; i < count; i++) {
                    BluetoothGattConnection connection = mConnection;
                    BluetoothGattCharacteristic writeCharacteristic = mWriteCharacteristic;
                    if (connection == null || writeCharacteristic == null) {
                        FLog.d(TAG, String.format(Locale.US, "sendCommand released(%d) %s", IncredistResult.STATUS_RELEASED, firstCommand.getClass().getSimpleName()));
                        return new IncredistResult(IncredistResult.STATUS_RELEASED);
                    }
                    ErrorLatch latch = new ErrorLatch();
                    connection.writeCharacteristic(writeCharacteristic, command.getValueData(i), success -> {
                        latch.mErrorCode = IncredistResult.STATUS_SUCCESS;
                        latch.countDown();
                    }, (errorCode, failure) -> {
                        latch.mErrorCode = errorCode;
                        latch.countDown();
                    });

                    try {
                        if (!latch.await(MFI_TRANSPORT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                            FLog.w(TAG, "send timeout");
                            latch.mErrorCode = IncredistResult.STATUS_SEND_TIMEOUT;
                        }
                    } catch (InterruptedException e) {
                        FLog.w(TAG, "send interrupted");
                        latch.mErrorCode = IncredistResult.STATUS_INTERRUPTED;
                    }

                    if (latch.mErrorCode != IncredistResult.STATUS_SUCCESS) {
                        FLog.w(TAG, String.format(Locale.JAPANESE, "sendCommand error %d %s", latch.mErrorCode, command.getClass().getSimpleName()));
                        return new IncredistResult(latch.mErrorCode);
                    }
                }
            } while (iterator.hasNext());
        }

        if (firstCommand.getResponseTimeout() == 0) {
            // 応答パケットがない場合は guardWait だけ待機して、 MFiNoResponse を結果とする
            try {
                Thread.sleep(firstCommand.getGuardWait());
            } catch (InterruptedException ex) {
                // ignore.
            }

            mCommand = null;
            FLog.d(TAG, String.format("sendCommand has no response %s", firstCommand.getClass().getSimpleName()));
            return firstCommand.parseResponse(new MFiNoResponse());
        } else {
            FLog.d(TAG, String.format("sendCommand recv packet(s) for %s", firstCommand.getClass().getSimpleName()));

            try {
                synchronized (mResponse) {
                    boolean continueReceive;
                    do {
                        continueReceive = false;
                        do {
                            long timeout = firstCommand.getResponseTimeout();
                            FLog.d(TAG, String.format(Locale.JAPANESE, "recv wait %dmsec", timeout));

                            if (timeout < 0) {
                                mResponse.wait(); // タイムアウト指定なし
                            } else {
                                mResponse.wait(timeout);
                            }

                            if (mCommand.cancelable() && !mResponse.hasData() && mCancelling != null) {
                                FLog.d(TAG, String.format("sendCommand canceled: %s", mCommand.getClass().getSimpleName()));
                                mCommand = null;
                                mCancelling.countDown();
                                return new IncredistResult(IncredistResult.STATUS_CANCELED);
                            }
                        } while (mResponse.needMoreData());

                        if (mResponse.isValid()) {
                            FLog.d(TAG, "recv valid packet: " + LogUtil.hexString(mResponse.getData()));
                            IncredistResult result = firstCommand.parseResponse(mResponse.copyInstance());
                            if (result.status == IncredistResult.STATUS_CONTINUE_MULTIPLE_RESPONSE) {
                                // 継続するパケットがある場合はパケット情報をクリアして次のデータを待つ
                                mResponse.clear();
                                continueReceive = true;
                            } else {
                                try {
                                    Thread.sleep(firstCommand.getGuardWait());
                                } catch (InterruptedException ex) {
                                    // ignore.
                                }
                                long real = System.currentTimeMillis() - startTime;
                                FLog.d(TAG, String.format(Locale.JAPANESE, "sendCommand result:%d wait:%d real:%d %s", result.status, firstCommand.getResponseTimeout(), real, mCommand.getClass().getSimpleName()));

                                mCommand = null;
                                if (result.status == IncredistResult.STATUS_SUCCESS) {
                                    mResponse.clear();
                                }
                                return result;
                            }
                        }
                    } while (continueReceive);
                }
            } catch (InterruptedException ex) {
                // ignore.
            }

            try {
                Thread.sleep(firstCommand.getGuardWait());
            } catch (InterruptedException ex) {
                // ignore.
            }

            FLog.w(TAG, String.format(Locale.JAPANESE, "sendCommand recv timeout(%d): %s %s", IncredistResult.STATUS_TIMEOUT, mCommand.getClass().getSimpleName(), LogUtil.hexString(mResponse.getData())));
            mCommand = null;
            return new IncredistResult(IncredistResult.STATUS_TIMEOUT);
        }
    }

    /**
     * コマンドを送信し、レスポンスを受信して返却します.
     * MFiEmvKernelSetupCommand専用のメソッド
     *
     * @param commandList 送信コマンド
     * @return レスポンスの MFiパケット
     */
    private IncredistResult sendEmvKernelSetupCommand(MFiCommand... commandList) {

        Iterator<MFiCommand> iterator = Arrays.asList(commandList).iterator();

        // レスポンスの解析などは最初の引数のオブジェクトで実行する
        MFiCommand firstCommand = commandList[0];

        // 送信コマンドの途中で割り込まれないように　synchronize で同期化
        synchronized (this) {
            mCommand = firstCommand;

            FLog.d(TAG, String.format("sendCommand start %s", firstCommand.getClass().getSimpleName()));

            // 受信用パケットを初期化
            if (firstCommand.getResponseTimeout() > 0) {
                mResponse.clear();
            }

            if (mCancelling != null) {
                mCancelling.countDown();

                FLog.d(TAG, String.format(Locale.US, "sendCommand cancelled(%d) %s", IncredistResult.STATUS_CANCELED, firstCommand.getClass().getSimpleName()));

                return new IncredistResult(IncredistResult.STATUS_CANCELED);
            }

            FLog.d(TAG, String.format(Locale.JAPANESE, "send %d iterator(s)", commandList.length));
            while (iterator.hasNext()) {
                MFiCommand command = iterator.next();
                BluetoothGattConnection connection = mConnection;
                if (connection == null) {
                    FLog.d(TAG, String.format(Locale.US, "sendCommand released(%d) %s", IncredistResult.STATUS_RELEASED, firstCommand.getClass().getSimpleName()));
                    return new IncredistResult(IncredistResult.STATUS_RELEASED);
                }

                // パケット数取得
                int count = command.getPacketCount();
                FLog.d(TAG, String.format(Locale.JAPANESE, "send %d packet(s)", count));
                // パケット数分incredistに送信
                for (int i = 0; i < count; i++) {
                    ErrorLatch latch = new ErrorLatch();
                    latch.mErrorCode = IncredistResult.STATUS_SUCCESS;
                    BluetoothGattCharacteristic writeCharacteristic = mWriteCharacteristic;
                    if (writeCharacteristic == null) {
                        FLog.d(TAG, String.format(Locale.US, "sendCommand released(%d) %s", IncredistResult.STATUS_RELEASED, firstCommand.getClass().getSimpleName()));
                        return new IncredistResult(IncredistResult.STATUS_RELEASED);
                    }
                    // パケット送信。
                    connection.writeCharacteristic(writeCharacteristic, command.getValueData(i), success -> {
                        // パケット送信時のコールバック。
                        FLog.w(TAG, "send packet success");
                        latch.mErrorCode = IncredistResult.STATUS_SUCCESS;
                        latch.countDown();
                    }, (errorCode, failure) -> {
                        FLog.w(TAG, String.format(Locale.JAPANESE, "sendCommand error %d %s", latch.mErrorCode, command.getClass().getSimpleName()));
                        latch.mErrorCode = errorCode;
                        latch.countDown();
                    });

                    try {
                        if (!latch.await(MFI_TRANSPORT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                            FLog.w(TAG, String.format(Locale.JAPANESE, "sendCommand packet timeout. %d count:%d", latch.mErrorCode, i));
                            latch.mErrorCode = IncredistResult.STATUS_SEND_TIMEOUT;
                        }
                    } catch (InterruptedException e) {
                        FLog.w(TAG, "send interrupted");
                        latch.mErrorCode = IncredistResult.STATUS_INTERRUPTED;
                    }

                    if (latch.mErrorCode != IncredistResult.STATUS_SUCCESS) {
                        FLog.w(TAG, String.format(Locale.JAPANESE, "sendCommand error %d %s", latch.mErrorCode, command.getClass().getSimpleName()));
                        return new IncredistResult(latch.mErrorCode);
                    }
                }

                ErrorLatch notifyLatch = new ErrorLatch();
                notifyLatch.mErrorCode = IncredistResult.STATUS_SUCCESS;

                connection.setNotifyFunction((notify) -> {

                    // onCharactaristicChangedが発生した時の処理
                    // BLE notify 受信時処理 : mResponse に append する
                    FLog.d(TAG, String.format(Locale.JAPANESE, "receive notify %d %s", notify.getValue().length, LogUtil.hexString(notify.getValue())));
                    mResponse.appendData(notify.getValue());
                    notifyLatch.countDown();
                });

                try {
                    if (!notifyLatch.await(5000, TimeUnit.MILLISECONDS)) {
                        FLog.d(TAG, String.format(Locale.JAPANESE, "receive response timeout. iterator:%s", iterator.hasNext()));
                        notifyLatch.mErrorCode = IncredistResult.STATUS_SEND_TIMEOUT;
                    }
                } catch (InterruptedException e) {
                    FLog.w(TAG, "send interrupted");
                    notifyLatch.mErrorCode = IncredistResult.STATUS_INTERRUPTED;
                }

                if (notifyLatch.mErrorCode == IncredistResult.STATUS_SUCCESS) {

                    byte[] data = mResponse.getData();
                    IncredistResult result = firstCommand.parseResponse(mResponse);

                    FLog.d(TAG, String.format(Locale.JAPANESE, "receive success. data:%s iterator:%s", LogUtil.hexString(data), iterator.hasNext()));
                    // 最終データの場合、responseをアプリに返却
                    if (!iterator.hasNext()) {
                        return result;
                    } else {
                        mResponse.clear();
                    }
                } else {
                    return new IncredistResult(notifyLatch.mErrorCode);
                }
            }
        }
        return new IncredistResult(IncredistResult.STATUS_SUCCESS);
    }

    /**
     * 送受信用の characteristic を取得.
     * 取得済みの場合はすぐに return する
     */
    private void findCharacteristics() {
        if (mWriteCharacteristic != null && mNotifyCharacteristic != null) {
            return;
        }

        BluetoothGattConnection connection = mConnection;

        if (connection == null) {
            return;
        }
        BluetoothGattService sendService = connection.findService(IncredistConstants.FS_INCREDIST_SEND_SERVICE_UUID_FULL);
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

        connection.registerNotify(mNotifyCharacteristic, (success) -> {
        }, (errorCode, failure) -> {
        }, (notify) -> {
            synchronized (mResponse) {

                // onCharactaristicChangedが発生した時の処理
                // BLE notify 受信時処理 : mResponse に append する
                FLog.d(TAG, String.format(Locale.JAPANESE, "receive notify %d %s", notify.getValue().length, LogUtil.hexString(notify.getValue())));
                mResponse.appendData(notify.getValue());

                // 受信データがこれ以上存在しないと判断した場合、受信完了を通知
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
     * <p>
     * キャンセルできるかどうかは MFiCommand によって異なる
     * キャンセルできないコマンドの場合は STATUS_NOT_CANCELLABLE を返す
     * <p>
     * キャンセルできるコマンドであっても、送信 / 受信待ち処理には、
     * - 送信前
     * - 送信中
     * - 受信待ち
     * - 受信中
     * - 受信完了後
     * の状態がある。送信前の場合は mCancelling != null をチェックし、
     * 受信待ちの場合は mResponse の notify を受け取ってそれぞれ countdown するので
     * キャンセル成功する。
     * <p>
     * 送信中の場合はコマンド途中で停止させることはせずに送信完了まで一旦待つ
     * <p>
     * 受信中(すでにMFiパケットの一部を受け取っている)場合には
     * フラグを立てて、MFiパケットの残りを受信した際にキャンセル済みの
     * コマンドについてはコールバックを呼び出さない。
     * <p>
     * このメソッドは sendCommand とは別のスレッドで実行する必要がある
     *
     * @return キャンセル成功した場合は STATUS_SUCCESS, 失敗した場合はエラー結果を含む IncredistResult オブジェクト
     */
    @Override
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
            boolean res = mCancelling.await(CANCEL_TIMEOUT, TimeUnit.MILLISECONDS);

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
    @Override
    public void release() {
        mConnection = null;
        mWriteCharacteristic = null;
        mNotifyCharacteristic = null;
    }
}
