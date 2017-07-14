package com.biomap.application.bio_app.Home;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.biomap.application.bio_app.Alerts.AlertsActivity;
import com.biomap.application.bio_app.Analytics.AnalyticsActivity;
import com.biomap.application.bio_app.Connect.ConnectActivity;
import com.biomap.application.bio_app.Login.BeginActivity;
import com.biomap.application.bio_app.Login.LoginActivity;
import com.biomap.application.bio_app.Login.LoginRegisterActivity;
import com.biomap.application.bio_app.Mapping.MappingActivity;
import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Bluetooth.BluetoothHelper;
import com.biomap.application.bio_app.Utility.BottomNavigationViewHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;

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

    // Bluetooth Fields
    private static final int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("74F6F000-EA13-4881-9E52-36F754875BF5");
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private String MACAddress;
    private BluetoothDevice mDevice;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the shared preferences for this instance (i.e. if user has logged in, etc.)
        SHARED_PREFERENCES = this.getSharedPreferences(
                "com.biomap.application.bio_app", Context.MODE_PRIVATE
        );

        setupBluetooth();
        setupToolbar();
        setupDateBanner();
        setupMenuButtons();
        setupBottomNavigationView();
        // setupFirebase();

        // TODO: Temp debug button to test animation activity.
        Button mDebugButton = (Button) findViewById(R.id.debug_button);
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
     * Establish Bluetooth connection to device.
     */
    private void setupBluetooth() {
        requestBluetoothEnable();
        queryBluetoothDevices();
        ConnectThread mConnection = new ConnectThread(mDevice);
        mConnection.run();
    }

    private void queryBluetoothDevices() {

        pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {

                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                if (deviceName.compareTo("BioMap") == 0) {
                    mDevice = device;
                    MACAddress = deviceHardwareAddress;
                    Log.d(TAG, "queryBluetoothDevices: Device: " + deviceName + "| Address: " + MACAddress);
                } else {
                    Log.e(TAG, "queryBluetoothDevices: Couldn't find the specified BioMap device.");
                }
            }
        }
    }

    /**
     * Requests user enable Bluetooth on their device.
     */
    private void requestBluetoothEnable() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            // TODO: Handle non-Bluetooth devices.
            Log.d(TAG, "setupBluetooth: Device doesn't support bluetooth");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    /**
     * Once connection is made, establish I/O stream.
     *
     * @param mmSocket socket made between client and server.
     */
    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
        Log.d(TAG, "manageMyConnectedSocket: CONNECTION SUCCESSFUL!");

        //BluetoothHelper bluetoothHelper = new BluetoothHelper(mmSocket);
    }

    /**
     * Handles making connection to device.
     */
    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            Log.d(TAG, "ConnectThread: run: Trying to connect.");
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
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
                AnalyticsActivity.class,
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

        // TODO: Finish dis
        Date date = new Date();

    }

}
