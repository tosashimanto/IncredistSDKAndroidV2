package jp.co.flight.incredist.android.model;

/**
 * EMV カーネルに指定する暗号化 TagType
 *
 * scanCreditCard のパラメータに指定する際、CreditCardType.MSR を指定する場合は AllTag を指定する必要がある
 */
@SuppressWarnings("unused")
public enum EmvTagType {
    // CHECKSTYLE:OFF MagicNumber
    AllTag(0),
    OnlyTag57(1);

    private final int mValue;

    EmvTagType(int value) {
        mValue = value;
    }

    public byte getValue() {
        return (byte) (mValue & 0xff);
    }
    // CHECKSTYLE:ON MagicNumber
}
