package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.Locale;

import jp.co.flight.incredist.android.internal.controller.command.IncredistCommand;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.util.FLog;
import jp.co.flight.incredist.android.internal.util.LogUtil;

/**
 * MFi 版 Incredist への送信コマンド.
 */

public abstract class MFiCommand extends MFiPacket implements IncredistCommand {
    private static final String TAG = "MFiCommand";

    static final int GUARD_WAIT_WITH_RESPONSE = 100;
    static final int GUARD_WAIT_WITHOUT_RESPONSE = 200;

    private static final int CHARACTERISTIC_VALUE_LENGTH = 20;

    /**
     * コンストラクタ.
     *
     * @param payload 送信内容
     */
    protected MFiCommand(@NonNull byte[] payload) {
        // CHECKSTYLE:OFF MagicNumber
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

            FLog.d(TAG, String.format(Locale.JAPANESE, "create MFiCommand %d %s", mMFiData.length, LogUtil.hexString(mMFiData)));
        }
        // CHECKSTYLE:ON MagicNumber
    }

    /**
     * 送信するパケットの個数.
     *
     * @return パケット数
     */
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
     * キャンセル可能かどうか
     *
     * @return キャンセルできるコマンドの場合 true
     */
    public boolean cancelable() {
        return false;
    }

    /**
     * 処理後のウェイト時間.
     *
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
     * 応答の解析.
     *
     * @return 解析結果
     */
    public IncredistResult parseResponse(MFiResponse response) {
        if (response.isValid()) {
            return parseMFiResponse(response);
        } else {
            int status = response.mErrorCode > 0 ? response.mErrorCode : IncredistResult.STATUS_INVALID_RESPONSE;
            return new IncredistResult(status);
        }
    }

    @NonNull
    protected abstract IncredistResult parseMFiResponse(MFiResponse response);
}
