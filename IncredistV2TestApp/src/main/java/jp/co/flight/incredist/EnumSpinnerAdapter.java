package jp.co.flight.incredist;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * Enum から選択するための Adapter
 */
public class EnumSpinnerAdapter<E extends Enum<E>> extends ArrayAdapter<E> {
    public EnumSpinnerAdapter(Context context, E[] enumValues) {
        super(context, android.R.layout.simple_spinner_item, enumValues);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
}
