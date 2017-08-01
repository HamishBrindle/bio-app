package com.biomap.application.bio_app.Mapping;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.biomap.application.bio_app.Alerts.AlertsActivity;
import com.biomap.application.bio_app.Connect.ConnectActivity;
import com.biomap.application.bio_app.Login.LoginRegisterActivity;
import com.biomap.application.bio_app.Mapping.Heatmap.MyGLSurfaceView;
import com.biomap.application.bio_app.Mapping.Heatmap.PressureNode;
import com.biomap.application.bio_app.OpenGL.GLHeatmap;
import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.BottomNavigationViewHelper;
import com.biomap.application.bio_app.Utility.CustomFontsLoader;
import com.biomap.application.bio_app.Vitals.VitalsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanSettings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ca.hss.heatmaplib.HeatMap;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

/**
 * Draws the pressure map on the Mapping Activity.
 * <p>
 * Created by hamis on 2017-06-13.
 */
public class MappingActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "MappingActivity";
    private static final int ACTIVITY_NUM = 0;
    private static final int RECORD_REQUEST_CODE = 1;
    private static final long DRAW_DELAY = 30;
    private static final double INTENSITY_MULTIPLIER = 1.5;
    private static final int MAPPING_RADIUS = 1200;


    // Bluetooth
    private static RxBleClient rxBleClient;
    private final Map<UUID, byte[]> characteristicValues = new HashMap<>();
    private Subscription scanSubscription;
    private Subscription connectionSubscription;
    private RxBleDevice device;
    private boolean deviceConnected;
    private boolean deviceFound;
    private PressureNode[][] sensorValueMatrix;
    private static final UUID SERVICE_UUID = UUID.fromString("74F6F000-EA13-4881-9E52-36F754875BF5");
    private static final UUID[] CHAR_UUID = {
            UUID.fromString("74F6F001-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F002-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F003-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F004-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F005-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F006-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F007-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F008-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F009-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F00A-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F00B-EA13-4881-9E52-36F754875BF5"),
//            UUID.fromString("74F6F00C-EA13-4881-9E52-36F754875BF5"),
//            UUID.fromString("74F6F00D-EA13-4881-9E52-36F754875BF5"),
//            UUID.fromString("74F6F00E-EA13-4881-9E52-36F754875BF5"),
//            UUID.fromString("74F6F00F-EA13-4881-9E52-36F754875BF5"),
    };

    // UI
    private DrawerLayout mDrawer;
    private boolean calibrated;
    private MyGLSurfaceView mGLView;
    private HeatMap heatmap;
    private boolean render;

    Random rand = new Random();

    /**
     * Constructor.
     */
    public MappingActivity() {
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        // Dashed warning line doesn't appear 'dashed' unless the following:
        ImageView mDashedLine = (ImageView) findViewById(R.id.dashed_line);
        mDashedLine.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // setupFirebase();
        setupDateBanner();
        setupFonts();
        setupToolbar();
        setupHeatMap();
        setupBottomNavigationView();

    }

    /**
     * Initialize Bluetooth setup.
     */
    private void setupBluetooth() {

        rxBleClient = RxBleClient.create(this);
        deviceConnected = false;
        deviceFound = false;
        getLocationPermission();
        scanBluetoothDevices();
    }

    /**
     * Initialize bluetooth device scanning and reading of characteristics within specified service
     * UUID.
     */
    private void readCharacteristics() {

        final Observable<RxBleConnection> connectionObservable = prepareConnectionObservable(); // your connectionObservable

        Log.e(TAG, "readCharacteristics: Trying to connect to Bluetooth device.");

        connectionObservable
                .flatMap( // get the characteristics from the service you're interested in
                        connection -> connection
                                .discoverServices()
                                .flatMap(services -> services
                                        .getService(SERVICE_UUID)
                                        .delay(5000, TimeUnit.MILLISECONDS)
                                        .map(BluetoothGattService::getCharacteristics)
                                ),
                        Pair::new
                )
                .flatMap(connectionAndCharacteristics -> {
                    final RxBleConnection connection = connectionAndCharacteristics.first;
                    final List<BluetoothGattCharacteristic> characteristics = connectionAndCharacteristics.second;
                    return readInitialValues(connection, characteristics)
                            .concatWith(setupNotifications(connection, characteristics));
                })
                .subscribe(
                        pair -> {
                            characteristicValues.put(pair.first.getUuid(), pair.second);
                            checkValues();
                        },
                        this::onReadFailure
                );


    }

    private void calibrateSensors() {

        int mapSize = characteristicValues.size();
        sensorValueMatrix = new PressureNode[15][11];

        for (int i = 0; i < mapSize; i++) {
            byte[] bytes = ((byte[]) characteristicValues.get(CHAR_UUID[i]));
            for (int j = 0; j < bytes.length; j++) {
                sensorValueMatrix[j][i] = new PressureNode(bytes[j], j, i);
            }
        }

        // Log.e(TAG, "" + entry.getKey() + ": [HEX] " + byteToHex(bytes));
        // mHeatMap.plotHeatMap();

        calibrated = true;

    }

    private void checkValues() {
        int mapSize = characteristicValues.size();
        int numVal = 0;

        if (mapSize > 10) {

            if (!calibrated)
                calibrateSensors();

            for (int i = 0; i < mapSize; i++) {
                byte[] bytes = ((byte[]) characteristicValues.get(CHAR_UUID[i]));
                for (int j = 0; j < bytes.length; j++) {
                    sensorValueMatrix[j][i].setPressure(bytes[j] & 0xFF);
                }
            }

            plotHeatMap(sensorValueMatrix);
            // randomHeatmap();

            // Log.e(TAG, "" + entry.getKey() + ": [HEX] " + byteToHex(bytes));
            characteristicValues.clear();
            // mHeatMap.plotHeatMap();
        }
    }

    public void plotHeatMap(PressureNode[][] pressure) {

        double intensity;
        float radius = 300;

        int numOfRow = 11;
        int numOfCol = pressure[0].length;

        float rowSize = 1.0f / numOfRow;
        float colSize = 1.0f / numOfCol;

        float rowOffset = rowSize / 2.0f;
        float colOffset = colSize / 2.0f;

        for (int yPos = 1; yPos < numOfRow; yPos++) {
            for (int xPos = numOfCol; xPos > 0; xPos--) {

                int curPressure = pressure[xPos - 1][yPos - 1].getPressure();

                if (curPressure < 25)
                    intensity = 0;
                else if (curPressure < 30)
                    intensity = 10;
                else if (curPressure < 35)
                    intensity = 20;
                else if (curPressure < 40)
                    intensity = 30;
                else if (curPressure < 45)
                    intensity = 40;
                else if (curPressure < 50)
                    intensity = 50;
                else if (curPressure < 55)
                    intensity = 60;
                else if (curPressure < 60)
                    intensity = 70;
                else
                    intensity = 80;

                try {
                    Thread.sleep(DRAW_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                HeatMap.DataPoint point = new HeatMap.DataPoint(((yPos * rowSize) - rowOffset), ((xPos * colSize) - colOffset), intensity * INTENSITY_MULTIPLIER);
                heatmap.addData(point);


            }
        }

        renderReady();

    }

    /**
     * Read the values from the characteristics.
     */
    private Observable<Pair<BluetoothGattCharacteristic, byte[]>> readInitialValues(RxBleConnection connection,
                                                                                    List<BluetoothGattCharacteristic> characteristics) {
        Log.e(TAG, "readInitialValues: Reading characteristics (Size = " + characteristics.size() + ").");
        return Observable.from(characteristics) // deal with every characteristic separately
                .filter(characteristic -> (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0) // filter characteristics that have read property
                .flatMap(connection::readCharacteristic, // read characteristic
                        Pair::new); // merge characteristic with byte[] to keep track from which characteristic the bytes came
    }

    /**
     * Setup the notifications for the characteristic connections.
     */
    private Observable<Pair<BluetoothGattCharacteristic, byte[]>> setupNotifications(RxBleConnection connection,
                                                                                     List<BluetoothGattCharacteristic> characteristics) {
        Log.e(TAG, "readInitialValues: Setting-up notifications");
        return Observable.from(characteristics) // deal with every characteristic separately
                .filter(characteristic -> (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) // filter characteristics that have notify property
                .flatMap(characteristic -> connection
                                .setupNotification(characteristic) // setup notification for each
                                .flatMap(observable -> observable), // to get the raw bytes from notification
                        Pair::new); // merge characteristic with byte[] to keep track from which characteristic the bytes came
    }

    /**
     * Convert a byte array to a hex string. Used for printing hex values from the micro-controller.
     *
     * @param bytes
     * @return
     */
    private String byteToHex(byte[] bytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }

        return sb.toString();
    }

    /**
     * Get a connection from the Bluetooth device.
     */
    private Observable<RxBleConnection> prepareConnectionObservable() {
        return device.establishConnection(true);
    }

    private void scanBluetoothDevices() {

        scanSubscription = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder()
                        // add custom filters if needed
                        .setDeviceName("BioMap")
                        .build()
        )
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(this::clearSubscription)
                .subscribe(
                        scanResult -> {
                            device = scanResult.getBleDevice();
                            readCharacteristics();
                            Log.e(TAG, "scanBluetoothDevices: BioMap found by name: " + device.getMacAddress());
                            scanSubscription.unsubscribe();
                        },
                        this::onScanFailure
                );
    }

    private void onScanFailure(Throwable throwable) {

        if (throwable instanceof BleScanException) {
            handleBleScanException((BleScanException) throwable);
        }
    }

    private void onReadFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Log.e(TAG, "onReadFailure: Read failure - ", throwable);
    }

    private void clearSubscription() {
        scanSubscription = null;
    }

    private void getLocationPermission() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permissions denied.");
            makeRequest();
        }
    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                RECORD_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RECORD_REQUEST_CODE: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user");
                } else {
                    Log.i(TAG, "Permission has been granted by user");
                }
                return;
            }
        }
    }

    private boolean isConnected() {

        return device.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;

    }

    private void handleBleScanException(BleScanException bleScanException) {

        switch (bleScanException.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                Toast.makeText(MappingActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.BLUETOOTH_DISABLED:
                Toast.makeText(MappingActivity.this, "Enable bluetooth and try again", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                Toast.makeText(MappingActivity.this,
                        "On Android 6.0 location permission is required. Implement Runtime Permissions", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                Toast.makeText(MappingActivity.this, "Location services needs to be enabled on Android 6.0", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_ALREADY_STARTED:
                Toast.makeText(MappingActivity.this, "Scan with the same filters is already started", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                Toast.makeText(MappingActivity.this, "Failed to register application for bluetooth scan", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_FEATURE_UNSUPPORTED:
                Toast.makeText(MappingActivity.this, "Scan with specified parameters is not supported", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_INTERNAL_ERROR:
                Toast.makeText(MappingActivity.this, "Scan failed due to internal error", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES:
                Toast.makeText(MappingActivity.this, "Scan cannot start due to limited hardware resources", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.UNKNOWN_ERROR_CODE:
            case BleScanException.BLUETOOTH_CANNOT_START:
            default:
                Toast.makeText(MappingActivity.this, "Unable to start scanning", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setupDateBanner() {
        //Setting the date banner
        TextView mDayofWeek = (TextView) findViewById(R.id.date_weekday);
        TextView mfullDate = (TextView) findViewById(R.id.date_month_day);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        Date date = new Date();
        Calendar cal = Calendar.getInstance();

        mfullDate.setText(simpleDateFormat.format(date));
        mDayofWeek.setText(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()));
    }

    private void setupFonts() {
        // Change the fonts in the activity by going through all the children of the parent layout.
        TextView mPageTitle = (TextView) findViewById(R.id.mapping_page_title);
        LinearLayout mBannerText = (LinearLayout) findViewById(R.id.banner_text);
        LinearLayout mMappingView = (LinearLayout) findViewById(R.id.mapping_viewGroup);
        LinearLayout mLeftRight = (LinearLayout) findViewById(R.id.weight_charts);
        TextView mWeightHeader = (TextView) findViewById(R.id.weight_header);
        TextView mLeftPercentage = (TextView) findViewById(R.id.left_percentage);
        TextView mRightPercentage = (TextView) findViewById(R.id.right_percentage);

        mPageTitle.setTypeface(CustomFontsLoader.getTypeface(this, CustomFontsLoader.GOTHAM_BOLD));
        mWeightHeader.setTypeface(CustomFontsLoader.getTypeface(this, CustomFontsLoader.GOTHAM_BOLD));
        mLeftPercentage.setTypeface(CustomFontsLoader.getTypeface(this, CustomFontsLoader.GOTHAM_BOLD));
        mRightPercentage.setTypeface(CustomFontsLoader.getTypeface(this, CustomFontsLoader.GOTHAM_BOLD));

        CustomFontsLoader.overrideFonts(this, mBannerText, CustomFontsLoader.GOTHAM_BOOK);
        CustomFontsLoader.overrideFonts(this, mMappingView, CustomFontsLoader.GOTHAM_MEDIUM);
        CustomFontsLoader.overrideFonts(this, mLeftRight, CustomFontsLoader.GOTHAM_BOOK);
    }

    private void setupFirebase() {
        final Intent register_login_intent = new Intent(this, LoginRegisterActivity.class);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupHeatMap() {
        heatmap = (HeatMap) findViewById(R.id.heatmap);
        heatmap.setMinimum(0.0);
        heatmap.setMaximum(100.0);
        heatmap.setRadius(MAPPING_RADIUS);

        render = false;

        heatmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heatmap.forceRefresh();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (heatmap != null) {
                        setupBluetooth();
                        break;
                    }
                }
            }
        }).start();
    }

    private void renderReady() {
        Log.e(TAG, "renderReady: Render is ready. Refresh the view...");
    }

    /**
     * Convert a 1D matrix into a
     *
     * @param input Array to be converted.
     * @return Converted array.
     */
    int[][] convert2DArray(int[] input) {
        int resolution = (int) Math.sqrt(input.length);
        int[][] output = new int[resolution][resolution];
        int count = 0;

        for (int yPos = 0; yPos < resolution; yPos++) {
            for (int xPos = 0; xPos < resolution; xPos++) {
                output[xPos][yPos] = input[count++];
            }
        }
        return output;
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
//                setupFirebase();
                Intent logoutintent = new Intent(getApplicationContext(), LoginRegisterActivity.class);
                ComponentName cn = logoutintent.getComponent();
                intent = IntentCompat.makeRestartActivityTask(cn);
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
        BottomNavigationViewHelper.enableNavigation(MappingActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem item = menu.getItem(ACTIVITY_NUM);
        item.setChecked(true);
    }

    /**
     * Gets a pressure-reading array of values for each node.
     *
     * @return The pressure map array of values.
     */
    private int[] getPressure() {

        // TODO: This will eventually get information from the nodes and create an array.

        return new int[]{
                0, 0, 0, 20, 30, 20, 0, 0,
                0, 20, 70, 88, 90, 75, 20, 0,
                15, 65, 70, 61, 64, 65, 50, 0,
                0, 75, 60, 40, 38, 60, 50, 0,
                0, 50, 50, 20, 0, 55, 55, 0,
                0, 60, 45, 0, 0, 48, 50, 0,
                0, 65, 40, 0, 0, 45, 55, 0,
                0, 40, 30, 0, 0, 55, 47, 0
        };
    }


}
