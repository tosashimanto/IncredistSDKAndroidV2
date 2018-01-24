package jp.co.flight.incredist.android.model;

import jp.co.flight.incredist.android.internal.controller.result.MagCardResult;

/**
 * 磁気カード読み取りデータ
 */
public class MagCard {
    public enum Type {
        None(0),
        JIS1(1),
        JIS2(2),
        Unknown(3);

        private final byte mValue;

        Type(int value) {
            mValue = (byte) value;
        }

        public byte getValue() {
            return mValue;
        }
    }

    private Type mCardType;
    private byte[] mKsn;
    private byte[] mTrack1;
    private byte[] mTrack2;
    private String mMaskedCardNo;
    private String mExpirationDate;
    private String mServiceCode;
    private String mCardHolderName;

    //TODO MagCardResult.toMagCard に書き直す
    public MagCard(MagCardResult result) {
        mCardType = result.cardType;
        mKsn = result.ksn;
        mTrack1 = result.track1;
        mTrack2 = result.track2;
        mMaskedCardNo = result.maskedCardNo;
        mExpirationDate = result.expirationDate;
        mServiceCode = result.serviceCode;
        mCardHolderName = result.cardHolderName;
    }

    protected MagCard(MagCard magCard) {
        mCardType = magCard.mCardType;
        mKsn = magCard.mKsn;
        mTrack1 = magCard.mTrack1;
        mTrack2 = magCard.mTrack2;
        mMaskedCardNo = magCard.mMaskedCardNo;
        mExpirationDate = magCard.mExpirationDate;
        mServiceCode = magCard.mServiceCode;
        mCardHolderName = magCard.mCardHolderName;
    }

    public Type getCardType() {
        return mCardType;
    }

    public byte[] getKsn() {
        return mKsn;
    }

    public byte[] getTrack1() {
        return mTrack1;
    }

    public byte[] getTrack2() {
        return mTrack2;
    }

    public String getMaskedCardNo() {
        return mMaskedCardNo;
    }

    public String getExpirationDate() {
        return mExpirationDate;
    }

    public String getServiceCode() {
        return mServiceCode;
    }

    public String getCardHolderName() {
        return mCardHolderName;
    }
}
