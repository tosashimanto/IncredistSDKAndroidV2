package jp.co.flight.incredist.android.model;

import jp.co.flight.incredist.android.internal.controller.result.PinEntryResult;

/**
 * PIN入力パラメータと結果
 */
public class PinEntry {
    public enum Type {
        PlainText(0),
        ISO9564(1),
        JMUPS(2);

        private final byte mValue;

        Type(int value) {
            mValue = (byte) value;
        }

        public byte getValue() {
            return mValue;
        }
    }

    public enum Mode {
        DebitScramble(0),
        Normal(1),
        EMV(2);

        private final byte mValue;

        Mode(int value) {
            mValue = (byte) value;
        }

        public byte getValue() {
            return mValue;
        }
    }

    public enum MaskMode {
        All(0),
        ExceptLastChar(1),
        None(2),
        NoneWithComma(3);

        private final byte mValue;

        MaskMode(int value) {
            mValue = (byte) value;
        }

        public byte getValue() {
            return mValue;
        }
    }

    public enum Alignment {
        Left(1),
        Right(2);

        private final byte mValue;

        Alignment(int value) {
            mValue = (byte) value;
        }

        public byte getValue() {
            return mValue;
        }
    }

    public static class Result {
        private final byte[] mPinData;
        private final byte[] mKsn;

        public Result(PinEntryResult result) {
            mPinData = result.pinData;
            mKsn = result.ksn;
        }

        public byte[] getPinData() {
            return mPinData;
        }

        public byte[] getKsn() {
            return mKsn;
        }
    }
}
