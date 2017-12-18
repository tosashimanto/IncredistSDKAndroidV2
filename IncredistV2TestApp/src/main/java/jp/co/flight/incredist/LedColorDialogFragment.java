package jp.co.flight.incredist;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import jp.co.flight.incredist.android.IncredistV2TestApp.R;
import jp.co.flight.incredist.android.IncredistV2TestApp.databinding.FragmentDialogLedColorBinding;
import jp.co.flight.incredist.android.model.LedColor;

/**
 * LED色を選択するダイアログ
 */
public class LedColorDialogFragment extends DialogFragment {

    private static final String ARG_KEY_SHOW_CHECKBOX = "arg_key_show_checkbox";

    public static class Param {
        public final ObservableField<LedColor> ledColor = new ObservableField<>();
        public final ObservableBoolean isOn = new ObservableBoolean();

        Param(LedColor ledColor, boolean isOn) {
            this.ledColor.set(ledColor);
            this.isOn.set(isOn);
        }
    }

    private Param mParam = new Param(LedColor.BLUE, true);

    /**
     * インスタンスを生成します
     *
     * @param showCheckbox チェックボックスを表示するかどうか
     * @return LedColorDialogFragment のインスタンス
     */
    public static LedColorDialogFragment newInstance(boolean showCheckbox) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_KEY_SHOW_CHECKBOX, showCheckbox);

        LedColorDialogFragment fragment = new LedColorDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public LedColorDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        Bundle args = getArguments();

        boolean showCheckbox = args.getBoolean(ARG_KEY_SHOW_CHECKBOX, true);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(showCheckbox ? R.string.dialog_title_set_led_color : R.string.dialog_title_felica_led_color);

        FragmentDialogLedColorBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_dialog_led_color, null, false);
        builder.setView(binding.getRoot());

        binding.setParam(mParam);
        binding.checkOn.setVisibility(showCheckbox ? View.VISIBLE : View.GONE);

        builder.setPositiveButton(R.string.ok, (dialogInterface, button) -> {
            Fragment target = getTargetFragment();
            int requestCode = getTargetRequestCode();

            if (target != null && target instanceof Listener) {
                ((Listener) target).onSetLedColor(requestCode, mParam.ledColor.get(), mParam.isOn.get());
            }
        });

        builder.setNegativeButton(R.string.cancel, (dialogInterface, button) -> {
            dismiss();
        });

        return builder.create();
    }

    public interface Listener {
        void onSetLedColor(int requestCode, LedColor color, boolean isOn);
    }
}
