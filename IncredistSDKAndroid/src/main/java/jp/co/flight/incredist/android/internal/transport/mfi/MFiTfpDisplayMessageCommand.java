package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.charset.Charset;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * MFi版 TFPメッセージ表示(tfpm)コマンド.
 */
public class MFiTfpDisplayMessageCommand extends MFiCommand {
    private static final byte[] EM_HEADER = {'t', 'f', 'p', 'm'};

    private static byte[] createPayload(int type, @Nullable String message) {
        byte[] messageBytes = null;
        int messageByteSize = 0;
        if (message != null) {
            messageBytes = message.replaceAll("¥", "\\\\").getBytes(Charset.forName("US-ASCII"));
            messageByteSize = messageBytes.length;
        }

        // CHECKSTYLE:OFF MagicNumber
        byte[] payload = new byte[7 + messageByteSize];
        System.arraycopy(EM_HEADER, 0, payload, 0, EM_HEADER.length);
        payload[4] = (byte) ((type / 100 % 10) + '0');
        payload[5] = (byte) ((type / 10 % 10) + '0');
        payload[6] = (byte) ((type % 10) + '0');

        if (messageByteSize > 0) {
            System.arraycopy(messageBytes, 0, payload, 7, messageByteSize);
        }
        // CHECKSTYLE:ON MagicNumber

        return payload;
    }

    /**
     * コンストラクタ.
     */
    public MFiTfpDisplayMessageCommand(int type, @Nullable String message) {
        super(createPayload(type, message));
    }

    /**
     * 最大応答待ち時間.
     *
     * @return 応答パケットがないので -1 を返す
     */
    @Override
    public long getResponseTimeout() {
        return -1;
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        if (response instanceof MFiNoResponse) {
            return new IncredistResult(IncredistResult.STATUS_SUCCESS);
        } else {
            return new IncredistResult(response.getErrorCode());
        }
    }
}
