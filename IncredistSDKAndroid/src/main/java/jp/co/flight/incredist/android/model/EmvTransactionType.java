package jp.co.flight.incredist.android.model;

/**
 * EMV カーネルに指定する TransactionType
 */
@SuppressWarnings("unused")
public enum EmvTransactionType {
    // CHECKSTYLE:OFF MagicNumber
    Purchase(0),
    Refund(0x20),
    Deposit(0x21);

    private final int mValue;

    EmvTransactionType(int value) {
        mValue = value;
    }

    public byte getValue() {
        return (byte) (mValue & 0xff);
    }
    // CHECKSTYLE:ON MagicNumber
}
