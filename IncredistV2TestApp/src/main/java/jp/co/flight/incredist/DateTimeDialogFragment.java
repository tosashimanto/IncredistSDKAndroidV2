package jp.co.flight.incredist;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import jp.co.flight.incredist.android.IncredistV2TestApp.R;
import jp.co.flight.incredist.android.IncredistV2TestApp.databinding.FragmentDialogDateTimeBinding;

/**
 * 日時設定用ダイアログ
 */
public class DateTimeDialogFragment extends DialogFragment {
    public DateTimeDialogFragment() {
    }

    public static DateTimeDialogFragment newInstance() {
        DateTimeDialogFragment fragment = new DateTimeDialogFragment();
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_date_time);

        FragmentDialogDateTimeBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_dialog_date_time, null, false);
        builder.setView(binding.getRoot());

        builder.setPositiveButton(R.string.ok, (dialogInterface, button) -> {
            Fragment target = getTargetFragment();
            int requestCode = getTargetRequestCode();

            if (target != null && target instanceof Listener) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd HHmmss", Locale.JAPANESE);

                try {
                    Date dt = dateFormat.parse(binding.editDate.getText() + " " + binding.editTime.getText().toString());

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dt);

                    ((Listener) target).onSetDateTime(requestCode, cal);
                } catch (ParseException ex) {
                    // ignore.
                }
            }
        });

        return builder.create();
    }

    public interface Listener {
        void onSetDateTime(int requestCode, Calendar cal);
    }
}
