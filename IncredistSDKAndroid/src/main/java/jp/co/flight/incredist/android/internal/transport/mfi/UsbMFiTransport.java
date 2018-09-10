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
    private static final long USB_TIMEOUT = 300;
    private final ExecutorService mExecutor;

    @Nullable
    private UsbDeviceConnection mConnection;
    @Nullable
    private UsbInterface mUsbInterface;

    @Nullable
    private UsbEndpoint mSendEndpoint;
    private UsbRequest mSendRequest;
    private ByteBuffer mSendBuffer = ByteBuffer.allocate(MAX_PACKET_LENGTH);

    @Nullable
    private UsbEndpoint mReceiveEndpoint;
    private UsbRequest mReceiveRequest;
    private ByteBuffer mReceiveBuffer = ByteBuffer.allocate(MAX_PACKET_LENGTH);

    private final MFiResponse mResponse = new MFiResponse();
    private MFiCommand mCommand = null;

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

        FLog.d(TAG, String.format(Locale.US, "proto:%d endpoints:%d", usbInterface.getInterfaceProtocol(), usbInterface.getEndpointCount()));
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = usbInterface.getEndpoint(i);

            FLog.d(TAG, String.format(Locale.US, "endpoint:%d type:%d direction:%d", endpoint.getEndpointNumber(), endpoint.getType(), endpoint.getDirection()));
            if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    mReceiveEndpoint = endpoint;
                    mReceiveRequest = new UsbRequest();
                    mReceiveRequest.initialize(mConnection, mReceiveEndpoint);
                } else if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    mSendEndpoint = endpoint;
                    mSendRequest = new UsbRequest();
                    mSendRequest.initialize(mConnection, mSendEndpoint);
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
        if (commandList == null || commandList.length == 0) {
            return new IncredistResult(IncredistResult.STATUS_INVALID_COMMAND);
        }

        MFiCommand firstCommand = commandList[0];
        mCommand = firstCommand;
        long startTime = System.currentTimeMillis();
        FLog.d(TAG, String.format("sendCommand %s", firstCommand.getClass().getSimpleName()));

        if (!sendRequests(commandList)) {
            return new IncredistResult(IncredistResult.STATUS_SEND_TIMEOUT);
        }
        byte[] buf = new byte[MAX_PACKET_LENGTH];

        mResponse.clear();

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
            synchronized (mResponse) {
                FLog.d(TAG, String.format("sendCommand recv packet(s) for %s", firstCommand.getClass().getSimpleName()));
                boolean continueReceive;
                do {
                    continueReceive = false;

                    do {
                        mReceiveBuffer.clear();
                        queueRequest(mReceiveRequest, mReceiveBuffer);
                        UsbRequest request;
                        try {
                            request = requestWait(USB_TIMEOUT);
                        } catch (TimeoutException ex) {
                            mReceiveRequest.cancel();
                            return new IncredistResult(IncredistResult.STATUS_TIMEOUT);
                        }

                        if (request == mReceiveRequest) {
                            mReceiveBuffer.flip();
                            int length = mReceiveBuffer.remaining();
                            if (length == 0) {
                                // USB の受信データが 0byte の場合は次のパケットを待つ
                                continue;
                            }

                            mReceiveBuffer.get(buf, 0, length);
                            FLog.d(TAG, String.format(Locale.US, "sendCommand received length:%d data: %s", length, LogUtil.hexString(buf, 0, length)));
                            mResponse.appendData(buf, 0, length);
                        } else if (request == null) {
                            // 受信エラー
                            break;
                        } else {
                            FLog.d(TAG, String.format(Locale.US, "unknown request endpoint:%d", request.getEndpoint().getEndpointNumber()));
                        }
                    } while (mResponse.isEmpty() || mResponse.needMoreData());

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
        }

        return new IncredistResult(IncredistResult.STATUS_FAILURE);
    }

    private void queueRequest(UsbRequest request, ByteBuffer buffer) {
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
        UsbDeviceConnection connection = mConnection;
        if (connection == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Future<UsbRequest> future = mExecutor.submit(() -> connection.requestWait());

            try {
                return future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
        } else {
            return connection.requestWait(timeout);
        }
    }

    private boolean sendRequests(MFiCommand[] commandList) {
        FLog.d(TAG, String.format(Locale.US, "sendRequests commandList.length:%d", commandList.length));
        for (MFiCommand command : commandList) {
            int count = command.getPacketCount(MAX_PACKET_LENGTH);

            FLog.d(TAG, String.format(Locale.US, "sendRequests packet count:%d", count));
            for (int i = 0; i < count; i++) {
                byte[] data = command.getValueData(i, MAX_PACKET_LENGTH);
                mSendBuffer.clear();
                mSendBuffer.put(data);
                queueRequest(mSendRequest, mSendBuffer);

                UsbRequest request;
                try {
                    request = requestWait(USB_TIMEOUT);
                } catch (TimeoutException ex) {
                    mSendRequest.cancel();
                    return false;
                }
                if (request != mSendRequest) {
                    FLog.d(TAG, "sendRequests request is not sendRequest");
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
        UsbDeviceConnection connection = mConnection;
        if (connection != null) {
            UsbInterface usbInterface = mUsbInterface;
            if (usbInterface != null) {
                connection.releaseInterface(usbInterface);
            }
            connection.close();
        }
        mConnection = null;
        mSendEndpoint = null;
        mReceiveEndpoint = null;
        mUsbInterface = null;
    }

    @Override
    public boolean isBusy() {
        return false;
    }
}
