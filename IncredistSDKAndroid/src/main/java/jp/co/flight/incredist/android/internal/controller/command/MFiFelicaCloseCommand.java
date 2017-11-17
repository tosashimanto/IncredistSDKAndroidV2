package jp.co.flight.incredist.android.internal.controller.command;

import android.support.annotation.NonNull;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiResponse;

/**
 * MFi版 FeliCa RFモード終了(fis)コマンド.
 */
public class MFiFelicaCloseCommand extends MFiCommand {
    /**
     * コンストラクタ.
     */
    public MFiFelicaCloseCommand() {
        super(new byte[]{'f', 'i', 's', 0x00});
    }

    /**
     * 最大応答待ち時間.
     *
     * @return 1000msec
     */
    @Override
    public long getResponseTimeout() {
        return 1000;
    }

    /**
     * レスポンス解析処理。
     *
     * TODO: 仕様確認が必要
     *
     * @param response レスポンスパケットデータ
     * @return 結果オブジェクト(成功時 STATUS_SUCCESS)
     */
    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length == 5) {
            //TODO status 内容確認 (Hidctl 通りだと逆になっている)
            if (bytes[0] == 'f' && bytes[1] == 'i' && bytes[2] == 's' && bytes[3] == 0 && bytes[4] == 0) {
                return new IncredistResult(IncredistResult.STATUS_SUCCESS);
            }
        }

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
