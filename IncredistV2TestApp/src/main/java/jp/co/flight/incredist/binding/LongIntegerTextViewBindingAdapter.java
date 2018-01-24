package jp.co.flight.incredist.binding;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.widget.TextView;

import java.util.Locale;

/**
 * long 値を入力するための BindingAdapter
 */
public class LongIntegerTextViewBindingAdapter {
    private LongIntegerTextViewBindingAdapter() {}

    @BindingAdapter("android:text")
    public static void setTextByte(TextView textView, long value) {
        textView.setText(String.format(Locale.JAPANESE, "%d", value));
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static long getLong(TextView textView) {
        try {
            return Long.parseLong(textView.getText().toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
