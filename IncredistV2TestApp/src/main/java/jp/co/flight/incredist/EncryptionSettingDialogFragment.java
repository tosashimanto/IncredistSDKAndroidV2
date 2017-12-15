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
import jp.co.flight.incredist.android.IncredistV2TestApp.databinding.FragmentDialogEncryptSettingBinding;
import jp.co.flight.incredist.android.model.EncryptionMode;

/**
 * 暗号化モード設定ダイアログ
 */
public class EncryptionSettingDialogFragment extends DialogFragment {

    private static final String ARG_KEY_ENCRYPTION_MODE = "arg_key_encryption_mode";

    public EncryptionSettingDialogFragment() {
    }

    public static EncryptionSettingDialogFragment newInstance(@Nullable EncryptionMode mode) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_KEY_ENCRYPTION_MODE, mode);

        EncryptionSettingDialogFragment fragment = new EncryptionSettingDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        Bundle args = getArguments();
        EncryptionMode argMode = args.getParcelable(ARG_KEY_ENCRYPTION_MODE);

        final EncryptionMode mode;
        if (argMode != null) {
            mode = argMode;
        } else {
            mode = new EncryptionMode((byte) 1, EncryptionMode.CipherMethod.DUKTPAES, EncryptionMode.BlockCipherMode.ECB,
                    EncryptionMode.DsConstant.DataEncryptionRequest, EncryptionMode.PaddingMode.FixedData, (byte) 0xff, true);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_encryption_setting);

        final FragmentDialogEncryptSettingBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_dialog_encrypt_setting, null, false);
        builder.setView(binding.getRoot());

        binding.setEncryptionMode(mode);

        builder.setPositiveButton(R.string.ok, (dialogInterface, button) -> {
            Fragment target = getTargetFragment();
            int requestCode = getTargetRequestCode();

            if (target != null && target instanceof Listener) {
                ((Listener) target).onSetEncryptionSetting(requestCode, mode);
            }
        });

        builder.setNegativeButton(R.string.cancel, (dialogInterface, button) -> {
            dismiss();
        });

        return builder.create();
    }

    public interface Listener {
        void onSetEncryptionSetting(int requestCode, EncryptionMode mode);
    }
}
