package com.biomap.application.bio_app.Home;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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
import android.widget.TextView;

import com.biomap.application.bio_app.Alerts.AlertsActivity;
import com.biomap.application.bio_app.Connect.ConnectActivity;
import com.biomap.application.bio_app.Login.BeginActivity;
import com.biomap.application.bio_app.Login.LoginRegisterActivity;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

/**
 * Home page.
 * <p>
 * Created by Hamish Brindle on 2017-06-13.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ACTIVITY_NUM = 2;
    /**
     * Stores information about user's session.
     */
    public static SharedPreferences SHARED_PREFERENCES;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private DrawerLayout mDrawer;

    // Bluetooth Fields
    private static final int REQUEST_ENABLE_BT = 1;

    private static final UUID MY_UUID = UUID.fromString("74F6F000-EA13-4881-9E52-36F754875BF5");

    private static final UUID[] SENSOR_GROUPS = {
            UUID.fromString("74F6F001-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F002-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F003-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F004-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F005-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F006-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F007-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F008-EA13-4881-9E52-36F754875BF5")
    };

    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private String MACAddress;
    private BluetoothDevice mDevice;
    private Handler mHandler;
    private BluetoothLeScannerCompat scanner;
    private BluetoothGatt mGatt;
    private List<BluetoothGattCharacteristic> bluetoothGattCharacteristics;
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            gatt.discoverServices();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            Log.e("BluetoothLeService", "onServicesDiscovered()");

            if (status == BluetoothGatt.GATT_SUCCESS) {

                List<BluetoothGattService> gattServices = mGatt.getServices();

                Log.e("onServicesDiscovered", "Services count: " + gattServices.size());

                for (BluetoothGattService gattService : gattServices) {
                    String serviceUUID = gattService.getUuid().toString();
                    if (serviceUUID.compareTo(MY_UUID.toString()) == 0) {
                        Log.e("onServicesDiscovered", "Service uuid: " + serviceUUID);
                        bluetoothGattCharacteristics = gattService.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic :
                                bluetoothGattCharacteristics) {
                            mGatt.readCharacteristic(characteristic);
                        }
                    }
                }
            } else {

                Log.w(TAG, "onServicesDiscovered received: " + status);

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            Log.d(TAG, "onCharacteristicRead: Reading characteristic: " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
    };

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            if (!results.isEmpty()) {
                ScanResult result = results.get(0);
                BluetoothDevice device = result.getDevice();

                if (device.getName().compareTo("BioMap") == 0) {
                    mDevice = device;
                    Log.d(TAG, "onBatchScanResults: GOT BIOMAP BITCH");
                }

                String deviceAddress = device.getAddress();

                Log.e(TAG, "onBatchScanResults: Device Address: " + deviceAddress);

                // Device detected, we can automatically connect to it and stop the scan
                mGatt = mDevice.connectGatt(getBaseContext(), true, mGattCallback);

                scanner.stopScan(scanCallback);

            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the shared preferences for this instance (i.e. if user has logged in, etc.)
        SHARED_PREFERENCES = this.getSharedPreferences(
                "com.biomap.application.bio_app", Context.MODE_PRIVATE
        );

        /* Initiate Bluetooth
        requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION},
                1);
        initBluetooth();
        */

        // Initialize page elements.
        setupFirebase();
        setupDebugButton();
        setupToolbar();
        setupMenuButtons();
        setupBottomNavigationView();

        ConstraintLayout mMenuButtons = (ConstraintLayout) findViewById(R.id.constraintLayout);
        CustomFontsLoader.overrideFonts(this, mMenuButtons, CustomFontsLoader.GOTHAM_BOLD);

    }

    private void setupDebugButton() {
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initBluetooth() {

        scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setReportDelay(1000)
                .setUseHardwareBatchingIfSupported(false).build();
        List<ScanFilter> filters = new ArrayList<>();

        Log.e(TAG, "initBluetooth: Size of filters: " + filters.size());

        for (ScanFilter scanFilter :
                filters) {
            Log.d(TAG, "initBluetooth: Device = " + scanFilter);
        }

        filters.add(new ScanFilter.Builder().setDeviceAddress("FC:08:04:93:81:D4").build());

        scanner.startScan(filters, settings, scanCallback);
    }

    /**
     * Initialize user authentication objects and listeners for authentication state changes.
     */
    private void setupFirebase() {

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
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
                    register_login_intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
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

        //finding header of nav bar
        View header = nvDrawer.getHeaderView(0);

        // Setup drawer view
        setupDrawerContent(nvDrawer);

        ImageButton hamburger = (ImageButton) findViewById(R.id.toolbar_hamburger);
        hamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(GravityCompat.START);
            }
        });

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

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] fullname = dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Name").getValue().toString().split(" ");
                mNameOfUser.setText(fullname[0].substring(0, 1).toUpperCase() + fullname[0].substring(1));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
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
                setupFirebase();
                intent = new Intent(this, LoginRegisterActivity.class);
                intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
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
}
