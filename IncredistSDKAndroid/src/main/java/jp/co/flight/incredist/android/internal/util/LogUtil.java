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
        return hexString(bytes, 0, bytes.length);
    }

    public static String hexString(byte[] bytes, int offset, int length) {
        if (bytes != null) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < length; i++) {
                if (offset + i < bytes.length) {
                    sb.append(String.format("%02x:", bytes[offset + i]));
                } else {
                    sb.append(" length over ");
                    break;
                }
            }

            return sb.toString();
        } else {
            return "(no data)";
        }
    }
}
