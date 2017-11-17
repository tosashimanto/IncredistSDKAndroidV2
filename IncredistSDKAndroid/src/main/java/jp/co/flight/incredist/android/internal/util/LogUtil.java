package jp.co.flight.incredist.android.internal.util;

/**
 * ログ用のユーティリティクラス
 */
public final class LogUtil {

    /**
     * インスタンスを生成しないための空のコンストラクタ
     */
    private LogUtil() {

    }

    /**
     * 16進でダンプした文字列を取得します
     *
     * @param bytes ダンプするデータ
     * @return 16進文字列
     */
    public static String hexString(byte[] bytes) {
        if (bytes != null) {
            StringBuilder sb = new StringBuilder();

            for (byte b : bytes) {
                sb.append(String.format("%02x:", b));
            }

            return sb.toString();
        } else {
            return "(no data)";
        }
    }
}
