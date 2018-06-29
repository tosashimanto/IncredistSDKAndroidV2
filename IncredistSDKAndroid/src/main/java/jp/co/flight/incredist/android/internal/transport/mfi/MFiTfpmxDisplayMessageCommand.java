package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.charset.Charset;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * MFi版 TFPメッセージ表示(tfpmx) 応答つきコマンド.
 */
public class MFiTfpmxDisplayMessageCommand extends MFiCommand {
    private static final byte[] EM_HEADER = {'t', 'f', 'p', 'm', 'x'};

    private static byte[] createPayload(int type, @Nullable String message) {
        byte[] messageBytes = null;
        int messageByteSize = 0;
        if (message != null) {
            messageBytes = message.replaceAll("¥", "\\\\").getBytes(Charset.forName("US-ASCII"));
            messageByteSize = messageBytes.length;
        }

        // CHECKSTYLE:OFF MagicNumber
        byte[] payload = new byte[8 + messageByteSize];
        System.arraycopy(EM_HEADER, 0, payload, 0, EM_HEADER.length);
        payload[5] = (byte) ((type / 100 % 10) + '0');
        payload[6] = (byte) ((type / 10 % 10) + '0');
        payload[7] = (byte) ((type % 10) + '0');

        if (messageByteSize > 0) {
            System.arraycopy(messageBytes, 0, payload, 8, messageByteSize);
        }
        // CHECKSTYLE:ON MagicNumber

        return payload;
    }

    /**
     * コンストラクタ.
     */
    public MFiTfpmxDisplayMessageCommand(int type, @Nullable String message) {
        super(createPayload(type, message));
    }

    /**
     * 最大応答待ち時間.
     *
     * @return
     */
    @Override
    public long getResponseTimeout() {
        return 300;
    }

    @Override
    public long getGuardWait() {
        return 0;
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length == 1) {
            return new IncredistResult(IncredistResult.STATUS_SUCCESS);
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
