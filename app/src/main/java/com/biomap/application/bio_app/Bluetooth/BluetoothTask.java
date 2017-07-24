package com.biomap.application.bio_app.Bluetooth;

import android.util.Log;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.Arrays;
import java.util.UUID;

import rx.Subscription;

/**
 * Creates task to collect data from characteristics using their UUID's.
 * <p>
 * Created by Hamish Brindle on 2017-07-20.
 */

class BluetoothTask extends Thread {

    private static final String TAG = "BluetoothTask";
    private UUID uuid;
    private String nodeValue;
    private Subscription sub;
    private RxBleClient rxBleClient;
    private RxBleDevice device;

    BluetoothTask(RxBleDevice device, RxBleClient rxBleClient, UUID uuid) {

        this.device = device;
        this.rxBleClient = rxBleClient;
        this.uuid = uuid;

        start();

    }

    @Override
    public void run() {

        device.establishConnection(false)
                .flatMap(rxBleConnection -> rxBleConnection.readCharacteristic(uuid))
                .subscribe(
                        characteristicValue -> {
                            // Read characteristic value.
                            nodeValue = Arrays.toString(characteristicValue);
                            Log.d(TAG, "getNodeValue: Nodevalue: " + nodeValue);
                        },
                        throwable -> {
                            // Handle an error here.
                        }
                );
    }

    public String getNodeValue() {
        return nodeValue;
    }

}
