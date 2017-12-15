package jp.co.flight.incredist.android.internal.controller.result;

import jp.co.flight.incredist.android.model.MagCard;

/**
 * 磁気カード読み取り結果
 */
public class MagCardResult extends IncredistResult {
    public MagCard.Type cardType;
    public byte[] ksn;
    public byte[] track1;
    public byte[] track2;
    public String maskedCardNo;
    public String expirationDate;
    public String serviceCode;
    public String cardHolderName;

    public MagCardResult() {
        super(IncredistResult.STATUS_SUCCESS);
    }
}
