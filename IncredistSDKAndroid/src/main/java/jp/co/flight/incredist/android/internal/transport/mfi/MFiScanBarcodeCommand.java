package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * 磁気カード読み取りコマンド(mj2)
 */
public class MFiScanBarcodeCommand extends MFiCommand {
    private static final byte[] B_HEADER = new byte[]{'b'};

    private final long mTimeout;

    public MFiScanBarcodeCommand(long timeout) {
        super(B_HEADER);
        mTimeout = timeout;
    }

    @Override
    public long getResponseTimeout() {
        return mTimeout;
    }

    @Override
    public boolean cancelable() {
        return true;
    }

    @Override
    public void onCancelled(MFiTransport transport) {
        onCommonCancelled(transport);
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        byte[] data = response.getData();

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
