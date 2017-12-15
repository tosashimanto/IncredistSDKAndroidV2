package jp.co.flight.incredist.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * PIND コマンド用の設定値 POJO
 */
public class PinEntryDParam implements Parcelable {
    private int mLength;
    private int mMaskMode;
    private int mPinMode;
    private int mPosition;
    private int mDirection;

    public PinEntryDParam() {
    }

    public PinEntryDParam(int length, int maskMode, int pinMode, int position, int direction) {
        mLength = length;
        mMaskMode = maskMode;
        mPinMode = pinMode;
        mPosition = position;
        mDirection = direction;
    }

    public int getLength() {
        return mLength;
    }

    public void setLength(int length) {
        mLength = length;
    }

    public int getMaskMode() {
        return mMaskMode;
    }

    public void setMaskMode(int maskMode) {
        mMaskMode = maskMode;
    }

    public int getPinMode() {
        return mPinMode;
    }

    public void setPinMode(int pinMode) {
        mPinMode = pinMode;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public int getDirection() {
        return mDirection;
    }

    public void setDirection(int direction) {
        mDirection = direction;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mLength);
        dest.writeInt(this.mMaskMode);
        dest.writeInt(this.mPinMode);
        dest.writeInt(this.mPosition);
        dest.writeInt(this.mDirection);
    }

    protected PinEntryDParam(Parcel in) {
        this.mLength = in.readInt();
        this.mMaskMode = in.readInt();
        this.mPinMode = in.readInt();
        this.mPosition = in.readInt();
        this.mDirection = in.readInt();
    }

    public static final Parcelable.Creator<PinEntryDParam> CREATOR = new Parcelable.Creator<PinEntryDParam>() {
        @Override
        public PinEntryDParam createFromParcel(Parcel source) {
            return new PinEntryDParam(source);
        }

        @Override
        public PinEntryDParam[] newArray(int size) {
            return new PinEntryDParam[size];
        }
    };
}
