package jp.co.flight.incredist.binding;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.widget.TextView;

import java.util.Locale;

/**
 * int 値を入力するための BindingAdapter
 */
public class IntegerTextViewBindingAdapter {
    private IntegerTextViewBindingAdapter() {}

    @BindingAdapter("android:text")
    public static void setTextByte(TextView textView, int value) {
        textView.setText(String.format(Locale.JAPANESE, "%d", value));
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static int getByte(TextView textView) {
        try {
            return Integer.parseInt(textView.getText().toString());
        } catch (NumberFormatException ex) {
            return (byte) 0;
        }
    }
}
