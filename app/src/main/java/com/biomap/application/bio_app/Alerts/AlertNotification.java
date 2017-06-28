package com.biomap.application.bio_app.Alerts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Vafa on 2017-06-27.
 */


public class AlertNotification {
    private static final String TAG = "AlertNotigication";
    private Calendar calendar;
    private PendingIntent mAlarmSender;
    private AlarmManager am;

    public PendingIntent getAlarmSender() {
        return mAlarmSender;
    }
    //The constructor for this class, setting up all of the alarm managers.

    public AlertNotification(Context context) {
        calendar = Calendar.getInstance();
        mAlarmSender = PendingIntent.getBroadcast(context, 0, new Intent(context, NotificationPublisher.class), 0);
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    }

    //Used to set the time you want the alarm to first go off.
    public long setTime(int origin, int toAdd) {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(origin, toAdd);
        long scheduledTime = calendar.getTimeInMillis();
        Log.d(TAG, "setTime: Time Set ");
        return scheduledTime;

    }

    //Setting up the alarm manager so it goes off at the given time.
    public void setAlarmManager(long notificationTime) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, notificationTime, mAlarmSender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, notificationTime, mAlarmSender);
        }
        Log.d(TAG, "setAlarmManager: Alarm Set");
    }

    //Setting up the repeating alarm manager, goes off the first time at 'notficatinTime' and repeats at every interval.
    public void setAlarmManagerRepeating(long notificationTime, long interval) {
        am.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime, interval, mAlarmSender);
        Log.d(TAG, "setAlarmManagerRepeating: Repeating Alarm Set");
    }

    //Cancel the repeating alarm.
    public void cancelAlarm(PendingIntent alarmListener) {
        am.cancel(alarmListener);
        Log.d(TAG, "cancelAlarm: Alarm Cancelled");

    }

}
