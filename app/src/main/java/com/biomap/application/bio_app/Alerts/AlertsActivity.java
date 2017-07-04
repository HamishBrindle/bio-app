package com.biomap.application.bio_app.Alerts;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.BottomNavigationViewHelper;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Allows user to define Alert intervals for their positioning reminders.
 * <p>
 * Created by Hamish Brindle on 2017-06-13.
 */

public class AlertsActivity extends AppCompatActivity {

    private static final String TAG = "AlertsActivity";

    private static final int ACTIVITY_NUM = 1;

    private static final int DEFAULT_PROGRESS = 15;

    private static final int MAXIMUM_PROGRESS = 20;

    private static final int MINIMUM_PROGRESS = 0;

    private static final int INC_DEC_VALUE = 1;

    public static SharedPreferences SHARED_PREFERENCES;

    public static SharedPreferences.Editor SHARED_PREFERENCES_EDITOR;

    private TextView mTime;

    private DonutProgress mDonutProgress;

    private int timerInterval;

    private boolean notificationOn;

    private AlertNotification alertNotification;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        Log.d(TAG, "onCreate: starting.");

        alertNotification = new AlertNotification(getApplicationContext());
        mDonutProgress = (DonutProgress) findViewById(R.id.donut_progress);

        // Get the shared preferences for this instance (i.e. if user has logged in, etc.)
        SHARED_PREFERENCES = this.getSharedPreferences(
                "com.biomap.application.bio_app.ALARM_PREFERENCES", Context.MODE_PRIVATE
        );


        // Initialize page elements.
        setupBottomNavigationView();
        setupAddRemoveButtons();
    }

    /**
     * Initializes the add and remove buttons for incrementing and decrementing the Alerts interval.
     */
    protected void setupAddRemoveButtons() {

        // Initialize the buttons.
        ImageButton mAdd = (ImageButton) findViewById(R.id.alerts_button_add);
        ImageButton mRemove = (ImageButton) findViewById(R.id.alerts_button_remove);
        final ToggleButton mToggle = (ToggleButton) findViewById(R.id.toggle_button_alarm);

        // Get the TextView displaying the time to the user (in middle of donut).
        mTime = (TextView) findViewById(R.id.time);

        // Retrieves the Alert preferences, but if it doesn't exist, sets to default value.
        timerInterval = SHARED_PREFERENCES.getInt(getString(R.string.alert_interval),
                DEFAULT_PROGRESS);

        // Set the progress value of the donut (how filled up it is).
        mDonutProgress.setDonut_progress(String.valueOf(timerInterval));

        // Set the maximum value the donut can fill to.
        mDonutProgress.setMax(MAXIMUM_PROGRESS);

        // Sets the text of the TextView located inside the donut.
        mTime.setText(String.valueOf(timerInterval));

        // Update the shared preferences of the user's Alert preference.
        updateAlarmPreferences(timerInterval);

        // Create the listeners for the two inc/dec buttons.
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Progress can't exceed maximum value.
                if (!(timerInterval >= MAXIMUM_PROGRESS)) {
                    updateAlarmDisplay(INC_DEC_VALUE, true);
                    updateAlarmPreferences(timerInterval);
                    mToggle.setChecked(false);
                }
            }
        });
        mRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(timerInterval <= MINIMUM_PROGRESS)) {
                    updateAlarmDisplay(INC_DEC_VALUE, false);
                    updateAlarmPreferences(timerInterval);
                    mToggle.setChecked(false);

                }
            }
        });

        mToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "onCheckedChanged: " + timerInterval);
                    long firstTime = alertNotification.setTime(Calendar.MINUTE, timerInterval);
                    alertNotification.setAlarmManagerRepeating(firstTime, TimeUnit.MINUTES.toMillis(timerInterval));
                    Log.d(TAG, "onCheckedChanged: Alarm Set");
                } else {
                    alertNotification.cancelAlarm();
                    Log.d(TAG, "onCheckedChanged: Alarm Cancelled");
                }
            }
        });
    }

    /**
     * Updates the display of the Alert timer.
     *
     * @param value     How much to increment or decrement the timer.
     * @param increment If true, increase the value - else decrease it.
     */
    protected void updateAlarmDisplay(int value, boolean increment) {
        if (increment) {
            timerInterval += value;
            mDonutProgress.setDonut_progress(Integer.toString(timerInterval));
        } else {
            timerInterval -= value;
            mDonutProgress.setDonut_progress(Integer.toString(timerInterval));
        }

        mTime.setText(String.valueOf(timerInterval));
    }

    /**
     * Update the saved value of the user's Alert interval.
     *
     * @param newInterval New value of the alert interval preference.
     */
    protected void updateAlarmPreferences(int newInterval) {
        SHARED_PREFERENCES_EDITOR = SHARED_PREFERENCES.edit();
        SHARED_PREFERENCES_EDITOR.putInt(getString(R.string.alert_interval), newInterval);
        SHARED_PREFERENCES_EDITOR.apply();
    }

    /**
     * Sets up and enables the bottom navigation bar for each activity.
     * <p>
     * Also customizes the bottom navigation so that the buttons don't physically react to being
     * selected. Without this method, the buttons grow and shrink and shift around. It's gross.
     */
    protected void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting-up bottom navigation view.");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(AlertsActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem item = menu.getItem(ACTIVITY_NUM);
        item.setChecked(true);
    }
}
