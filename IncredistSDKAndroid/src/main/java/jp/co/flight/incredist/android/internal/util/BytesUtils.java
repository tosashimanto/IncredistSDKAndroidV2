package jp.co.flight.incredist.android.internal.util;

import android.support.annotation.NonNull;

import java.nio.charset.Charset;

/**
 * バイト配列操作用メソッド
 */
public class BytesUtils {
    private BytesUtils() {
        // インスタンス生成不可
    }

    /**
     * byte 配列を ASCII コードとして文字列へ変換
     *
     * @param data   入力データ
     * @param offset オフセット
     * @param length 長さ
     * @return 変換文字列
     */
    @NonNull
    public static String toAsciiString(byte[] data, int offset, int length) {
        return new String(data, offset, length, Charset.defaultCharset());
    }

    /**
     * byte 配列を 16進数の列として文字列へ変換
     * BCD データを数字文字列へ変換するのにも利用できる
     *
     * @param data   入力データ
     * @param offset オフセット
     * @param length 長さ
     * @return 変換文字列
     */
    @NonNull
    public static String toHexString(byte[] data, int offset, int length) {
        if (data != null) {
            StringBuilder sb = new StringBuilder();

            for (byte b : data) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * Reduced PAN and Validity の 14byte からマスクされたカード番号を取得
     *
     * @param data   入力データ
     * @param offset オフセット
     * @param length データの長さ
     * @return マスクされたカード番号 16文字 ######XXXXXX#### 形式
     */
    @NonNull
    public static String getMaskedCardNo(byte[] data, int offset, int length) {
        // CHECKSTYLE:OFF MagicNumber
        if (length >= 10) {
            String ascii = toAsciiString(data, offset, 10);

            return ascii.substring(0, 6) + "XXXXXX" + ascii.substring(6, 10);
        }
        // CHECKSTYLE:ON MagicNumber

        return "";
    }

    /**
     * Reduced PAN and Validity の 14byte からマスクされた有効期限を取得
     *
     * @param data   入力データ
     * @param offset オフセット
     * @param length データの長さ
     * @return 有効期限 MMYY 形式
     */
    @NonNull
    public static String getExpirationDate(byte[] data, int offset, int length) {
        // CHECKSTYLE:OFF MagicNumber
        if (length >= 14) {
            return toAsciiString(data, 10, 4);
        }
        // CHECKSTYLE:ON MagicNumber

        return "";
    }

    /**
     * データの先頭が prefix と一致するかどうかチェックする
     *
     * @param data   入力データ
     * @param prefix 先頭一致でのチェックデータ
     * @return data が prefix で始まっている場合 true, そうではない場合 false
     */
    public static boolean startsWith(byte[] data, byte[] prefix) {
        if (data == null || prefix == null || data.length < prefix.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }

        return true;
    }
}
