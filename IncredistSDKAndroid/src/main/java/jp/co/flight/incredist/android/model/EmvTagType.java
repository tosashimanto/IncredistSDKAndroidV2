package jp.co.flight.incredist.android.model;

/**
 * EMV カーネルに指定する暗号化 TagType
 */
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
