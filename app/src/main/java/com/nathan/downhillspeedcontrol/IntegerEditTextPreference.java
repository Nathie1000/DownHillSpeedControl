package com.nathan.downhillspeedcontrol;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class IntegerEditTextPreference extends EditTextPreference {

    public IntegerEditTextPreference(final Context context) {
        super(context);
    }

    public IntegerEditTextPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public IntegerEditTextPreference(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected String getPersistedString(final String defaultReturnValue) {
        int defaultAsInt;
        try {
            defaultAsInt = Integer.parseInt(defaultReturnValue);
        }
        catch (NumberFormatException e) {
            // No default is set
            defaultAsInt = 0;
        }

        final int intValue = getPersistedInt(defaultAsInt);
        return Integer.toString(intValue);
    }

    @Override
    protected boolean persistString(final String value) {
        try {
            return persistInt(Integer.parseInt(value));
        }
        catch (NumberFormatException e) {
            // This shouldn't happen as long as it has inputType="number"
            return false;
        }
    }
}
