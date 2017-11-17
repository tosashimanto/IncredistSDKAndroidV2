package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.Nullable;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * MFi パケットの共通処理.
 */
abstract class MFiPacket {
    /**
     * MFiヘッダを付与した送信内容.
     */
    @Nullable
    byte[] mMFiData;

    /**
     * エラーコード.
     */
    int mErrorCode = -1;

    /**
     * コンストラクタ.
     */

    MFiPacket() {
        mMFiData = null;
    }

    /**
     * コンストラクタ.
     *
     * @param size パケット長
     */
    MFiPacket(int size) {
        mMFiData = new byte[size];
    }

    /**
     * MFi パケットが正常かどうかを返します.
     *
     * @return 正常なパケットの場合 True
     */
    boolean isValid() {
        if (mErrorCode >= 0) {
            return mErrorCode == IncredistResult.STATUS_SUCCESS;
        } else {
            return mMFiData != null
                    && mMFiData.length > 10
                    && getPacketLengthValue() == mMFiData[2]
                    && getChecksum() == mMFiData[mMFiData.length - 1];
        }
    }

    /**
     * パケット長の値を計算します.
     *
     * @return パケット長(offset 2)に設定するべき値
     */
    byte getPacketLengthValue() {
        if (mMFiData != null) {
            return (byte) ((mMFiData.length - 4) & 0xff);
        } else {
            return 0;
        }
    }

    /**
     * チェックサムを計算します.
     * パケット長(offset 2)を先に設定しておく必要があります.
     *
     * @return チェックサム値
     */
    byte getChecksum() {
        if (mMFiData != null) {
            int sum = 0;
            for (int i = 2; i < mMFiData.length - 1; i++) {
                sum += mMFiData[i];
            }

            return (byte) (-(sum & 0xff));
        } else {
            return 0;
        }
    }
}
