package jp.co.flight.incredist.android.internal.transport.mfi;

/**
 * MFi パケットの共通処理
 */
/* package */
abstract class MFiPacket {
    /**
     * MFiヘッダを付与した送信内容.
     */
    /* package */
    final byte[] mMFiData;

    /**
     * コンストラクタ.
     *
     * @param size パケット長
     */
    /* package */
    MFiPacket(int size) {
        mMFiData = new byte[size];
    }

    /**
     * MFi パケットが正常かどうかを返します.
     *
     * @return 正常なパケットの場合 True
     */
    public boolean isValid() {
        return mMFiData.length > 10
                && getPacketLengthValue() == mMFiData[2]
                && getChecksum() == mMFiData[mMFiData.length - 1];
    }

    /**
     * パケット長の値を計算します.
     *
     * @return パケット長(offset 2)に設定するべき値
     */
    /* package */
    byte getPacketLengthValue() {
        return (byte) ((mMFiData.length - 3) & 0xff);
    }

    /**
     * チェックサムを計算します.
     * パケット長(offset 2)を先に設定しておく必要があります.
     *
     * @return チェックサム値
     */
    /* package */
    byte getChecksum() {
        int sum = 0;
        for (int i = 2; i < mMFiData.length - 1; i++) {
            sum += mMFiData[i];
        }

        return (byte) (-(sum & 0xff));
    }
}
