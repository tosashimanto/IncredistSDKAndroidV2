package jp.co.flight.incredist.android.model;

/**
 * 磁気カード読み取りデータ
 *
 * JIS1 の場合は　track1 のみが有効で track2 は存在しないので null が返却されます
 * JIS2 の場合は MaskedCardNo, ExpirationDate, ServiceCode, CardHolderName は存在しないので null が返却されます
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

    public MagCard(Type cardType, byte[] ksn, byte[] track1, byte[] track2, String maskedCardNo, String expirationDate, String serviceCode, String cardHolderName) {
        mCardType = cardType;
        mKsn = ksn;
        mTrack1 = track1;
        mTrack2 = track2;
        mMaskedCardNo = maskedCardNo;
        mExpirationDate = expirationDate;
        mServiceCode = serviceCode;
        mCardHolderName = cardHolderName;
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
