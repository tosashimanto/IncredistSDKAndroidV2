package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * Incredist から返却された MFi パケット
 */
public class MFiResponse extends MFiPacket {
    private static final String TAG = "MFiResponse";

    private int appendPos = 0;

    /**
     * コンストラクタ.
     */
    MFiResponse() {
        super();
    }

    /**
     * 受信データを追加
     *
     * @param data 受信データのbyte配列
     */
    /*package*/
    synchronized void appendData(byte[] data) {
        if (mMFiData == null) {
            // 最初のパケット
            int length = responseLength(data);

            if (length > 0) {
                mMFiData = new byte[length];

                System.arraycopy(data, 0, mMFiData, 0, data.length);
                appendPos = data.length;
                errorCode = -1;
            } else {
                errorCode = IncredistResult.STATUS_INVALID_RESPONSE_HEADER;
            }
        } else {
            if (appendPos + data.length <= mMFiData.length) {
                System.arraycopy(data, 0, mMFiData, appendPos, data.length);
                appendPos += data.length;
                errorCode = -1;
            } else {
                // 受信サイズエラー(パケット長より多くのデータを受信)
                errorCode = IncredistResult.STATUS_TOO_LARGE_RESPONSE;
            }
        }
    }

    /**
     * 受信を継続するかどうかを取得します.
     *
     * @return 受信継続: true 受信終了: false
     */
    /*package*/
    synchronized boolean needMoreData() {
        if (errorCode >= 0 || mMFiData == null) {
            // エラー状態の場合は受信終了
            return false;
        } else {
            // パケット長に達するまで受信継続
            return appendPos < mMFiData.length;
        }
    }

    /**
     * 受信パケットデータをクリアします.
     */
    /*package*/
    synchronized void clear() {
        errorCode = -1;
        appendPos = 0;
        mMFiData = null;
    }

    /**
     * 先頭パケットからパケット長を取得します.
     *
     * @param firstValue 先頭パケットのデータ
     * @return パケット長. 不正なデータの場合 0.
     */
    private static int responseLength(@NonNull byte[] firstValue) {
        if (firstValue.length > 10 && firstValue[0] == (byte) 0xff && firstValue[1] == (byte) 0x55) {
            return (firstValue[2] & 0xff) + 4;
        } else {
            return 0;
        }
    }

    /**
     * MFi パケットのデータ部分を取得します.
     *
     * @return ヘッダとチェックサムを除いたデータ部分の byte 配列
     */
    @Nullable
    synchronized public byte[] getData() {
        if (mMFiData != null) {
            return Arrays.copyOfRange(mMFiData, 9, mMFiData.length - 1);
        } else {
            return null;
        }
    }

    /**
     * MFiResponse パケットの複製オブジェクトを生成します.
     *
     * @return このオブジェクトの複製.
     */
    /*package*/
    MFiResponse copyInstance() {
        MFiResponse copy = new MFiResponse();
        copy.errorCode = this.errorCode;
        copy.appendPos = this.appendPos;
        if (this.mMFiData != null) {
            copy.mMFiData = Arrays.copyOf(mMFiData, mMFiData.length);
        } else {
            copy.mMFiData = null;
        }
        return copy;
    }
}
