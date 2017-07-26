package com.biomap.application.bio_app.Home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
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
import android.widget.TextView;

import com.biomap.application.bio_app.Alerts.AlertsActivity;
import com.biomap.application.bio_app.Bluetooth.BluetoothHelper;
import com.biomap.application.bio_app.Connect.ConnectActivity;
import com.biomap.application.bio_app.Login.AccountActivity;
import com.biomap.application.bio_app.Login.BeginActivity;
import com.biomap.application.bio_app.Login.LoginRegisterActivity;
import com.biomap.application.bio_app.Mapping.MappingActivity;
import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.BottomNavigationViewHelper;
import com.biomap.application.bio_app.Vitals.VitalsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

/**
 * Home page.
 * <p>
 * Created by Hamish Brindle on 2017-06-13.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int ACTIVITY_NUM = 2;

    public static SharedPreferences SHARED_PREFERENCES;

    private FirebaseDatabase database;

    private DatabaseReference myRef;

    private DrawerLayout mDrawer;

    private BluetoothHelper bluetoothHelper;

    private Map<UUID, byte[]> sensorValues;

    private ImageButton mAccountSettings;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the shared preferences for this instance (i.e. if user has logged in, etc.)
        SHARED_PREFERENCES = this.getSharedPreferences(
                "com.biomap.application.bio_app", Context.MODE_PRIVATE
        );

        // Request permission for Bluetooth
        requestPermissions(new String[]{
                        Manifest.permission.BLUETOOTH},
                1);

        mAccountSettings = (ImageButton) findViewById(R.id.toolbar_settings);
        mAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accountIntent = new Intent(getApplicationContext(), AccountActivity.class);
                startActivity(accountIntent);

            }
        });

        // setupFirebase();
        // bluetoothHelper = new BluetoothHelper(this);
        setupDebugButton();
        setupToolbar();
        setupMenuButtons();
        setupBottomNavigationView();


    }

    /**
     * A debug button used for various executions (animation, bluetooth, etc.)
     */
    private void setupDebugButton() {
        // TODO: Temp debug button to test animation activity.
        //Remove before deploying
        ImageView mDebugButton = (ImageView) findViewById(R.id.biomap_logo_imageView);

        mDebugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent debugIntent = new Intent(getBaseContext(), BeginActivity.class);
                startActivity(debugIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();


                /*
                Map<UUID, byte[]> reversedMap = new TreeMap<>(bluetoothHelper.getCharacteristicValues());

                for (Map.Entry entry : reversedMap.entrySet()) {
                    byte[] bytes = ((byte[]) entry.getValue());
                    Log.e(TAG, "Print sensor values of " + entry.getKey() + ": [HEX] " + byteToHex(bytes));
                }
                */
            }
        });
    }

    /**
     * Convert a byte array to a hex string. Used for printing hex values from the micro-controller.
     *
     * @param bytes from characteristic
     * @return String of hex values for the characteristic.
     */
    private String byteToHex(byte[] bytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }

        return sb.toString();
    }


    /**
     * Initialize user authentication objects and listeners for authentication state changes.
     */
    private void setupFirebase() {

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        final Intent register_login_intent = new Intent(this, LoginRegisterActivity.class);

        FirebaseAuth.AuthStateListener mAuthListener = firebaseAuth -> {

            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged.Main:signed_in:" + user.getUid());

            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged.Main:signed_out");
                // Create the logout activity intent.
                register_login_intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(register_login_intent);
                finish();
            }
        };

        // Get the user's authentication credentials and check if signed in or not.
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuthListener.onAuthStateChanged(mAuth);

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

        //finding header of nav bar
        View header = nvDrawer.getHeaderView(0);

        // Setup drawer view
        setupDrawerContent(nvDrawer);

        ImageButton hamburger = (ImageButton) findViewById(R.id.toolbar_hamburger);
        hamburger.setOnClickListener(v -> mDrawer.openDrawer(GravityCompat.START));

        TextView mTimeOfDay = (TextView) header.findViewById(R.id.nav_header_time_of_day);
        final TextView mNameOfUser = (TextView) header.findViewById(R.id.nav_header_user_name);

        Calendar calender = Calendar.getInstance();

        if (6 < calender.get(Calendar.HOUR_OF_DAY) && calender.get(Calendar.HOUR_OF_DAY) < 12) {
            mTimeOfDay.setText(getString(R.string.good_morning_text));
        } else if (12 <= calender.get(Calendar.HOUR_OF_DAY) && calender.get(Calendar.HOUR_OF_DAY) < 17) {
            mTimeOfDay.setText(getString(R.string.good_afternoon_text));
        } else {
            mTimeOfDay.setText(getString(R.string.good_evening_text));
        }

//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String[] fullname = dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Name").getValue().toString().split(" ");
//                mNameOfUser.setText(fullname[0].substring(0, 1).toUpperCase() + fullname[0].substring(1));
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

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
                menuItem -> {
                    selectDrawerItem(menuItem);
                    return true;
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
                setupFirebase();
                intent = new Intent(this, LoginRegisterActivity.class);
                intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                break;
            case R.id.nav_account:
                intent = new Intent(this, AccountActivity.class);
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting-up bottom navigation view.");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);

        // Set color of selected item in the navbar (unique to each activity)
        bottomNavigationViewEx.setIconTintList(ACTIVITY_NUM, getColorStateList(R.color.bottom_nav_main));

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(MainActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem item = menu.getItem(ACTIVITY_NUM);
        item.setChecked(true);
    }

    /**
     * Creates the menu buttons for the home activity.
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
            menuButtons[i].setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), menuActivities[finalI]);
                startActivity(intent);
                // Make switching between activities blend via fade-in / fade-out
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            });
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setupFirebase();
    }
}
