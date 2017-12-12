package jp.co.flight.incredist.binding;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.widget.TextView;

import java.util.Locale;

/**
 * byte 値を入力するための BindingAdapter
 */
public class ByteTextViewBindingAdapter {
    private ByteTextViewBindingAdapter() {}

    @BindingAdapter("android:text")
    public static void setTextByte(TextView textView, byte value) {
        textView.setText(String.format(Locale.JAPANESE, "%02x", value));
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static byte getByte(TextView textView) {
        try {
            return (byte) (Integer.parseInt(textView.getText().toString(), 16) & 0xff);
        } catch (NumberFormatException ex) {
            return (byte) 0;
        }
    }
}
