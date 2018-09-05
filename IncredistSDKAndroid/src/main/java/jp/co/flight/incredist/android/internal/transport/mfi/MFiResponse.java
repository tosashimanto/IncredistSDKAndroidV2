package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.Locale;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.util.FLog;

/**
 * Incredist から返却された MFi パケット.
 */
public class MFiResponse extends MFiPacket {
    private static final String TAG = "MFiResponse";

    private int mAppendPos = 0;

    /**
     * コンストラクタ.
     */
    MFiResponse() {
        super();
    }

    /**
     * 受信データを追加.
     *
     * @param data 受信データのbyte配列
     */
    void appendData(byte[] data) {
        appendData(data, 0, data.length);
    }

    /**
     * 受信データを追加.
     *
     * @param data       受信データのbyte配列
     * @param offset     先頭位置
     * @param datalength データ長
     */
    synchronized void appendData(byte[] data, int offset, int datalength) {
        if (mMFiData == null) {
            // 最初のパケット
            int length = responseLength(data);

            if (length > 0) {
                mMFiData = new byte[length];

                int firstLen = datalength;
                if (firstLen > length) {
                    // 実際に受信したデータの方がパケット長より長い
                    FLog.w(TAG, String.format(Locale.US, "packet length mismatch header:%d received:%d", length, datalength));
                    firstLen = length;
                }
                System.arraycopy(data, offset, mMFiData, 0, firstLen);
                mAppendPos = datalength;
                mErrorCode = -1;
            } else {
                mErrorCode = IncredistResult.STATUS_INVALID_RESPONSE_HEADER;
            }
        } else {
            if (mAppendPos + datalength <= mMFiData.length) {
                System.arraycopy(data, offset, mMFiData, mAppendPos, datalength);
                mAppendPos += datalength;
                mErrorCode = -1;
            } else {
                // 受信サイズエラー(パケット長より多くのデータを受信)
                mErrorCode = IncredistResult.STATUS_TOO_LARGE_RESPONSE;
            }
        }
    }
    
    /**
     * 受信を継続するかどうかを取得します.
     *
     * @return 受信継続: true 受信終了: false
     */
    synchronized boolean needMoreData() {
        if (mErrorCode >= 0 || mMFiData == null) {
            // エラー状態の場合は受信終了
            return false;
        } else {
            // パケット長に達するまで受信継続
            return mAppendPos < mMFiData.length;
        }
    }

    /**
     * 受信パケットデータをクリアします.
     */
    synchronized void clear() {
        mErrorCode = -1;
        mAppendPos = 0;
        mMFiData = null;
    }

    /**
     * 先頭パケットからパケット長を取得します.
     *
     * @param firstValue 先頭パケットのデータ
     * @return パケット長. 不正なデータの場合 0.
     */
    private static int responseLength(@NonNull byte[] firstValue) {
        // CHECKSTYLE:OFF MagicNumber
        if (firstValue.length > 10 && firstValue[0] == (byte) 0xff && firstValue[1] == (byte) 0x55) {
            return (firstValue[2] & 0xff) + 4;
        } else {
            return 0;
        }
        // CHECKSTYLE:ON MagicNumber
    }

    /**
     * MFi パケットのデータ部分を取得します.
     *
     * @return ヘッダとチェックサムを除いたデータ部分の byte 配列
     */
    @Nullable
    public synchronized byte[] getData() {
        if (mMFiData != null) {
            return Arrays.copyOfRange(mMFiData, 9, mMFiData.length - 1); // SUPPRESS CHECKSTYLE MagicNumber
        } else {
            return null;
        }
    }

    /**
     * MFiResponse パケットの複製オブジェクトを生成します.
     *
     * @return このオブジェクトの複製.
     */
    MFiResponse copyInstance() {
        MFiResponse copy = new MFiResponse();
        copy.mErrorCode = this.mErrorCode;
        copy.mAppendPos = this.mAppendPos;
        if (this.mMFiData != null) {
            copy.mMFiData = Arrays.copyOf(mMFiData, mMFiData.length);
        } else {
            copy.mMFiData = null;
        }
        return copy;
    }

    /**
     * 受信データが存在するかどうかを返します
     *
     * @return 受信データがある場合 true
     */
    public boolean hasData() {
        return mAppendPos > 0;
    }
}
