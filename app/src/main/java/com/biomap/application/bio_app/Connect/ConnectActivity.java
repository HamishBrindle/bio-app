package com.biomap.application.bio_app.Connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.biomap.application.bio_app.Alerts.AlertsActivity;
import com.biomap.application.bio_app.Analytics.AnalyticsActivity;
import com.biomap.application.bio_app.Mapping.MappingActivity;
import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Bluetooth.BluetoothHelper;
import com.biomap.application.bio_app.Utility.BottomNavigationViewHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

/**
 * Connect.
 * <p>
 * Created by Hamish Brindle on 2017-06-13.
 */

public class ConnectActivity extends AppCompatActivity implements BluetoothProfile.ServiceListener {

    private static final String TAG = "ConnectActivity";
    private static final int ACTIVITY_NUM = 4;
    private DrawerLayout mDrawer;
    private TextView mConnectionMsg;
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * Broadcast Receiver that detects bond state changes (Bluetooth enabling/disabling)
     */
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        mConnectionMsg.setText("Bluetooth off");
                        Log.d(TAG, "onReceive: Bluetooth off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        mConnectionMsg.setText("Turning Bluetooth off...");
                        Log.d(TAG, "onReceive: Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        mConnectionMsg.setText("Bluetooth on");
                        Log.d(TAG, "onReceive: Bluetooth on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        mConnectionMsg.setText("Turning Bluetooth on...");
                        Log.d(TAG, "onReceive: Turning Bluetooth on...");
                        break;
                }
            }
        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        Log.d(TAG, "onCreate: starting.");

        setupToolbar();
        setupBottomNavigationView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
    }

    private void setupBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mConnectionMsg = (TextView) findViewById(R.id.connected);
        Button mOnOff = (Button) findViewById(R.id.bluetooth_btn);

        // Register for broadcasts on BluetoothAdapter state change.
        IntentFilter stateChangeFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, stateChangeFilter);

        // Register for broadcasts of Bluetooth pairing.
        IntentFilter pairingFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver2, pairingFilter);

        mOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter == null) {
                    Log.d(TAG, "onClick: Bluetooth capabilities unavailable.");
                    return;
                }

                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBTIntent);

                    IntentFilter BTIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(mBroadcastReceiver1, BTIntentFilter);
                }
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();

                    IntentFilter BTIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(mBroadcastReceiver1, BTIntentFilter);

                }
            }
        });

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
    public void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting-up bottom navigation view.");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(ConnectActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem item = menu.getItem(ACTIVITY_NUM);
        item.setChecked(true);
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        mConnectionMsg.setText("I am connected.");
    }

    @Override
    public void onServiceDisconnected(int profile) {
        mConnectionMsg.setText("I am NOT connected.");
    }
}
