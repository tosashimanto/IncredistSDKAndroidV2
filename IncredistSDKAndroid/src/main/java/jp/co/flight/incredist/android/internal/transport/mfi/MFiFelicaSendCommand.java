package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import java.util.Arrays;

import jp.co.flight.incredist.android.internal.controller.result.FelicaCommandResult;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * MFi版 FeliCa コマンドパケット送信コマンド.
 */
public class MFiFelicaSendCommand extends MFiCommand {
    private static final byte[] FSOC_HEADER = {'f', 's', 'o', 'c'};

    /**
     * コマンドパケット生成
     *
     * @param felicaCommand FeliCaコマンド
     * @return fsoc コマンドのパケット内容
     */
    private static byte[] createPayload(byte[] felicaCommand) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] payload = new byte[felicaCommand.length + 7];

        System.arraycopy(FSOC_HEADER, 0, payload, 0, FSOC_HEADER.length);
        payload[4] = (byte) felicaCommand.length;
        payload[5] = (byte) 200;   // wait 200ms
        System.arraycopy(felicaCommand, 0, payload, 6, felicaCommand.length);

        return payload;
        // CHECKSTYLE:ON MagicNumber
    }

    /**
     * コンストラクタ.
     */
    public MFiFelicaSendCommand(byte[] felicaCommand) {
        super(createPayload(felicaCommand));
    }

    /**
     * 最大応答待ち時間.
     *
     * @return 1000msec
     */
    @Override
    public long getResponseTimeout() {
        return 1000;  // SUPPRESS CHECKSTYLE MagicNumber
    }

    /**
     * 通信後の
     *
     * @return
     */
    @Override
    public long getGuardWait() {
        return 0;
    }

    /**
     * レスポンス解析処理。
     *
     * 'f' 's' 'o' 'c' status1 status2 length data が返却される
     *
     * @param response レスポンスパケットデータ
     * @return 結果オブジェクト(成功時 STATUS_SUCCESS)
     */
    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length > 7) {
            int length = bytes[6] & 0xff;

            if (bytes[0] == 'f' && bytes[1] == 's' && bytes[2] == 'o' && bytes[3] == 'c' && bytes.length == length + 7) {
                return new FelicaCommandResult(bytes[4] & 0xff, bytes[5] & 0xff,
                        Arrays.copyOfRange(bytes, 8, bytes.length),
                        Arrays.copyOfRange(bytes, 4, bytes.length));
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
