package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.util.LogUtil;

/**
 * MFi版 FeliCa RFモード開始(fi)コマンド.
 */
public class MFiFelicaOpenWithoutLedCommand extends MFiCommand {
    /**
     * コンストラクタ.
     */
    public MFiFelicaOpenWithoutLedCommand() {
        super(new byte[]{'f', 'i', 'l', 'o', 'f', 'f', 0x00});
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

    /**
     * レスポンス解析処理。
     *
     * 'f' 'i' status1 status2 が返却される
     *
     * @param response レスポンスパケットデータ
     * @return 結果オブジェクト(成功時 STATUS_SUCCESS)
     */
    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length == 4) {
            //TODO status 内容確認 (Hidctl 通りだと逆になっている)
            if (bytes[0] == 'f' && bytes[1] == 'i' && bytes[2] == 0 && bytes[3] == 0) {
                return new IncredistResult(IncredistResult.STATUS_SUCCESS);
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE, LogUtil.hexString(bytes));
    }
}
