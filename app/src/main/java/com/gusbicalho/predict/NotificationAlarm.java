package com.gusbicalho.predict;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.gusbicalho.predict.util.Utility;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class NotificationAlarm extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 4242;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            setAlarm(context);
            return;
        }
        if (!Utility.allowNotifications(context)) {
            return;
        }
        long lastNot = Utility.getLastNotification(context);
        if (lastNot + 24*3600000 > System.currentTimeMillis()) { // if diff is less than 24h, check dates
            Calendar lastNotification = new GregorianCalendar();
            lastNotification.setTimeInMillis(Utility.getLastNotification(context));
            Calendar now = new GregorianCalendar();
            now.setTimeInMillis(System.currentTimeMillis());
            if (lastNotification.get(Calendar.DATE) == now.get(Calendar.DATE)) { // Already made a prediction today
                return;
            }
        }
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        NotificationCompat.Builder notBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.comment_chart_hole)
                        .setContentTitle(context.getString(R.string.daily_notification_title))
                        .setAutoCancel(true);
        PendingIntent pendingIntent =
                TaskStackBuilder.create(context)
                        .addNextIntent(new Intent(context, MainActivity.class))
                        .addNextIntent(new Intent(context, NewPredictionActivity.class))
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notBuilder.build());

        wl.release();
    }

    public static void setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, NotificationAlarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        Calendar alarmTime = Utility.notificationTime(context);

        Calendar now = new GregorianCalendar();
        now.setTimeInMillis(System.currentTimeMillis());
        now.set(Calendar.HOUR_OF_DAY, alarmTime.get(Calendar.HOUR_OF_DAY));
        now.set(Calendar.MINUTE, alarmTime.get(Calendar.MINUTE));
        if (now.getTimeInMillis() < System.currentTimeMillis()) {
            now.add(Calendar.DATE, 1);
        }

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi); // Millisec * Second * Minute
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, NotificationAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
