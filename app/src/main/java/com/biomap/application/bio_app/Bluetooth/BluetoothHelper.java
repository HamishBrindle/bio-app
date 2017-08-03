package com.biomap.application.bio_app.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.biomap.application.bio_app.Mapping.MappingActivity;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanSettings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class BluetoothHelper {

    private static final String TAG = "BluetoothHelper";
    private static final int RECORD_REQUEST_CODE = 1;

    // Bluetooth
    private static RxBleClient rxBleClient;
    private final Map<UUID, byte[]> characteristicValues = new HashMap<>();
    private Subscription scanSubscription;
    private Subscription connectionSubscription;
    private RxBleDevice device;
    private boolean deviceConnected;
    private boolean deviceFound;
    private int[][] sensorValueMatrix = new int[15][15];
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
            UUID.fromString("74F6F00C-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F00D-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F00E-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F00F-EA13-4881-9E52-36F754875BF5"),
    };
    private Context context;

    /**
     * Constructor.
     *
     * @param context
     */
    public BluetoothHelper(Context context) {
        this.context = context;
        setupBluetooth();
    }

    /**
     * Initialize Bluetooth setup.
     */
    public void setupBluetooth() {

        rxBleClient = RxBleClient.create(context);
        deviceConnected = false;
        deviceFound = false;
        scanBluetoothDevices();
    }

    /**
     * Get a connection from the Bluetooth device.
     */
    private Observable<RxBleConnection> prepareConnectionObservable() {
        return device.establishConnection(true);
    }

    /**
     * Scan available Bluetooth devices and filter only our own. Once we have our device,
     * we initiate a scan of it's characteristics.
     */
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

    /**
     * Initialize bluetooth device scanning and reading of characteristics within specified service
     * UUID.
     */
    private void readCharacteristics() {

        final Observable<RxBleConnection> connectionObservable = prepareConnectionObservable(); // your connectionObservable

        Log.e(TAG, "readCharacteristics: Trying to connect to Bluetooth device.");

        new Thread(new Runnable() {
            @Override
            public void run() {
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
                                throwable -> {
                                    onReadFailure(throwable);
                                }
                        );
            }
        }).start();

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
     * Determine if the defined amount of characteristics have had value stored,
     * then print those values.
     */
    private void checkValues() {

        if (characteristicValues.size() > 10) {
            for (Map.Entry entry : characteristicValues.entrySet()) {
                byte[] bytes = ((byte[]) entry.getValue());
                Log.e(TAG, "" + entry.getKey() + ": [HEX] " + byteToHex(bytes));
            }
            characteristicValues.clear();
            // mHeatMap.plotHeatMap();
        }
    }

    /**
     * When scan fails...
     *
     * @param throwable
     */
    private void onScanFailure(Throwable throwable) {

        if (throwable instanceof BleScanException) {
            handleBleScanException((BleScanException) throwable);
        }
    }

    /**
     * When read fails...
     *
     * @param throwable
     */
    private void onReadFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Log.e(TAG, "onReadFailure: Read failure - ", throwable);
    }

    /**
     * Nullify scan subscription.
     */
    private void clearSubscription() {
        scanSubscription = null;
    }

    /**
     * Is device connected.
     *
     * @return
     */
    private boolean isConnected() {

        return device.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;

    }

    /**
     * Handle any errors in the scan proccess.
     *
     * @param bleScanException
     */
    private void handleBleScanException(BleScanException bleScanException) {

        switch (bleScanException.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                Toast.makeText(context, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.BLUETOOTH_DISABLED:
                Toast.makeText(context, "Enable bluetooth and try again", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                Toast.makeText(context,
                        "On Android 6.0 location permission is required. Implement Runtime Permissions", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                Toast.makeText(context, "Location services needs to be enabled on Android 6.0", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_ALREADY_STARTED:
                Toast.makeText(context, "Scan with the same filters is already started", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                Toast.makeText(context, "Failed to register application for bluetooth scan", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_FEATURE_UNSUPPORTED:
                Toast.makeText(context, "Scan with specified parameters is not supported", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_INTERNAL_ERROR:
                Toast.makeText(context, "Scan failed due to internal error", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES:
                Toast.makeText(context, "Scan cannot start due to limited hardware resources", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.UNKNOWN_ERROR_CODE:
            case BleScanException.BLUETOOTH_CANNOT_START:
            default:
                Toast.makeText(context, "Unable to start scanning", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Getter for characteristic values Map.
     *
     * @return map.
     */
    public Map<UUID, byte[]> getCharacteristicValues() {
        return characteristicValues;
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
     * Convert a byte array to a hex string. Used for printing hex values from the micro-controller.
     *
     * @param bytes
     * @return
     */
    private int[] byteToInt(byte[] bytes) {

        int[] row = new int[8];
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
            row[i] = buffer.getShort();
        }

        return row;
    }
}