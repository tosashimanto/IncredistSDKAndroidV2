package jp.co.flight.incredist.android.internal.controller.result;

/**
 * EMVカーネル設定チェック結果クラス.
 */
@SuppressWarnings("WeakerAccess")
public class EmvCheckKernelSettingResult extends IncredistResult {
    public final boolean isMatched;

    public EmvCheckKernelSettingResult(boolean isMatched) {
        super(IncredistResult.STATUS_SUCCESS);

        this.isMatched = isMatched;
    }
}
