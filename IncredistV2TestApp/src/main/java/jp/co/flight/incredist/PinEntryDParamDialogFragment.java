package jp.co.flight.incredist;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import jp.co.flight.incredist.android.IncredistV2TestApp.R;
import jp.co.flight.incredist.android.IncredistV2TestApp.databinding.FragmentDialogPinEntryDParamBinding;
import jp.co.flight.incredist.model.PinEntryDParam;

/**
 * pinEntryD 用の設定ダイアログ
 */
public class PinEntryDParamDialogFragment extends DialogFragment {
    private static final String ARG_KEY_PARAM = "arg_key_param";

    public PinEntryDParamDialogFragment() {
        // Required empty public constructor
    }

    public static PinEntryDParamDialogFragment newInstance(@Nullable PinEntryDParam param) {
        PinEntryDParamDialogFragment fragment = new PinEntryDParamDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_KEY_PARAM, param);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        Bundle args = getArguments();
        PinEntryDParam argParam = args.getParcelable(ARG_KEY_PARAM);

        final PinEntryDParam param;
        if (argParam != null) {
            param = argParam;
        } else {
            param = new PinEntryDParam(4, 1, 0, 4, 1);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_encryption_setting);

        final FragmentDialogPinEntryDParamBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_dialog_pin_entry_d_param, null, false);
        builder.setView(binding.getRoot());

        binding.setSetting(param);

        builder.setPositiveButton(R.string.ok, (dialogInterface, button) -> {
            Fragment target = getTargetFragment();
            int requestCode = getTargetRequestCode();

            if (target != null && target instanceof Listener) {
                ((Listener) target).onSetPinEntryDParam(requestCode, param);
            }
        });

        builder.setNegativeButton(R.string.cancel, (dialogInterface, button) -> {
            dismiss();
        });

        return builder.create();
    }

    public interface Listener {
        void onSetPinEntryDParam(int requestCode, PinEntryDParam param);
    }
}
