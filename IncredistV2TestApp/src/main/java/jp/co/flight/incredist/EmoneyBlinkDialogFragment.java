package jp.co.flight.incredist;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import jp.co.flight.incredist.android.IncredistV2TestApp.R;
import jp.co.flight.incredist.android.IncredistV2TestApp.databinding.FragmentDialogEmoneyBlinkBinding;
import jp.co.flight.incredist.android.model.LedColor;

/**
 * 電子マネー用の点滅設定ダイアログ
 */
public class EmoneyBlinkDialogFragment extends DialogFragment {

    public static class Param {
        public final ObservableBoolean isBlink = new ObservableBoolean();
        public final ObservableField<LedColor> ledColor = new ObservableField<>();
        public final ObservableInt duration = new ObservableInt();

        Param(boolean isBlink, LedColor ledColor, int duration) {
            this.isBlink.set(isBlink);
            this.ledColor.set(ledColor);
            this.duration.set(duration);
        }
    }

    private Param mParam = new Param(true, LedColor.BLUE, 200);

    /**
     * インスタンスを生成します
     *
     * @return EmoneyBlinkDialogFragment のインスタンス
     */
    public static EmoneyBlinkDialogFragment newInstance() {
        EmoneyBlinkDialogFragment fragment = new EmoneyBlinkDialogFragment();
        return fragment;
    }

    public EmoneyBlinkDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_emoney_blink);

        FragmentDialogEmoneyBlinkBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_dialog_emoney_blink, null, false);
        builder.setView(binding.getRoot());

        binding.setParam(mParam);

        builder.setPositiveButton(R.string.ok, (dialogInterface, button) -> {
            Fragment target = getTargetFragment();
            int requestCode = getTargetRequestCode();

            if (target != null && target instanceof Listener) {
                ((Listener) target).onSetEmoneyBlink(requestCode, mParam.isBlink.get(), mParam.ledColor.get(), mParam.duration.get());
            }
        });

        builder.setNegativeButton(R.string.cancel, (dialogInterface, button) -> {
            dismiss();
        });

        return builder.create();
    }

    public interface Listener {
        void onSetEmoneyBlink(int requestCode, boolean isBlink, LedColor color, int duration);
    }
}
