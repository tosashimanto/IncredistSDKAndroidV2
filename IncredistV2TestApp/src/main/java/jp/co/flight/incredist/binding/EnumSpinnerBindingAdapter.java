package jp.co.flight.incredist.binding;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import jp.co.flight.incredist.EnumSpinnerAdapter;
import jp.co.flight.incredist.android.model.EmvTransactionType;
import jp.co.flight.incredist.android.model.EncryptionMode;
import jp.co.flight.incredist.android.model.LedColor;

/**
 * EnumSpinnerAdapter 用の BindingAdapter 定義
 *
 * android:selectedItemPosition は DataBinding ライブラリ側で定義済みのプロパティ
 */
public class EnumSpinnerBindingAdapter {

    private EnumSpinnerBindingAdapter() {
    }

    @BindingAdapter({"enumValues", "itemValue"})
    public static <E extends Enum<E>> void setEnumValues(Spinner spinner, E[] values, E selection) {
        spinner.setAdapter(new EnumSpinnerAdapter<>(spinner.getContext(), values));
        spinner.setSelection(selection.ordinal());
    }

    @BindingAdapter("itemValueAttrChanged")
    public static void setItemValueAttrChanged(Spinner spinner, InverseBindingListener listener) {
        if (listener == null) {
            spinner.setOnItemSelectedListener(null);
        } else {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    listener.onChange();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    listener.onChange();
                }
            });
        }
    }

    @InverseBindingAdapter(attribute = "itemValue")
    public static EncryptionMode.CipherMethod getCipherMethod(Spinner spinner) {
        return EncryptionMode.CipherMethod.values()[spinner.getSelectedItemPosition()];
    }

    @InverseBindingAdapter(attribute = "itemValue")
    public static EncryptionMode.BlockCipherMode getBlockCipherMode(Spinner spinner) {
        return EncryptionMode.BlockCipherMode.values()[spinner.getSelectedItemPosition()];
    }

    @InverseBindingAdapter(attribute = "itemValue")
    public static EncryptionMode.DsConstant getDsConstant(Spinner spinner) {
        return EncryptionMode.DsConstant.values()[spinner.getSelectedItemPosition()];
    }

    @InverseBindingAdapter(attribute = "itemValue")
    public static EncryptionMode.PaddingMode getPaddingMode(Spinner spinner) {
        return EncryptionMode.PaddingMode.values()[spinner.getSelectedItemPosition()];
    }

    @InverseBindingAdapter(attribute = "itemValue")
    public static LedColor getLedColor(Spinner spinner) {
        return LedColor.values()[spinner.getSelectedItemPosition()];
    }

    @InverseBindingAdapter(attribute = "itemValue")
    public static EmvTransactionType getTransactionType(Spinner spinner) {
        return EmvTransactionType.values()[spinner.getSelectedItemPosition()];
    }
}
