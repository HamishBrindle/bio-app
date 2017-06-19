package com.biomap.application.bio_app.Home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.biomap.application.bio_app.Alerts.AlertsActivity;
import com.biomap.application.bio_app.Analytics.AnalyticsActivity;
import com.biomap.application.bio_app.Connect.ConnectActivity;
import com.biomap.application.bio_app.Login.LoginActivity;
import com.biomap.application.bio_app.Mapping.MappingActivity;
import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.BottomNavigationViewHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /**
     * Activity indicator.
     */
    private static final String TAG = "MainActivity";

    /**
     * The corresponding activity number allowing for bottom toolbar to switch between activities.
     */
    private static final int ACTIVITY_NUM = 2;

    /**
     * Stores information about user's session.
     */
    public static SharedPreferences SHARED_PREFERENCES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Starting.");

        // Get the shared preferences for this instance (i.e. if user has logged in, etc.)
        SHARED_PREFERENCES = this.getSharedPreferences(
                "com.biomap.application.bio_app", Context.MODE_PRIVATE
        );

        // Initialize the navigation bar (bottom) and the pager (top)
        setupBottomNavigationView();
        setupViewPager();
    }

    /**
     * Adds menu, home, and settings fragments to the MainActivity.
     */
    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new MenuFragment());
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new SettingsFragment());

        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        TabLayout.Tab tabMenu = tabLayout.getTabAt(0);
        assert tabMenu != null;
        tabMenu.setIcon(R.drawable.ic_hamburger);

        TabLayout.Tab tabSettings = tabLayout.getTabAt(2);
        assert tabSettings != null;
        tabSettings.setIcon(R.drawable.ic_settings);

        // Starts the MainActivity HomeFragment when booted
        TabLayout.Tab tabHome = tabLayout.getTabAt(1);
        assert tabHome != null;
        viewPager.setCurrentItem(tabHome.getPosition());

    }

    /**
     * Sets up and enables the bottom navigation bar for each activity.
     * Also customizes the bottom navigation so that the buttons don't physically react to being
     * selected. Without this method, the buttons grow and shrink and shift around. It's gross.
     */
    public void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting-up bottom navigation view.");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(MainActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem item = menu.getItem(ACTIVITY_NUM);
        item.setChecked(true);
    }
}
