package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Locale;

import jp.co.flight.incredist.android.internal.controller.result.FelicaCommandResult;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.util.FLog;

/**
 * MFi版 FeliCa コマンドパケット送信コマンド.
 */
public class MFiFelicaSendCommand extends MFiCommand {
    private static final String TAG = "MFiFelicaSendCommand";
    private static final byte[] FSOC_HEADER = {'f', 's', 'o', 'c'};

    private final int mWaitParam;
    private final long mCreatedTime;
    private final long mWait;

    /**
     * コマンドパケット生成
     *
     * @param felicaCommand FeliCaコマンド
     * @return fsoc コマンドのパケット内容
     */
    private static byte[] createPayload(byte wait, byte[] felicaCommand) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] payload = new byte[felicaCommand.length + 7];

        System.arraycopy(FSOC_HEADER, 0, payload, 0, FSOC_HEADER.length);
        payload[4] = (byte) felicaCommand.length;
        payload[5] = wait;
        System.arraycopy(felicaCommand, 0, payload, 6, felicaCommand.length);

        return payload;
        // CHECKSTYLE:ON MagicNumber
    }

    /**
     * コンストラクタ.
     *
     * @param wait          コマンド待ち時間(単位: msec)
     * @param felicaCommand felicaコマンド
     */
    public MFiFelicaSendCommand(int wait, byte[] felicaCommand) {
        super(createPayload((byte) wait, felicaCommand));
        mWaitParam = wait;
        mWait = wait * 5 + 2000;
        mCreatedTime = System.currentTimeMillis();
    }

    /**
     * 最大応答待ち時間.
     *
     * @return MFiFelicaSendCommandで指定されたwait * 5 + 2000
     */
    @Override
    public long getResponseTimeout() {
        return mWait;
    }

    /**
     * 通信後のウェイト時間
     * FeliCa 通信処理では 100 とする
     *
     * @return 0msec
     */
    @Override
    public long getGuardWait() {
        return 100;
    }

    /**
     * レスポンス解析処理。
     * <p>
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
        if (bytes != null && bytes.length >= 7) {
            int length = bytes[6] & 0xff;

            if (length == 0 && bytes.length == 7) {
                FLog.d(TAG, String.format(Locale.JAPANESE, "wait param:%d wait:%d real:%d length: %d", mWaitParam, mWait, System.currentTimeMillis() - mCreatedTime, bytes.length));

                return new FelicaCommandResult(bytes[4] & 0xff, bytes[5] & 0xff,
                        new byte[]{},
                        Arrays.copyOfRange(bytes, 4, bytes.length));
            } else if (bytes[0] == 'f' && bytes[1] == 's' && bytes[2] == 'o' && bytes[3] == 'c' && bytes.length == length + 7) {
                FLog.d(TAG, String.format(Locale.JAPANESE, "wait param:%d wait:%d real:%d length: %d", mWaitParam, mWait, System.currentTimeMillis() - mCreatedTime, bytes.length));

                return new FelicaCommandResult(bytes[4] & 0xff, bytes[5] & 0xff,
                        Arrays.copyOfRange(bytes, 8, bytes.length),
                        Arrays.copyOfRange(bytes, 4, bytes.length));
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}
