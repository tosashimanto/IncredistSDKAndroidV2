package jp.co.flight.incredist.android.model;

/**
 * 暗号化モード設定用クラス
 */
public class EncryptionMode {
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

    public enum DSConstant {
        PINEncryption(1),                    // PIN Encryption
        MessageAuthRequest(2),               // Message Authentication, request or both ways
        MessageAuthResponse(3),              // Message Authentication, response
        DataEncryptionRequest(4),            // Data Encryption, request or both ways
        DataEncryptionResponse(5);           // Data Encryption, response

        private final byte mValue;

        DSConstant(int value) {
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
    private DSConstant mDSConstant;
    private PaddingMode mPaddingMode;
    private byte mPaddingValue;
    private boolean mIsPin;

    /**
     * コンストラクタ
     *
     * @param keyNumber キー番号
     * @param cipherMethod 暗号化方法
     * @param blockCipherMode ブロック暗号化モード
     * @param dsConstant キー種別
     * @param paddingMode パディングモード
     * @param paddingValue パディング値
     * @param isPin 磁気入力の後PIN入力するかどうか
     */
    public EncryptionMode(byte keyNumber, CipherMethod cipherMethod, BlockCipherMode blockCipherMode,
                          DSConstant dsConstant, PaddingMode paddingMode, byte paddingValue, boolean isPin) {
        mKeyNumber = keyNumber;
        mCipherMethod = cipherMethod;
        mBlockCipherMode = blockCipherMode;
        mDSConstant = dsConstant;
        mPaddingMode = paddingMode;
        mPaddingValue = paddingValue;
        mIsPin = isPin;
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
    public DSConstant getDSConstant() {
        return mDSConstant;
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
}
