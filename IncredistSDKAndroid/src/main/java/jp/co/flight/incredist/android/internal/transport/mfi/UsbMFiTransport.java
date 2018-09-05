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

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.util.FLog;
import jp.co.flight.incredist.android.internal.util.LogUtil;

public class UsbMFiTransport implements MFiTransport {
    private static final String TAG = "UsbMFiTransport";
    private static final int MAX_PACKET_LENGTH = 64;
    private static final long USB_TIMEOUT = 1000;

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

    private MFiResponse mResponse = new MFiResponse();
    private MFiCommand mCommand = null;

    public UsbMFiTransport(@NonNull UsbDeviceConnection connection, @NonNull UsbInterface usbInterface) {
        mConnection = connection;
        mUsbInterface = usbInterface;

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
        //TODO NPE対策が必要
        queueRequest(mReceiveRequest, mReceiveBuffer);

        if (commandList == null || commandList.length == 0) {
            return new IncredistResult(IncredistResult.STATUS_INVALID_COMMAND);
        }

        MFiCommand firstCommand = commandList[0];
        mCommand = firstCommand;
        long startTime = System.currentTimeMillis();
        FLog.d(TAG, String.format("sendCommand %s", firstCommand.getClass().getSimpleName()));

        UsbRequest request = sendRequests(commandList);
        byte[] buf = new byte[MAX_PACKET_LENGTH];

        mResponse.clear();

        synchronized (mResponse) {
            FLog.d(TAG, "sendCommand wait receive");
            boolean continueReceive;
            do {
                continueReceive = false;

                if (request == mReceiveRequest) {
                    mReceiveBuffer.flip();
                    int length = mReceiveBuffer.remaining();
                    mReceiveBuffer.get(buf, 0, length);
                    FLog.d(TAG, String.format("sendCommand received length:%d data: %s", length, LogUtil.hexString(buf, 0, length)));
                    mResponse.appendData(buf, 0, length);

                    queueRequest(mReceiveRequest, mReceiveBuffer);
                } else if (request != null) {
                    FLog.d(TAG, String.format(Locale.US, "unknown request endpoint:%d", request.getEndpoint().getEndpointNumber()));
                }

                do {
                    FLog.d(TAG, "sendCommand requestWait");
                    request = mConnection.requestWait();
                    FLog.d(TAG, "sendCommand requestWait end");

                    if (request == mReceiveRequest) {
                        mReceiveBuffer.flip();
                        int length = mReceiveBuffer.remaining();
                        mReceiveBuffer.get(buf, 0, length);
                        FLog.d(TAG, String.format("sendCommand received data: %s", LogUtil.hexString(buf, 0, length)));
                        mResponse.appendData(buf, 0, length);
                    } else if (request == null) {
                        // 受信エラー
                        break;
                    } else {
                        FLog.d(TAG, String.format(Locale.US, "unknown request endpoint:%d", request.getEndpoint().getEndpointNumber()));
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

        return new IncredistResult(IncredistResult.STATUS_FAILURE);
    }

    private void queueRequest(UsbRequest request, ByteBuffer buffer) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            FLog.d(TAG, String.format(Locale.US, "queue endpoint:%d with length", request.getEndpoint().getEndpointNumber()));
            if (request.queue(buffer, MAX_PACKET_LENGTH)) {
                FLog.d(TAG, "queue success");
            } else {
                FLog.d(TAG, "queue failed");
            }
        } else {
            FLog.d(TAG, String.format(Locale.US, "queue endpoint:%d without length", request.getEndpoint().getEndpointNumber()));
            if (request.queue(buffer)) {
                FLog.d(TAG, "queue success");
            } else {
                FLog.d(TAG, "queue failed");
            }
        }
    }

    private UsbRequest sendRequests(MFiCommand[] commandList) {
        FLog.d(TAG, String.format(Locale.US, "sendRequests %d", commandList.length));
        for (MFiCommand command : commandList) {
            int count = command.getPacketCount(MAX_PACKET_LENGTH);

            FLog.d(TAG, String.format(Locale.US, "sendRequests count %d", count));
            for (int i = 0; i < count; i++) {
                byte[] data = command.getValueData(i, MAX_PACKET_LENGTH);
                mSendBuffer.put(data);
                queueRequest(mSendRequest, mSendBuffer);

                //TODO タイムアウトパラメータが API26 以上
                FLog.d(TAG, "sendRequests requestWait");
                UsbRequest request = mConnection.requestWait();
                FLog.d(TAG, "sendRequests requestWait end");

                if (request != mSendRequest) {
                    FLog.d(TAG, "sendRequests request is not sendRequest");
                    return request;
                }
            }
        }

        FLog.d(TAG, "sendRequests completed");

        // 正常に送信完了した場合
        return null;
    }

    @Override
    public IncredistResult cancel() {
        return null;
    }

    @Override
    public void release() {
        UsbDeviceConnection connection = mConnection;
        if (connection != null) {
            connection.releaseInterface(mUsbInterface);
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
