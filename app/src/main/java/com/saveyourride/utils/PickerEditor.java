package com.saveyourride.utils;

import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.lang.reflect.Field;

public class PickerEditor {
    //In this utility class implement functions to edit pickers at runtime

    public static void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) try {
                Field selectorWheelPaintField = numberPicker.getClass()
                        .getDeclaredField("mSelectorWheelPaint");
                selectorWheelPaintField.setAccessible(true);
                ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                ((EditText) child).setTextColor(color);
                numberPicker.invalidate();
                return;
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setDividerColor(NumberPicker numberPicker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(numberPicker, colorDrawable);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
