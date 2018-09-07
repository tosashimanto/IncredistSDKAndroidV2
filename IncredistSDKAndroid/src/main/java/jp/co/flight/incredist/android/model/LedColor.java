package jp.co.flight.incredist.android.model;

/**
 * LED色定義
 */
@SuppressWarnings("unused")
public enum LedColor {
    BLUE(0x31),
    YELLOW(0x32),
    GREEN(0x33),
    RED(0x34),
    ALL(0x35),
    NONE(0x36);

    private final int mValue;

    LedColor(int value) {
        mValue = value;
    }

    public byte getValue() {
        return (byte) mValue;
    }

    public static LedColor getEnum(int value) {
        for (LedColor ledColor : LedColor.values()) {
            if (ledColor.getValue() == value) {
                return ledColor;
            }
        }

        return null;
    }
}
