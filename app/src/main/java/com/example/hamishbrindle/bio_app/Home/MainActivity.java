package com.example.hamishbrindle.bio_app.Home;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int ACTIVITY_NUM = 2;

    public static SharedPreferences SHARED_PREFERENCES;

    public static Boolean MOBILE_REGISTER_FLAG;

    public static BluetoothAdapter mBluetoothAdapter;

    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] writeBuf = (byte[]) msg.obj;
            int begin = msg.arg1;
            int end = msg.arg2;
            switch (msg.what) {
                case 1:
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.substring(begin, end);
                    break;
            }
        }
    };

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

        // Bluetooth - check if available for device
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth}
            Log.d(TAG, "BluetoothAdapter: Bluetooth not available on this device.");
        }

        // Check if Bluetooth is disabled - if so, enable it.
        assert mBluetoothAdapter != null;
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        // Retrieve Bluetooth device that application will communicate with.
        BluetoothDevice mDevice = null; //
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mDevice = device;
            }
        }

        // Confirm mDevice isn't null then initialize our connection thread to the Bluetooth.
        if (mDevice != null) {
            ConnectThread mConnectThread = new ConnectThread(mDevice);
            mConnectThread.start();
        } else {
            Log.d(TAG, "BluetoothDevice: Cannot find Bluetooth Device.");
        }


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

    /**
     * Connects Bluetooth Socket
     */
    private static class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;

        private final BluetoothDevice mmDevice;

        private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                // TODO: Catch exception
            }
            mmSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    // TODO: Catch exception
                }
            }

            ConnectedThread mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                // TODO: Catch exception
            }
        }
    }

    /**
     * Once Bluetooth is connected, facilitates passing data to UI.
     */
    private static class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            while (true) {
                try {
                    bytes += mmInStream.read(buffer, bytes, buffer.length - bytes);
                    for (int i = begin; i < bytes; i++) {
                        if (buffer[i] == "#".getBytes()[0]) {
                            mHandler.obtainMessage(1, begin, i, buffer).sendToTarget();
                            begin = i + 1;
                            if (i == bytes - 1) {
                                bytes = 0;
                                begin = 0;
                            }
                        }
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException ignored) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) {
            }
        }
    }


}
