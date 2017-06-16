package com.biomap.application.bio_app.Home;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.BottomNavigationViewHelper;
import com.biomap.application.bio_app.Login.LoginActivity;
import com.biomap.application.bio_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

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

    /**
     * Shared Preference: User has logged in and doesn't need to again.
     */
    public static Boolean MOBILE_REGISTER_FLAG;

    /**
     * User's login credentials.
     */
    private FirebaseAuth mAuth;

    /**
     * Listener for login/logout state changes.
     */
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Starting.");

        // Get the shared preferences for this instance (i.e. if user has logged in, etc.)
        SHARED_PREFERENCES = this.getSharedPreferences(
                "com.example.hamishbrindle.bio_app", Context.MODE_PRIVATE
        );

        // Initialize the navigation bar (bottom) and the pager (top)
        setupBottomNavigationView();
        setupViewPager();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged.Main:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged.Main:signed_out");
                    // Create the logout activity intent.
                    Intent logOutIntent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(logOutIntent);
                    finish();
                }
            }
        };

        // Get the user's authentication credentials and check if signed in or not.
        mAuth = FirebaseAuth.getInstance();
        mAuthListener.onAuthStateChanged(mAuth);

        // Create Logout button on the main page for testing.
        // TODO: Move logout button to settings fragment.
        Button mSignOut = (Button) findViewById(R.id.singoutbtn);
        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: signed out button");
                // Sign out the user.
                mAuth.signOut();
                // Check if user is signed out.
                // TODO: Preferably, we'd like to not call this manually.
                mAuthListener.onAuthStateChanged(mAuth);
            }
        });
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
     * <p>
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
