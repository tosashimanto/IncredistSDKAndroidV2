package jp.co.flight.incredist.android.internal.controller.result;

import java.util.Calendar;

/**
 * デバイス時刻取得結果クラス.
 */
@SuppressWarnings("WeakerAccess")
public class RtcResult extends IncredistResult {
    public final Calendar calendar;

    /**
     * コンストラクタ.
     *
     * @param calendar 時刻
     */
    public RtcResult(Calendar calendar) {
        super(IncredistResult.STATUS_SUCCESS);

        this.calendar = calendar;
    }
}
