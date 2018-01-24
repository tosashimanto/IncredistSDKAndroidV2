package jp.co.flight.incredist.android.internal.controller.result;

import java.io.ByteArrayOutputStream;

import jp.co.flight.incredist.android.model.EmvPacket;

/**
 * 決済時の EMVKernel からの応答データ
 */
public class EmvResult extends IncredistResult {
    private final int mCountPacket;
    private final int mDataLength;
    private int mCount;
    private int mLength;

    ByteArrayOutputStream mByteStream = new ByteArrayOutputStream();

    public EmvResult(int countPacket, int dataLength) {
        super(IncredistResult.STATUS_SUCCESS);

        mCountPacket = countPacket;
        mDataLength = dataLength;
        mCount = 0;
        mLength = 0;
    }

    /**
     * 受信データ処理
     *
     * @param data 受信データ
     * @param offset オフセット
     * @param length データ長
     * @return 指定されたパケット数受信したらtrue
     */
    public boolean appendBytes(int index, byte[] data, int offset, int length) {
        if (index == mCount + 1) {
            if (mLength + length <= mDataLength) {
                mByteStream.write(data, offset, length);
                mLength += length;
            }

            mCount++;

            if (mCount == mCountPacket) {
                return true;
            }
        }

        return false;
    }

    /**
     * 受信データの長さが正常かどうか
     *
     * @return 正常な長さの場合 true, 異常の場合 false
     */
    public boolean isValidLength() {
        return mLength == mDataLength;
    }

    public EmvPacket toEmvPacket() {
        return new EmvPacket(mByteStream.toByteArray());
    }
}
