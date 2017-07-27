package com.biomap.application.bio_app.Bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import rx.Observable;

public class BluetoothHelper {

    private static final String TAG = "BluetoothHelper";

    private static final String MAC_ADDRESS = "FC:08:04:93:81:D4";

    private static final UUID[] SERVICE_UUID = {
            UUID.fromString("74F6F000-EA13-4881-9E52-36F754875BF5")
    };

    private static final UUID[] CHARS_UUID = {
            UUID.fromString("74F6F001-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F002-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F003-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F004-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F005-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F006-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F007-EA13-4881-9E52-36F754875BF5"),
            UUID.fromString("74F6F008-EA13-4881-9E52-36F754875BF5")
    };

    private RxBleClient bleClient;

    private RxBleDevice bleDevice;

    private Context context;

    private final Map<UUID, byte[]> characteristicValues = new HashMap<>();
    private boolean connected;

    public BluetoothHelper(Context context) {

        this.context = context;
        this.bleClient = RxBleClient.create(context);
        this.bleDevice = bleClient.getBleDevice(MAC_ADDRESS);

        initBle();

    }

    /**
     * Initialize bluetooth device scanning and reading of characteristics within specified service
     * UUID.
     */
    private void initBle() {

        final UUID serviceUuid = SERVICE_UUID[0]; // your service UUID

        final Observable<RxBleConnection> connectionObservable = prepareConnectionObservable(); // your connectionObservable

        new Thread(new Runnable() {

            @Override
            public void run() {

                Log.e(TAG, "initBle: Trying to connect to Bluetooth device.");

                connectionObservable
                        .flatMap( // get the characteristics from the service you're interested in
                                connection -> connection
                                        .discoverServices()
                                        .flatMap(services -> services
                                                .getService(serviceUuid)
                                                .map(BluetoothGattService::getCharacteristics)
                                        ),
                                Pair::new
                        )
                        .flatMap(connectionAndCharacteristics -> {
                            final RxBleConnection connection = connectionAndCharacteristics.first;
                            final List<BluetoothGattCharacteristic> characteristics = connectionAndCharacteristics.second;
                            return readInitialValues(connection, characteristics);
                            //.concatWith(setupNotifications(connection, characteristics));
                        })
                        .subscribe(
                                pair -> {
                                    characteristicValues.put(pair.first.getUuid(), pair.second);
                                    connected = true;
                                },
                                throwable -> {
                                    Log.e(TAG, "initBle: Unable to subscribe Bluetooth device.");
                                    connected = false;
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
        Log.e(TAG, "readInitialValues: Reading characteristics");
        return Observable.from(characteristics) // deal with every characteristic separately
                .filter(characteristic -> (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0) // filter characteristics that have read property
                .flatMap(connection::readCharacteristic, // read characteristic
                        Pair::new); // merge characteristic with byte[] to keep track from which characteristic the bytes came
    }

    /**
     * Setup the notifications for the characteristic connections. I'm not sure we need this just yet.
     */
    private Observable<Pair<BluetoothGattCharacteristic, byte[]>> setupNotifications(RxBleConnection connection,
                                                                                     List<BluetoothGattCharacteristic> characteristics) {
        Log.e(TAG, "readInitialValues: Setting-up notifications.]");
        return Observable.from(characteristics) // deal with every characteristic separately
                .filter(characteristic -> (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) // filter characteristics that have notify property
                .flatMap(characteristic -> connection
                                .setupNotification(characteristic) // setup notification for each
                                .flatMap(observable -> observable), // to get the raw bytes from notification
                        Pair::new); // merge characteristic with byte[] to keep track from which characteristic the bytes came
    }

    /**
     * Get a connection from the Bluetooth device.
     */
    private Observable<RxBleConnection> prepareConnectionObservable() {
        return bleDevice.establishConnection(true);
    }

    /**
     * Getter for characteristic values Map.
     *
     * @return map.
     */
    public Map<UUID, byte[]> getCharacteristicValues() {
        return characteristicValues;
    }
}