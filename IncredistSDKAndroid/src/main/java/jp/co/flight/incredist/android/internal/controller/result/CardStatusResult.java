package jp.co.flight.incredist.android.internal.controller.result;

/**
 * カード挿入チェック結果クラス.
 */
@SuppressWarnings("WeakerAccess")
public class CardStatusResult extends IncredistResult {
    public final boolean isInserted;

    public CardStatusResult(boolean isInserted) {
        super(IncredistResult.STATUS_SUCCESS);

        this.isInserted = isInserted;
    }
}
