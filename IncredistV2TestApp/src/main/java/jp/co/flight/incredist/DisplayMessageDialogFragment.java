package jp.co.flight.incredist;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import jp.co.flight.incredist.android.IncredistV2TestApp.R;
import jp.co.flight.incredist.android.IncredistV2TestApp.databinding.FragmentDialogDisplayMessageBinding;

/**
 * メッセージ機能設定用ダイアログ
 */
public class DisplayMessageDialogFragment extends DialogFragment {

    private static final String ARG_KEY_TITLE = "arg_key_title";
    private static final String ARG_KEY_MESSAGE_TYPE = "arg_key_message_type";
    private static final String ARG_KEY_MESSAGE = "arg_key_message";

    interface Listener {
        void onDisplayMessage(int requestCode, int type, String message);
    }

    /**
     * ダイアログのインスタンスを生成します.
     *
     * @param title タイトル
     * @param type メッセージ番号
     * @param message メッセージ
     * @return DisplayMessageDialogFragment のインスタンス
     */
    public static DisplayMessageDialogFragment newInstance(String title, int type, String message) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY_TITLE, title);
        args.putInt(ARG_KEY_MESSAGE_TYPE, type);
        args.putString(ARG_KEY_MESSAGE, message);

        DisplayMessageDialogFragment fragment = new DisplayMessageDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public DisplayMessageDialogFragment() {

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        Bundle args = getArguments();

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(args.getString(ARG_KEY_TITLE));

        final FragmentDialogDisplayMessageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_dialog_display_message, null, false);
        builder.setView(binding.getRoot());

        binding.editType.setText(String.valueOf(args.getInt(ARG_KEY_MESSAGE_TYPE)));
        binding.editMessage.setText(args.getString(ARG_KEY_MESSAGE));

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Fragment target = getTargetFragment();
                int requestCode = getTargetRequestCode();

                if (target != null && target instanceof Listener) {
                    ((Listener) target).onDisplayMessage(requestCode, Integer.parseInt(binding.editType.getText().toString()), binding.editMessage.getText().toString());
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });

        return builder.create();
    }
}
