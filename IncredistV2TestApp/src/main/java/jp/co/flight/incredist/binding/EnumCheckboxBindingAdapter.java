package jp.co.flight.incredist.binding;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import java.util.EnumSet;

import jp.co.flight.incredist.android.model.CreditCardType;

/**
 * Checkbox と EnumSet 値を結びつける BindingAdapter
 */
public class EnumCheckboxBindingAdapter {
    private EnumCheckboxBindingAdapter() {
    }

    @BindingAdapter({"enumSet"})
    public static void setEnumSet(CheckBox checkBox, EnumSet<CreditCardType> valueSet) {
        if (valueSet.contains(checkBox.getTag())) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }

        // 共通の親の tag に書き込んでいるので注意が必要
        ((View) checkBox.getParent()).setTag(valueSet);
    }

    @InverseBindingAdapter(attribute = "enumSet", event = "android:checkedAttrChanged")
    public static EnumSet<CreditCardType> getEnumSet(CheckBox checkBox) {
        ViewGroup parent = (ViewGroup) checkBox.getParent();
        EnumSet<CreditCardType> result = EnumSet.noneOf(CreditCardType.class);

        for (int i = 0; i < parent.getChildCount(); i++) {
            View v = parent.getChildAt(i);

            if (v instanceof CheckBox && v.getTag() instanceof CreditCardType && ((CheckBox) v).isChecked()) {
                result.add((CreditCardType) v.getTag());
            }
        }

        return result;
    }
}
