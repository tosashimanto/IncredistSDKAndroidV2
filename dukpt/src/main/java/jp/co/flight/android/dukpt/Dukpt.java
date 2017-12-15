package jp.co.flight.android.dukpt;

import jp.co.flight.android.dukpt.domain.Decrypto;
import jp.co.flight.android.dukpt.domain.DesDecrypto;

/**
 * Dukptライブラリ JNI 呼び出しクラス
 */
public class Dukpt {

    public Dukpt() {
        System.loadLibrary("Dukpt");
    }

    public native Decrypto decrypt2(byte[] szKsn, long wSize, byte[] szEncData, byte[] szDecData);

    public native Decrypto aesDecrypt2(long wSize, byte[] szEncData, byte[] szDecData);

    public native DesDecrypto desEncrypt(long wSize, byte[] szOrgData, byte[] szEncData, byte[] sessionKey, int parity_Flag);

    public native DesDecrypto desDecrypt(long wSize, byte[] szEncData, byte[] szDecData, byte[] sessionKey);

}
