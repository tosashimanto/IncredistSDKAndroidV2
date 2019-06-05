package jp.co.flight.incredist.android.model;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * PIN入力パラメータと結果
 */
@SuppressWarnings("unused")
public class PinEntry implements Parcelable {

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

    /* 単独クラス（PinEntryResult）に移行
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
    */

    //値定義
    private Type     mType ;
    private Mode     mMode ;
    private MaskMode mMaskMode ;

    //Getter/Setter
    public void setType(Type type) {
        mType = type;
    }
    public Type getType() {
        return mType ;
    }

    public void setMode(Mode mode) {
        mMode = mode;
    }
    public Mode getMode() {
        return mMode;
    }

    public void setMaskMode(MaskMode mode) {
        mMaskMode = mode;
    }
    public MaskMode getMaskMode() {
        return mMaskMode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType.ordinal() );
        dest.writeInt(this.mMode.ordinal() );
        dest.writeInt(this.mMaskMode.ordinal() );
    }

    public PinEntry() {
    }

    public PinEntry( Type type, Mode mode, MaskMode maskMode ) {
        mType = type;
        mMode = mode;
        mMaskMode = maskMode;
    }

    public PinEntry( byte type, byte mode, byte maskMode ) {
        for( Type x : Type.values() ) {
            if( x.getValue() == type ) {
                mType = x;
                break;
            }
        }
        for( Mode x : Mode.values() ) {
            if( x.getValue() == mode ) {
                mMode = x;
                break;
            }
        }
        for( MaskMode x : MaskMode.values() ) {
            if( x.getValue() == maskMode ) {
                mMaskMode = x;
                break;
            }
        }
    }

    protected PinEntry(Parcel in) {

        int type = in.readInt();
        this.mType = type == -1 ? null : Type.values()[type];
        int mode = in.readInt();
        this.mMode = type == -1 ? null : Mode.values()[mode];
        int maskMode = in.readInt();
        this.mMaskMode = type == -1 ? null : MaskMode.values()[maskMode];
    }

    public static final Parcelable.Creator<PinEntry> CREATOR = new Parcelable.Creator<PinEntry>() {
        @Override
        public PinEntry createFromParcel(Parcel source) {
            return new PinEntry(source);
        }

        @Override
        public PinEntry[] newArray(int size) {
            return new PinEntry[size];
        }
    };

}
