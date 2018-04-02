package jp.co.flight.incredist.android.internal.controller.result;

/**
 * 点滅処理結果クラス.
 */
@SuppressWarnings("WeakerAccess")
public class BlinkResult extends IncredistResult {
    public final boolean isOn;

    public BlinkResult(boolean isOn) {
        super(IncredistResult.STATUS_SUCCESS);

        this.isOn = isOn;
    }
}
