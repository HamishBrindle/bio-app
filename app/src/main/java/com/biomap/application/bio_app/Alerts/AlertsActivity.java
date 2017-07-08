package com.biomap.application.bio_app.Alerts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.biomap.application.bio_app.Analytics.AnalyticsActivity;
import com.biomap.application.bio_app.Connect.ConnectActivity;
import com.biomap.application.bio_app.Home.MainActivity;
import com.biomap.application.bio_app.Mapping.MappingActivity;
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
    private static final int MAXIMUM_PROGRESS = 30;
    private static final int MINIMUM_PROGRESS = 0;
    private static final int INC_DEC_VALUE = 1;
    public static SharedPreferences SHARED_PREFERENCES;
    public static SharedPreferences.Editor SHARED_PREFERENCES_EDITOR;
    private TextView mTime;
    private DonutProgress mDonutProgress;
    private ToggleButton mToggle;
    private int timerInterval;
    private boolean notificationOn;
    private AlertNotification alertNotification;
    private DrawerLayout mDrawer;

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
        setupToolbar();
        setupAddRemoveButtons();
        setupBottomNavigationView();
    }

    /**
     * Initializes the add and remove buttons for incrementing and decrementing the Alerts interval.
     */
    protected void setupAddRemoveButtons() {

        // Initialize the buttons.
        ImageButton mAdd = (ImageButton) findViewById(R.id.alerts_button_add);
        ImageButton mRemove = (ImageButton) findViewById(R.id.alerts_button_remove);

        // Get toggle button for turning notifications on/off
        mToggle = (ToggleButton) findViewById(R.id.toggle_button_alarm);

        // Get the TextView displaying the time to the user (in middle of donut).
        mTime = (TextView) findViewById(R.id.time);

        // Retrieves the Alert preferences, but if it doesn't exist, sets to default value.
        timerInterval = SHARED_PREFERENCES.getInt(getString(R.string.alert_interval),
                DEFAULT_PROGRESS);
        notificationOn = SHARED_PREFERENCES.getBoolean("notifications",
                false);

        // Load the preferences for the toggle button.
        mToggle.setChecked(notificationOn);

        // Set the progress value of the donut (how filled up it is).
        mDonutProgress.setDonut_progress(String.valueOf(timerInterval));

        // Set the maximum value the donut can fill to.
        mDonutProgress.setMax(MAXIMUM_PROGRESS);

        // Sets the text of the TextView located inside the donut.
        mTime.setText(String.valueOf(timerInterval));

        // Update the shared preferences of the user's Alert preference.
        updateAlarmPreferences(timerInterval);
        updateNotificationPreferences();

        // Create the listeners for the two inc/dec buttons.
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Progress can't exceed maximum value.
                if (!(timerInterval >= MAXIMUM_PROGRESS)) {
                    updateAlarmDisplay(INC_DEC_VALUE, true);
                    updateAlarmPreferences(timerInterval);
                    alertNotification.resetAlarm(timerInterval);
                    mToggle.setChecked(true);
                }
            }
        });
        mRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(timerInterval <= MINIMUM_PROGRESS)) {
                    updateAlarmDisplay(INC_DEC_VALUE, false);
                    updateAlarmPreferences(timerInterval);
                    alertNotification.resetAlarm(timerInterval);
                    mToggle.setChecked(true);
                }
            }
        });

        mToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
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
     * Updates the notification toggle button as per the user's stored preferences.
     */
    protected void updateNotificationPreferences() {

        notificationOn = !notificationOn;
        mToggle.setChecked(notificationOn);

        // Save the notifications preference.
        SHARED_PREFERENCES_EDITOR = SHARED_PREFERENCES.edit();
        SHARED_PREFERENCES_EDITOR.putBoolean("notifications", notificationOn);
        SHARED_PREFERENCES_EDITOR.apply();
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
     * Setup the top-action-bar for navigation, page title, and settings.
     */
    private void setupToolbar() {
        // Set a Toolbar to replace the ActionBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Find drawer view
        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nvView);

        // Setup drawer view
        setupDrawerContent(nvDrawer);

        ImageButton hamburger = (ImageButton) findViewById(R.id.toolbar_hamburger);
        hamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(GravityCompat.START);
            }
        });

    }

    /**
     * Open's the drawer when the hamburger (menu button) is selected.
     *
     * @param item Selected item.
     * @return Selected item as well.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Setup the buttons for inside the navigation drawer in the top-action-bar.
     *
     * @param navigationView The drawer menu.
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    /**
     * Gets user's choice of drawer buttons.
     *
     * @param menuItem The drawer button chosen.
     */
    public void selectDrawerItem(MenuItem menuItem) {

        Intent intent = null;

        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                intent = new Intent(getBaseContext(), MainActivity.class);
                break;
            case R.id.nav_mapping:
                intent = new Intent(getBaseContext(), MappingActivity.class);
                break;
            case R.id.nav_alerts:
                intent = new Intent(getBaseContext(), AlertsActivity.class);
                break;
            case R.id.nav_analytics:
                intent = new Intent(getBaseContext(), AnalyticsActivity.class);
                break;
            case R.id.nav_connect:
                intent = new Intent(getBaseContext(), ConnectActivity.class);
                break;
            default:

        }

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();

        startActivity(intent);
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
