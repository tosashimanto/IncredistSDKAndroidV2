package jp.co.flight.incredist.android.model;

/**
 * 決済時のクレジットカード種別
 */
@SuppressWarnings("unused")
public enum CreditCardType {
    // CHECKSTYLE:OFF MagicNumber
    MSR(1 << 7),
    ContactEMV(1 << 6),
    ContactlessEMV(1 << 2);

    private final int mValue;

    CreditCardType(int value) {
        mValue = value;
    }

    public byte getValue() {
        return (byte) (mValue & 0xff);
    }
    // CHECKSTYLE:ON MagicNumber
}
