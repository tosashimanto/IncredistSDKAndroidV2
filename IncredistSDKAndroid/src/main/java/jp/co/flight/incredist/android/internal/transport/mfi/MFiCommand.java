package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

/**
 * MFi 版 Incredist への送信コマンド
 */

public class MFiCommand extends MFiPacket {
    private int CHARACTERISTIC_VALUE_LENGTH = 20;

    /**
     * コンストラクタ.
     *
     * @param payload 送信内容
     */
    public MFiCommand(@NonNull byte[] payload) {
        super(payload.length + 10);
        byte[] mfiHeader = {
                (byte) 0xff, (byte) 0x55, (byte) 0x00, (byte) 0x00, (byte) 0x43,
                (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x01
        };

        System.arraycopy(mfiHeader, 0, mMFiData, 0, mfiHeader.length);
        System.arraycopy(payload, 0, mMFiData, 9, payload.length + 9);
        mMFiData[2] = getPacketLengthValue();
        mMFiData[mMFiData.length - 1] = getChecksum();
    }

    /**
     * 送信するパケットの個数.
     *
     * @return パケット数
     */
    /* package */
    int getPacketCount() {
        return mMFiData.length / CHARACTERISTIC_VALUE_LENGTH + 1;
    }

    /**
     * 送信するパケットを取得します.
     *
     * @param count 何番目のパケットか
     * @return 分割されたパケットデータ
     */
    /* package */
    @Nullable
    byte[] getValueData(int count) {
        if (count > 0 && count <= getPacketCount()) {
            int from = (count - 1) * CHARACTERISTIC_VALUE_LENGTH;
            int to = count * CHARACTERISTIC_VALUE_LENGTH;
            if (mMFiData.length < to) {
                to = mMFiData.length;
            }
            return Arrays.copyOfRange(mMFiData, from, to);
        } else {
            return null;
        }
    }

}
