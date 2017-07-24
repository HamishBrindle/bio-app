package com.biomap.application.bio_app.Alerts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * AlarmNotfication sets up the alarms needed to trigger the alerts.
 */

class AlertNotification {
    private static final String TAG = "AlertNotigication";
    private Calendar calendar;
    private PendingIntent mAlarmSender;
    private AlarmManager am;
    Intent intent;

    /**
     * The constructor of this class, setting up the instance variables.
     *
     * @param context the context of the class calling this object.
     */
    AlertNotification(Context context) {
        calendar = Calendar.getInstance();
        intent = new Intent(context, NotificationPublisher.class);
        mAlarmSender = PendingIntent.getBroadcast(context, 0, intent, 0);
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Gets the pending intent for the alarm manager.
     *
     * @return mAlarmSender the pending intent.
     */
    public PendingIntent getAlarmSender() {
        return mAlarmSender;
    }

    /**
     * Sets up the time for the alarm manager to go off.
     *
     * @param origin The unit which you want to add to, eg. Calender.MINUTE
     * @param toAdd  the amount you want to add to the origin.
     * @return the final added time.
     */
    long setTime(int origin, int toAdd) {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(origin, toAdd);
        long scheduledTime = calendar.getTimeInMillis();
        Log.d(TAG, "setTime: Time Set ");
        return scheduledTime;

    }

    /**
     * Set up the alarm manager to go off once at the given time.
     *
     * @param notificationTime the time for the alarm to go off.
     */
    public void setAlarmManager(long notificationTime) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, notificationTime, mAlarmSender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, notificationTime, mAlarmSender);
        }
        Log.d(TAG, "setAlarmManager: Alarm Set");
    }

    /**
     * Set up the alarm manger to go off repeatedly.
     *
     * @param notificationTime the first time the alarm goes off
     * @param interval         the interval in which the alarm goes off.
     */
    void setAlarmManagerRepeating(long notificationTime, long interval) {
        am.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime, interval, mAlarmSender);
    }

    /**
     * Cancels the alarm manager.
     */
    void cancelAlarm() {
        am.cancel(mAlarmSender);
    }

    /**
     * Reset the alarm.
     */
    void resetAlarm(int newInterval) {
        this.cancelAlarm();
        long init = setTime(Calendar.MINUTE, newInterval);
        setAlarmManagerRepeating(init, TimeUnit.MINUTES.toMillis(newInterval));
    }

    boolean isset(Context context) {
        PendingIntent piCheck = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);

        if (piCheck != null) {
            Log.d(TAG, "piCheck returned NOT NULL and probably returned pi");
            return true;
        } else {
            Log.d(TAG, "piCheck returned NULL pi does not exist");
            return false;
        }

    }
}
