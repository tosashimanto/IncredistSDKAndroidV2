package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

import jp.co.flight.incredist.android.internal.controller.command.IncredistCommand;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * MFi 版 Incredist への送信コマンド
 */

public abstract class MFiCommand extends MFiPacket implements IncredistCommand {
    /*package*/ static final int GUARD_WAIT_WITH_RESPONSE = 100;
    /*package*/ static final int GUARD_WAIT_WITHOUT_RESPONSE = 200;

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

        if (mMFiData != null) {
            System.arraycopy(mfiHeader, 0, mMFiData, 0, mfiHeader.length);
            System.arraycopy(payload, 0, mMFiData, 9, payload.length);
            mMFiData[2] = getPacketLengthValue();
            mMFiData[mMFiData.length - 1] = getChecksum();
        }
    }

    /**
     * 送信するパケットの個数.
     *
     * @return パケット数
     */
    /* package */
    int getPacketCount() {
        if (mMFiData != null) {
            return mMFiData.length / CHARACTERISTIC_VALUE_LENGTH + 1;
        } else {
            return 0;
        }
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
        if (mMFiData != null && count >= 0 && count < getPacketCount()) {
            int from = count * CHARACTERISTIC_VALUE_LENGTH;
            int to = (count + 1) * CHARACTERISTIC_VALUE_LENGTH;
            if (mMFiData.length < to) {
                to = mMFiData.length;
            }
            return Arrays.copyOfRange(mMFiData, from, to);
        } else {
            return null;
        }
    }


    /**
     * 処理後のウェイト時間
     * @return デフォルト値(100msec)
     */
    @Override
    public long getGuardWait() {
        if (getResponseTimeout() <= 0) {
            return MFiCommand.GUARD_WAIT_WITHOUT_RESPONSE;
        } else {
            return MFiCommand.GUARD_WAIT_WITH_RESPONSE;
        }
    }

    /**
     * 応答の解析
     */
    public IncredistResult parseResponse(MFiResponse response) {
        if (response.isValid()) {
            return parseMFiResponse(response);
        } else {
            int status = response.errorCode > 0 ? response.errorCode : IncredistResult.STATUS_INVALID_RESPONSE;
            return new IncredistResult(status);
        }
    }

    @NonNull
    protected abstract IncredistResult parseMFiResponse(MFiResponse response);
}
