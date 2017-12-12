package jp.co.flight.incredist.binding;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.widget.Spinner;

import jp.co.flight.incredist.EnumSpinnerAdapter;
import jp.co.flight.incredist.android.model.EncryptionMode;

/**
 * EnumSpinnerAdapter 用の BindingAdapter 定義
 *
 * android:selectedItemPosition は DataBinding ライブラリ側で定義済みのプロパティ
 */
public class EnumSpinnerBindingAdapter {

    private EnumSpinnerBindingAdapter() {
    }

    @BindingAdapter({"enumValues", "android:selectedItemPosition"})
    public static <E extends Enum<E>> void setEnumValues(Spinner spinner, E[] values, E selection) {
        spinner.setAdapter(new EnumSpinnerAdapter<>(spinner.getContext(), values));
        spinner.setSelection(selection.ordinal());
    }

    @InverseBindingAdapter(attribute = "android:selectedItemPosition")
    public static EncryptionMode.CipherMethod getCipherMethod(Spinner spinner) {
        return EncryptionMode.CipherMethod.values()[spinner.getSelectedItemPosition()];
    }

    @InverseBindingAdapter(attribute = "android:selectedItemPosition")
    public static EncryptionMode.BlockCipherMode getBlockCipherMode(Spinner spinner) {
        return EncryptionMode.BlockCipherMode.values()[spinner.getSelectedItemPosition()];
    }

    @InverseBindingAdapter(attribute = "android:selectedItemPosition")
    public static EncryptionMode.DsConstant getDsConstant(Spinner spinner) {
        return EncryptionMode.DsConstant.values()[spinner.getSelectedItemPosition()];
    }

    @InverseBindingAdapter(attribute = "android:selectedItemPosition")
    public static EncryptionMode.PaddingMode getPaddingMode(Spinner spinner) {
        return EncryptionMode.PaddingMode.values()[spinner.getSelectedItemPosition()];
    }
}
