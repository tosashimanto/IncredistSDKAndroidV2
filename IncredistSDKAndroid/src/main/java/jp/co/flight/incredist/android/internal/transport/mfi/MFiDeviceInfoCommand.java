package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import java.nio.charset.Charset;

import jp.co.flight.incredist.android.internal.controller.result.DeviceInfoResult;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * MFi版 デバイス情報取得(d)コマンド.
 */
public class MFiDeviceInfoCommand extends MFiCommand {
    /**
     * コンストラクタ.
     */
    public MFiDeviceInfoCommand() {
        super(new byte[]{'d', 0x00});
    }

    /**
     * 最大応答待ち時間.
     *
     * @return 1000msec
     */
    @Override
    public long getResponseTimeout() {
        return 1000; // SUPPRESS CHECKSTYLE MagicNumber
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] bytes = response.getData();
        if (bytes != null) {
            String str = new String(bytes, Charset.forName("UTF-8"));
            String[] values = str.split("\n");

            if (values.length >= 4) {
                return new DeviceInfoResult(values[0], values[1], values[2], values[3], values.length > 4 ? values[4] : null);
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
