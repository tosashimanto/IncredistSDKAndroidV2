package jp.co.flight.incredist.binding;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import jp.co.flight.incredist.android.model.EmvTagType;

/**
 * RadioGroup と Enum 値を結びつける BindingAdapter
 */
public class EnumRadioGroupBindingAdapter {
    private EnumRadioGroupBindingAdapter() {
    }

    @BindingAdapter("itemValue")
    public static <E extends Enum<E>> void setEnumValue(RadioGroup radioGroup, E value) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View v = radioGroup.getChildAt(i);

            if (v instanceof RadioButton && v.getTag() == value) {
                ((RadioButton) v).setChecked(true);
            }
        }
    }

    @BindingAdapter("itemValueAttrChanged")
    public static void setItemValueAttrChanged(RadioGroup group, InverseBindingListener listener) {
        if (listener == null) {
            group.setOnCheckedChangeListener(null);
        } else {
            group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    listener.onChange();
                }
            });
        }
    }

    @InverseBindingAdapter(attribute = "itemValue")
    public static EmvTagType getEnumValue(RadioGroup radioGroup) {
        View v = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
        if (v instanceof RadioButton) {
            return (EmvTagType) ((RadioButton) v).getTag();
        }

        return null;
    }
}
