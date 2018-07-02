package jp.co.flight.incredist.android.internal.controller.result;

/**
 * FeliCa コマンド送信結果
 */
public class FelicaCommandResult extends IncredistResult {
    public final int status1;
    public final int status2;
    public final byte[] resultData;
    public final byte[] rawData;
    public final long real;

    /**
     * コンストラクタ
     *
     * @param resultData FeliCaコマンドの応答データ
     */
    public FelicaCommandResult(int status1, int status2, byte[] resultData, byte[] rawData, long real) {
        super(IncredistResult.STATUS_SUCCESS);

        this.status1 = status1;
        this.status2 = status2;
        this.resultData = resultData;
        this.rawData = rawData;
        this.real = real;
    }
}
