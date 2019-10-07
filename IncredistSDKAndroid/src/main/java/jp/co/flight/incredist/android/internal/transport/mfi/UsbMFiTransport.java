package jp.co.flight.incredist.android.internal.transport.mfi;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.util.FLog;
import jp.co.flight.incredist.android.internal.util.LogUtil;

/**
 * USB - MFi 版 Incredist との MFi パケット通信を行うユーティリティクラス.
 * このクラスのメソッドはバックグラウンドスレッドで実行される前提なので同期処理として実装する.
 */
public class UsbMFiTransport implements MFiTransport {
    private static final String TAG = "UsbMFiTransport";
    private static final int MAX_PACKET_LENGTH = 64;
    private static final long USB_TIMEOUT = 5000;
    private static final long SLEEP_INTERVAL = 100;

    private final ExecutorService mExecutor;

    @Nullable
    private UsbDeviceConnection mConnection;
    @Nullable
    private UsbInterface mUsbInterface;

    private UsbEndpoint mSendEndpoint;
//    private UsbRequest mSendRequest;

    private UsbEndpoint mReceiveEndpoint;
//    private UsbRequest mReceiveRequest;

    private Future<UsbRequest> mFuture = null;
    private boolean mIsReleasing = false ;

    // ANDROID_TFPS-1196
    private static final Object mLockObj = new Object();
    /**
     * コンストラクタ.
     *
     * @param connection   UsbDeviceConnection オブジェクト
     * @param usbInterface UsbInterface オブジェクト
     */
    public UsbMFiTransport(@NonNull UsbDeviceConnection connection, @NonNull UsbInterface usbInterface) {
        mConnection = connection;
        mUsbInterface = usbInterface;

        mExecutor = Executors.newCachedThreadPool();
        mIsReleasing = false;
        FLog.d(TAG, String.format(Locale.US, "proto:%d endpoints:%d", usbInterface.getInterfaceProtocol(), usbInterface.getEndpointCount()));
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = usbInterface.getEndpoint(i);

            FLog.d(TAG, String.format(Locale.US, "endpoint:%d type:%d direction:%d", endpoint.getEndpointNumber(), endpoint.getType(), endpoint.getDirection()));
            if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    mReceiveEndpoint = endpoint;
//                    mReceiveRequest = new UsbRequest();
//                    mReceiveRequest.initialize(mConnection, mReceiveEndpoint);
                } else if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    mSendEndpoint = endpoint;
//                    mSendRequest = new UsbRequest();
//                    mSendRequest.initialize(mConnection, mSendEndpoint);

                }
            }
        }

        if (mConnection.claimInterface(usbInterface, true)) {
            FLog.d(TAG, "claimInterface success");
        } else {
            FLog.d(TAG, "claimInterface failed");
        }
    }

    @WorkerThread
    @Override
    public IncredistResult sendCommand(MFiCommand... commandList) {
        FLog.d(TAG,"");
        //ANDROID_TFPS-1127 クラッシュ抑止
        if( mIsReleasing ) {
            FLog.d(TAG,"");
            return new IncredistResult(IncredistResult.STATUS_RELEASED );
        }
        if (commandList == null || commandList.length == 0) {
            FLog.d(TAG,"");
            return new IncredistResult(IncredistResult.STATUS_INVALID_COMMAND);
        }
        synchronized (mLockObj) {
            MFiCommand firstCommand = commandList[0];
            MFiCommand command = firstCommand;
            long startTime = System.currentTimeMillis();
            FLog.d(TAG, String.format("sendCommand %s", firstCommand.getClass().getSimpleName()));
            if (!sendRequests(commandList)) {
                FLog.d(TAG, "");
                return new IncredistResult(IncredistResult.STATUS_SEND_TIMEOUT);
            }
            if (firstCommand.getResponseTimeout() == 0) {
                // 応答パケットがない場合は guardWait だけ待機して、 MFiNoResponse を結果とする
                try {
                    FLog.d(TAG, "");
                    Thread.sleep(firstCommand.getGuardWait());
                } catch (InterruptedException ex) {
                    // ignore.
                }
                command = null;
                FLog.d(TAG, String.format("sendCommand has no response %s", firstCommand.getClass().getSimpleName()));
                return firstCommand.parseResponse(new MFiNoResponse());
            } else {
                FLog.d(TAG, String.format("sendCommand recv packet(s) for %s", firstCommand.getClass().getSimpleName()));
                boolean continueReceive;
                UsbRequest receiveRequest = new UsbRequest();
                receiveRequest.initialize(mConnection, mReceiveEndpoint);
                do {
                    MFiResponse response = new MFiResponse();
                    response.clear();
                    do {
                        FLog.d(TAG, "");
                        continueReceive = false;
                        long timeout = firstCommand.getResponseTimeout();
                        if (timeout <= 0) {
                            timeout = USB_TIMEOUT;
                        }
                        ByteBuffer receiveBuffer = ByteBuffer.allocate(MAX_PACKET_LENGTH);
                        receiveBuffer.clear();
                        for (int n = 0; n < MAX_PACKET_LENGTH; n++) {
                            receiveBuffer.put((byte) 0x00);
                        }
                        receiveBuffer.clear();

                        boolean isRequested = false;
                        UsbRequest request = null;
                        try {
                            FLog.d(TAG, "sendCommand " + firstCommand.getClass().getSimpleName());
                            FLog.d(TAG, "requestWait(" + timeout + ")");
                            while (true) {
                                FLog.d(TAG, "");
//                            if( mIsReleasing ) {
//                                FLog.d(TAG,"");
//                                return new IncredistResult(IncredistResult.STATUS_RELEASED );
//                            }
//                            synchronized (mLockObj) {
//                                if( mIsReleasing ) {
//                                    FLog.d(TAG,"");
//                                    return new IncredistResult(IncredistResult.STATUS_RELEASED );
//                                }
//                                synchronized (mLockObj) {
                                if (isRequested == false) {
                                    FLog.d(TAG, "");
                                    queueRequest(receiveRequest, receiveBuffer);
                                    isRequested = true;
                                }
                                if ((request = requestWait(timeout)) == receiveRequest) {
                                    FLog.d(TAG, "");
                                    break;
                                }
//                                }
                                if (request == null) {
                                    FLog.d(TAG, "");
                                    break;
                                }
                                try {
                                    FLog.d(TAG, "");
                                    Thread.sleep(SLEEP_INTERVAL);
                                } catch (InterruptedException e) {
                                    FLog.d(TAG, "InterruptedException:" + e.getMessage());
                                }
//                            }
                            }
                        } catch (TimeoutException ex) {
                            FLog.d(TAG, "TimeoutException");
                            receiveRequest.cancel();
                            return new IncredistResult(IncredistResult.STATUS_TIMEOUT);
                        }

                        if (request == receiveRequest) {
                            FLog.d(TAG, "");
                            receiveBuffer.flip();
                            int length = receiveBuffer.remaining();
                            if (length == 0) {
                                // USB の受信データが 0byte の場合は次のパケットを待つ
                                FLog.d(TAG, "");
                                continue;
                            }

                            byte[] buf = new byte[MAX_PACKET_LENGTH];
                            receiveBuffer.get(buf, 0, length);
                            FLog.d(TAG, String.format(Locale.US, "sendCommand received length:%d data: %s", length, LogUtil.hexString(buf, 0, length)));
                            response.appendData(buf, 0, length);
                        } else if (request == null) {
                            // 受信エラー
                            FLog.d(TAG, "Error requestWait returns null");
                            break;
                        } else {
                            FLog.d(TAG, String.format(Locale.US, "unknown request endpoint:%d", request.getEndpoint().getEndpointNumber()));
                        }
                    } while (response.isEmpty() || response.needMoreData());

                    if (response.isValid()) {
                        FLog.d(TAG, "recv valid packet: " + LogUtil.hexString(response.getData()));
                        IncredistResult result = firstCommand.parseResponse(response.copyInstance());
                        if (result.status == IncredistResult.STATUS_CONTINUE_MULTIPLE_RESPONSE) {
                            // 継続するパケットがある場合はパケット情報をクリアして次のデータを待つ
                            response.clear();
                            continueReceive = true;
                        } else {
                            try {
                                FLog.d(TAG, "");
                                Thread.sleep(firstCommand.getGuardWait());
                            } catch (InterruptedException ex) {
                                // ignore.
                            }
                            long real = System.currentTimeMillis() - startTime;
                            FLog.d(TAG, String.format(Locale.JAPANESE, "sendCommand result:%d wait:%d real:%d %s", result.status, firstCommand.getResponseTimeout(), real, command.getClass().getSimpleName()));

                            command = null;
                            if (result.status == IncredistResult.STATUS_SUCCESS) {
                                FLog.d(TAG, "");
                                response.clear();
                            }
                            FLog.d(TAG, "");
                            return result;
                        }
                    }

                } while (continueReceive);
            }
        }
        return new IncredistResult(IncredistResult.STATUS_FAILURE);
    }

    private void queueRequest(UsbRequest request, ByteBuffer buffer) {
        //ANDROID_TFPS-1127 クラッシュ抑止
        FLog.d(TAG, "");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            FLog.d(TAG, String.format(Locale.US, "queue endpoint:%d with length", request.getEndpoint().getEndpointNumber()));
            if (!request.queue(buffer, MAX_PACKET_LENGTH)) {
                FLog.d(TAG, "queue failed");
            }
        } else {
            FLog.d(TAG, String.format(Locale.US, "queue endpoint:%d without length", request.getEndpoint().getEndpointNumber()));
            if (!request.queue(buffer)) {
                FLog.d(TAG, "queue failed");
            }
        }
    }

    @SuppressWarnings("Convert2MethodRef")
    private UsbRequest requestWait(long timeout) throws TimeoutException {
        FLog.d(TAG,"");
        UsbDeviceConnection connection = mConnection;
        if (connection == null) {
            FLog.d(TAG,"");
            return null;
        }
        // ANDROID_GMO-595　Long.MAX_VALUEが指定された場合はタイムアウト無しとする
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            //ANDROID_TFPS-1127 クラッシュ抑止
            mFuture = mExecutor.submit(() -> connection.requestWait());
            try {
                if (timeout == Long.MAX_VALUE) {
                    FLog.d(TAG,"");
                    return mFuture.get();
                } else {
                    FLog.d(TAG,"");
                    return mFuture.get(timeout, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException | ExecutionException e) {
                FLog.d(TAG,"");
                return null;
            } catch (CancellationException c) {
                FLog.d(TAG, "Futrue get Canceled");
                return null;
            }
        } else {
            if (timeout == Long.MAX_VALUE) {
                return connection.requestWait();
            } else {
                return connection.requestWait(timeout);
            }
        }
    }

    private boolean sendRequests(MFiCommand[] commandList) {
        FLog.d(TAG, String.format(Locale.US, "sendRequests commandList.length:%d", commandList.length));
        //ANDROID_TFPS-1127 クラッシュ抑止
        if( mIsReleasing ) {
            FLog.d(TAG,"");
            return false ;
        }
        UsbRequest sendRequest = new UsbRequest();
        sendRequest.initialize(mConnection, mSendEndpoint);

        for (MFiCommand command : commandList) {
            int count = command.getPacketCount(MAX_PACKET_LENGTH);

            FLog.d(TAG, String.format(Locale.US, "sendRequests packet count:%d", count));
            for (int i = 0; i < count; i++) {
                byte[] data = command.getValueData(i, MAX_PACKET_LENGTH);
                FLog.d(TAG, "sendRequest command[" + i + "] sendBuffer data=" + LogUtil.hexString(data));

                //バッファの0x00クリア。
                //clearメソッドはポインタ位置を０に戻すだけでバッファ内部はクリアしないことに注意
                ByteBuffer sendBuffer = ByteBuffer.allocate(MAX_PACKET_LENGTH);
                sendBuffer.clear();
                for (int n = 0; n < MAX_PACKET_LENGTH; n++) {
                    sendBuffer.put((byte) 0x00);
                }
                //バッファの0x00クリアによりポインタ位置がMAX_PACKET_LENGTHに移動したので改めてポインタ位置を０に戻す。
                sendBuffer.clear();
                sendBuffer.put(data);

                boolean isRequested = false ;
                UsbRequest request = null;
                try {
                    while (true) {
                        FLog.d(TAG,"");
//                        if( mIsReleasing ) {
//                            FLog.d(TAG,"");
//                            return false ;
//                        }
//                        synchronized (mLockObj) {
//                            if( mIsReleasing ) {
//                                FLog.d(TAG,"");
//                                return false ;
//                            }
//                            synchronized (mLockObj) {
                                if (isRequested == false) {
                                    FLog.d(TAG, "");
                                    queueRequest(sendRequest, sendBuffer);
                                    isRequested = true;
                                }
                                if ((request = requestWait(USB_TIMEOUT)) == sendRequest) {
                                    FLog.d(TAG, "");
                                    break;
                                }
//                            }
                            if (request == null) {
                                FLog.d(TAG,"");
                                return false;
                            }
                            try {
                                FLog.d(TAG, "sendRequests request is not sendRequest");
                                Thread.sleep(SLEEP_INTERVAL);
                            } catch (InterruptedException e) {
                                FLog.d(TAG, "InterruptedException:" + e.getMessage());
                            }
                        }
//                    }
                } catch (TimeoutException ex) {
                    FLog.d(TAG, "TimeoutException");
                    sendRequest.cancel();
                    return false;
                }
            }
        }
        // 正常に送信完了した場合
        FLog.d(TAG, "sendRequests completed");
        return true;
    }

    //TODO キャンセル処理未実装(電子マネーアプリでは必要ないはず)
    @Override
    public IncredistResult cancel() {
        return null;
    }

    @Override
    public void release() {
        FLog.d(TAG,"");
        mIsReleasing = true;
        if (mFuture != null) {
            FLog.d(TAG,"");
            mFuture.cancel(true);
        }

        synchronized (mLockObj) {        UsbDeviceConnection connection = mConnection;
        if (connection != null) {
            FLog.d(TAG,"");
            UsbInterface usbInterface = mUsbInterface;
            if (usbInterface != null) {
                connection.releaseInterface(usbInterface);
            }
            connection.close();

        }
            FLog.d(TAG,"");
//            mSendRequest.cancel();
//            mReceiveRequest.cancel();
            mFuture = null;
            mConnection = null;
            mUsbInterface = null;
        }
        FLog.d(TAG,"");
    }

    @Override
    public boolean isBusy() {
        return false;
    }
}
