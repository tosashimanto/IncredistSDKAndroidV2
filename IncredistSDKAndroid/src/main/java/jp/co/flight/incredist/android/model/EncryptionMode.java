package jp.co.flight.incredist.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 暗号化モード設定パラメータクラス
 */
@SuppressWarnings("unused")
public class EncryptionMode implements Parcelable {
    public enum CipherMethod {
        DUKTPTDES(1),            // DUKTP TDES
        DUKTPAES(2),             // DUKTP AES
        FixedTDES(3),            // Fixed key TDES
        FixedAES128(4),          // Fixed key AES128
        FixedAES256(5);          // Fixed key AES256

        private final byte mValue;

        CipherMethod(int value) {
            mValue = (byte) value;
        }

        public byte getValue() {
            return mValue;
        }
    }

    public enum BlockCipherMode {
        ECB(1),            // 暗号ブックモード
        CBC(2);            // 暗号文ブロック連鎖モード

        private final byte mValue;

        BlockCipherMode(int value) {
            mValue = (byte) value;
        }

        public byte getValue() {
            return mValue;
        }
    }

    public enum DsConstant {
        PINEncryption(1),                    // PIN Encryption
        MessageAuthRequest(2),               // Message Authentication, request or both ways
        MessageAuthResponse(3),              // Message Authentication, response
        DataEncryptionRequest(4),            // Data Encryption, request or both ways
        DataEncryptionResponse(5);           // Data Encryption, response

        private final byte mValue;

        DsConstant(int value) {
            mValue = (byte) value;
        }

        public byte getValue() {
            return mValue;
        }
    }

    public enum PaddingMode {
        FixedData(0),
        PKCS5(1);

        private final byte mValue;

        PaddingMode(int value) {
            mValue = (byte) value;
        }

        public byte getValue() {
            return mValue;
        }
    }

    private byte mKeyNumber;
    private CipherMethod mCipherMethod;
    private BlockCipherMode mBlockCipherMode;
    private DsConstant mDsConstant;
    private PaddingMode mPaddingMode;
    private byte mPaddingValue;
    private boolean mIsPin;

    /**
     * コンストラクタ
     *
     * @param keyNumber       キー番号
     * @param cipherMethod    暗号化方法
     * @param blockCipherMode ブロック暗号化モード
     * @param dsConstant      キー種別
     * @param paddingMode     パディングモード
     * @param paddingValue    パディング値
     * @param isPin           磁気入力の後PIN入力するかどうか
     */
    public EncryptionMode(byte keyNumber, CipherMethod cipherMethod, BlockCipherMode blockCipherMode,
                          DsConstant dsConstant, PaddingMode paddingMode, byte paddingValue, boolean isPin) {
        mKeyNumber = keyNumber;
        mCipherMethod = cipherMethod;
        mBlockCipherMode = blockCipherMode;
        mDsConstant = dsConstant;
        mPaddingMode = paddingMode;
        mPaddingValue = paddingValue;
        mIsPin = isPin;
    }

    public void setKeyNumber(byte keyNumber) {
        mKeyNumber = keyNumber;
    }

    public void setCipherMethod(CipherMethod cipherMethod) {
        mCipherMethod = cipherMethod;
    }

    public void setBlockCipherMode(BlockCipherMode blockCipherMode) {
        mBlockCipherMode = blockCipherMode;
    }

    public void setDsConstant(DsConstant dsConstant) {
        mDsConstant = dsConstant;
    }

    public void setPaddingMode(PaddingMode paddingMode) {
        mPaddingMode = paddingMode;
    }

    public void setPaddingValue(byte paddingValue) {
        mPaddingValue = paddingValue;
    }

    public void setPin(boolean pin) {
        mIsPin = pin;
    }

    /**
     * キー番号を取得します
     *
     * @return キー番号
     */
    public byte getKeyNumber() {
        return mKeyNumber;
    }

    /**
     * 暗号化方法を取得します
     *
     * @return 暗号化方法
     */
    public CipherMethod getCipherMethod() {
        return mCipherMethod;
    }

    /**
     * ブロック暗号化モードを取得します
     *
     * @return ブロック暗号化モード
     */
    public BlockCipherMode getBlockCipherMode() {
        return mBlockCipherMode;
    }

    /**
     * トランザクションキー種別を取得します
     *
     * @return キー種別
     */
    public DsConstant getDsConstant() {
        return mDsConstant;
    }

    /**
     * パディングモードを取得します
     *
     * @return パディングモード
     */
    public PaddingMode getPaddingMode() {
        return mPaddingMode;
    }

    /**
     * パディング値を取得します
     *
     * @return パディング値
     */
    public byte getPaddingValue() {
        return mPaddingValue;
    }

    /**
     * PIN入力モードを取得します
     *
     * @return PIN入力モード
     */
    public boolean isPin() {
        return mIsPin;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.mKeyNumber);
        dest.writeInt(this.mCipherMethod == null ? -1 : this.mCipherMethod.ordinal());
        dest.writeInt(this.mBlockCipherMode == null ? -1 : this.mBlockCipherMode.ordinal());
        dest.writeInt(this.mDsConstant == null ? -1 : this.mDsConstant.ordinal());
        dest.writeInt(this.mPaddingMode == null ? -1 : this.mPaddingMode.ordinal());
        dest.writeByte(this.mPaddingValue);
        dest.writeByte(this.mIsPin ? (byte) 1 : (byte) 0);
    }

    protected EncryptionMode(Parcel in) {
        this.mKeyNumber = in.readByte();
        int tmpCipherMethod = in.readInt();
        this.mCipherMethod = tmpCipherMethod == -1 ? null : CipherMethod.values()[tmpCipherMethod];
        int tmpBlockCipherMode = in.readInt();
        this.mBlockCipherMode = tmpBlockCipherMode == -1 ? null : BlockCipherMode.values()[tmpBlockCipherMode];
        int tmpDsConstant = in.readInt();
        this.mDsConstant = tmpDsConstant == -1 ? null : DsConstant.values()[tmpDsConstant];
        int tmpPaddingMode = in.readInt();
        this.mPaddingMode = tmpPaddingMode == -1 ? null : PaddingMode.values()[tmpPaddingMode];
        this.mPaddingValue = in.readByte();
        this.mIsPin = in.readByte() != 0;
    }

    public static final Parcelable.Creator<EncryptionMode> CREATOR = new Parcelable.Creator<EncryptionMode>() {
        @Override
        public EncryptionMode createFromParcel(Parcel source) {
            return new EncryptionMode(source);
        }

        @Override
        public EncryptionMode[] newArray(int size) {
            return new EncryptionMode[size];
        }
    };
}
