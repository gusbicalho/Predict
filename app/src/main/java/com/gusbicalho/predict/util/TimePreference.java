package com.gusbicalho.predict.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimePreference extends DialogPreference {
    private static final Pattern PATTERN_TIME = Pattern.compile("(\\d\\d):(\\d\\d)");
    private static final String FORMAT_TIME = "HH:mm";

    public static Calendar parse(String s) {
        Calendar cal = new GregorianCalendar();
        Matcher m = PATTERN_TIME.matcher(s);
        if (m.matches()) {
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(1)));
            cal.set(Calendar.MINUTE, Integer.parseInt(m.group(2)));
        } else {
            cal.setTimeInMillis(Long.parseLong(s));
        }
        return cal;
    }

    private Calendar calendar;
    private TimePicker picker = null;

    public TimePreference(Context c) {
        this(c, null);
    }

    public TimePreference(Context c, AttributeSet attrs) {
        this(c, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public TimePreference(Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);

        setDialogTitle(null);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        calendar = new GregorianCalendar();
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        picker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
        return picker;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());

            setSummary(getSummary());
            if (callChangeListener(calendar.getTimeInMillis())) {
                persistString(DateFormat.format(FORMAT_TIME, calendar).toString());
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (defaultValue == null) {
            if (restoreValue) {
                calendar = parse(getPersistedString("00:00"));
            } else {
                calendar = parse("00:00");
            }
        } else {
            if (restoreValue) {
                defaultValue = getPersistedString(defaultValue.toString());
            }
            Matcher m = PATTERN_TIME.matcher(defaultValue.toString());
            if (m.matches()) {
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(1)));
                calendar.set(Calendar.MINUTE, Integer.parseInt(m.group(2)));
            } else {
                calendar.setTimeInMillis(Long.parseLong(defaultValue.toString()));
            }
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        if (calendar == null) {
            return null;
        }
        return DateFormat.getTimeFormat(getContext()).format(new Date(calendar.getTimeInMillis()));
    }
}