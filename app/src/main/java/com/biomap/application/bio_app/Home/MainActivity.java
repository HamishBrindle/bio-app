package com.biomap.application.bio_app.Home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.biomap.application.bio_app.Alerts.AlertsActivity;
import com.biomap.application.bio_app.Connect.ConnectActivity;
import com.biomap.application.bio_app.Login.BeginActivity;
import com.biomap.application.bio_app.Login.LoginActivity;
import com.biomap.application.bio_app.Login.LoginRegisterActivity;
import com.biomap.application.bio_app.Login.ProfileActivity;
import com.biomap.application.bio_app.Mapping.MappingActivity;
import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.BottomNavigationViewHelper;
import com.biomap.application.bio_app.Utility.CustomFontsLoader;
import com.biomap.application.bio_app.Vitals.VitalsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.Date;

/**
 * Home page.
 * <p>
 * Created by Hamish Brindle on 2017-06-13.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Stores information about user's session.
     */
    public static SharedPreferences SHARED_PREFERENCES;

    private static final String TAG = "MainActivity";
    private static final int ACTIVITY_NUM = 2;

    private DrawerLayout mDrawer;
    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the shared preferences for this instance (i.e. if user has logged in, etc.)
        SHARED_PREFERENCES = this.getSharedPreferences(
                "com.biomap.application.bio_app", Context.MODE_PRIVATE
        );

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();


        // Initialize page elements.
        setupToolbar();
        setupDateBanner();
        setupMenuButtons();
        setupBottomNavigationView();
        setupFirebase();

        ConstraintLayout mMenuButtons = (ConstraintLayout) findViewById(R.id.constraintLayout);
        CustomFontsLoader.overrideFonts(this, mMenuButtons, CustomFontsLoader.GOTHAM_BOLD);


        // TODO: Temp debug button to test animation activity.
        //Remove before deploying
        ImageView mDebugButton = (ImageView) findViewById(R.id.biomap_logo_imageView);
        mDebugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent debugIntent = new Intent(getBaseContext(), BeginActivity.class);
                startActivity(debugIntent);

                // Make switching between activities blend via fade-in / fade-out
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                finish();
            }
        });

    }


    /**
     * Initialize user authentication objects and listeners for authentication state changes.
     */
    private void setupFirebase() {

        final Intent register_login_intent = new Intent(this, LoginRegisterActivity.class);

        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
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
                    startActivity(register_login_intent);
                    finish();
                }
            }
        };

        // Get the user's authentication credentials and check if signed in or not.
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuthListener.onAuthStateChanged(mAuth);
        final Intent setUpIntent = new Intent(this, ProfileActivity.class);

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
     * @param item Menu item selected.
     * @return Icon pressed / to be navigated to.
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
            case R.id.nav_mapping:
                intent = new Intent(getBaseContext(), MappingActivity.class);
                break;
            case R.id.nav_alerts:
                intent = new Intent(getBaseContext(), AlertsActivity.class);
                break;
            case R.id.nav_analytics:
                intent = new Intent(getBaseContext(), VitalsActivity.class);
                break;
            case R.id.nav_connect:
                intent = new Intent(getBaseContext(), ConnectActivity.class);
                break;
            case R.id.nav_sign_out:
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(getBaseContext(), LoginRegisterActivity.class);
                finish();
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
     * Also customizes the bottom navigation so that the buttons don't physically react to being
     * selected. Without this method, the buttons grow and shrink and shift around. It's gross.
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting-up bottom navigation view.");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(MainActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem item = menu.getItem(ACTIVITY_NUM);
        item.setChecked(true);
    }

    /**
     * Creates the menu buttons for the menu fragment.
     */
    public void setupMenuButtons() {

        ImageButton[] menuButtons = {
                (ImageButton) findViewById(R.id.menu_button_mapping),
                (ImageButton) findViewById(R.id.menu_button_alerts),
                (ImageButton) findViewById(R.id.menu_button_analytics),
                (ImageButton) findViewById(R.id.menu_button_connect)
        };

        final Class[] menuActivities = {
                MappingActivity.class,
                AlertsActivity.class,
                VitalsActivity.class,
                ConnectActivity.class
        };

        int numMenuButtons = menuButtons.length;

        for (int i = 0; i < numMenuButtons; i++) {
            final int finalI = i;
            menuButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), menuActivities[finalI]);
                    startActivity(intent);

                    // Make switching between activities blend via fade-in / fade-out
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                }
            });
        }

    }

    public void setupDateBanner() {

        Date date = new Date();

    }

}
