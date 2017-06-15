package com.example.hamishbrindle.bio_app.Home;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.hamishbrindle.bio_app.Login.LoginActivity;
import com.example.hamishbrindle.bio_app.R;
import com.example.hamishbrindle.bio_app.Utility.BottomNavigationViewHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int ACTIVITY_NUM = 2;

    /**
     * Stores information about user's session.
     */
    public static SharedPreferences SHARED_PREFERENCES;

    /**
     * Shared Preference: User has logged in and doesn't need to again.
     */
    public static Boolean MOBILE_REGISTER_FLAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Starting.");

        // Get the shared preferences for this instance (i.e. if user has logged in, etc.)
        SHARED_PREFERENCES = this.getSharedPreferences(
            "com.example.hamishbrindle.bio_app", Context.MODE_PRIVATE
        );

        // Executes only once upon start-up; signals for login
        if (SHARED_PREFERENCES.getBoolean("FIRST_EXECUTE", true)) {

            // Indicating whether user has registered/logged in or not.
            MOBILE_REGISTER_FLAG = SHARED_PREFERENCES.getBoolean("MOBILE_REGISTER_FLAG", false);
            Log.d(TAG, "MOBILE_REGISTER_FLAG: setting flag to FALSE.");

            // Set FIRST_EXECUTE to false so not to trigger login screen again.
            SHARED_PREFERENCES.edit().putBoolean("FIRST_EXECUTE", false).apply();
        }

        // Check if user has registered/logged in
        checkForUser();

        // Initialize the navigation bar (bottom) and the pager (top)
        setupBottomNavigationView();
        setupViewPager();

    }

    /**
     * Checks if a user has logged in.
     */
    private void checkForUser() {
        // User has NOT logged in yet.
        if (!SHARED_PREFERENCES.getBoolean("MOBILE_REGISTER_FLAG", false)) {
            Intent intent = new Intent(this,
                    LoginActivity.class);
            startActivity(intent);
        } else {
            Log.d(TAG, "User is already logged in.");
        }
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
        tabMenu.setIcon(R.drawable.ic_hamburger);

        TabLayout.Tab tabBiomap = tabLayout.getTabAt(1);
        // TODO: This tab doesnt need an icon
        // tabBiomap.setIcon(R.drawable.ic_logo);

        TabLayout.Tab tabSettings = tabLayout.getTabAt(2);
        tabSettings.setIcon(R.drawable.ic_settings);

        // Starts the MainActivty HomeFragment when booted
        TabLayout.Tab tabHome = tabLayout.getTabAt(1);
        assert tabHome != null; // To get rid of a NullPointerException warning on next line
        viewPager.setCurrentItem(tabHome.getPosition());

    }

    /**
     * Sets up and enables the bottom navigation bar for each activity.
     *
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
