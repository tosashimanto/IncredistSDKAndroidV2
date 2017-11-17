package jp.co.flight.incredist.android.model;

/**
 * FeliCa コマンド処理結果
 */
public class FelicaCommandResult {
    private final int mStatus1;
    private final int mStatus2;
    private final byte[] mResultData;

    /**
     * コンストラクタ
     *
     * @param resultData 処理結果データ
     */
    public FelicaCommandResult(int status1, int status2, byte[] resultData) {
        mStatus1 = status1;
        mStatus2 = status2;
        mResultData = resultData;
    }

    /**
     * 処理結果1 を取得します
     *
     * @return 処理結果1
     */
    public int getStatus1() {
        return mStatus1;
    }

    /**
     * 処理結果2 を取得します
     *
     * @return 処理結果2
     */
    public int getStatus2() {
        return mStatus2;
    }

    /**
     * 結果データを取得します
     *
     * @return 結果データ
     */
    public byte[] getResultData() {
        return mResultData;
    }

}
