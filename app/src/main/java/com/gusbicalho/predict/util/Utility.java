package com.gusbicalho.predict.util;

import android.content.Context;
import android.preference.PreferenceManager;

import com.gusbicalho.predict.R;

import java.util.Calendar;

public class Utility {
    public static boolean allowNotifications(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pref_key_daily_notifications), true);
    }

    public static Calendar notificationTime(Context context) {
        return TimePreference.parse(
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getString(
                                context.getString(R.string.pref_key_notification_time),
                                context.getString(R.string.pref_default_notification_time)));
    }

    public static void setLastPrediction(Context context, long time) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(context.getString(R.string.pref_key_last_notification), time)
                .commit();
    }

    public static long getLastNotification(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.pref_key_last_notification), Long.MIN_VALUE);
    }
}
