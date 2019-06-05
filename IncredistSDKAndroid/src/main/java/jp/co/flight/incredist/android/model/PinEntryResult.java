package jp.co.flight.incredist.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * PIN入力結果
 */
public class PinEntryResult extends IncredistResult {
    public final byte[] ksn;
    public final byte[] pinData;

    /**
     * コンストラクタ
     *
     * @param ksn     KSN
     * @param pinData PIN入力データ
     */
    public PinEntryResult(byte[] ksn, byte[] pinData) {
        super(IncredistResult.STATUS_SUCCESS);

        this.ksn = ksn;
        this.pinData = pinData;
    }

    public byte[] getPinData() {
        return pinData;
    }

    public byte[] getKsn() {
        return ksn;
    }
}
