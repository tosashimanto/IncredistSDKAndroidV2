package jp.co.flight.incredist.binding;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.widget.TextView;

import java.util.Locale;

/**
 * int/long 値を入力するための BindingAdapter
 */
public class IntegerTextViewBindingAdapter {
    private IntegerTextViewBindingAdapter() {
    }

    @BindingAdapter("android:text")
    public static void setTextLong(TextView textView, long value) {
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

    @BindingAdapter("android:text")
    public static void setTextInt(TextView textView, int value) {
        textView.setText(String.format(Locale.JAPANESE, "%d", value));
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static int getInt(TextView textView) {
        try {
            return Integer.parseInt(textView.getText().toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
