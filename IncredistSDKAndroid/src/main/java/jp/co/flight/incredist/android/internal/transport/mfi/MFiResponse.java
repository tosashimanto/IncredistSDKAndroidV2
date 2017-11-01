package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

/**
 * Incredist から返却された MFi パケット
 */
public class MFiResponse extends MFiPacket {
    /**
     * コンストラクタ.
     *
     * @param firstValue 先頭パケットのデータ
     */
    MFiResponse(@NonNull byte[] firstValue) {
        super(MFiResponse.responseLength(firstValue));
    }

    /**
     * 先頭パケットからパケット長を取得します.
     *
     * @param firstValue 先頭パケットのデータ
     * @return パケット長. 不正なデータの場合 0.
     */
    private static int responseLength(@NonNull byte[] firstValue) {
        if (firstValue.length > 10 && firstValue[0] == (byte) 0xff && firstValue[1] == (byte) 0x55) {
            return firstValue[2] & 0xff;
        } else {
            return 0;
        }
    }

}
