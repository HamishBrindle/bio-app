package com.biomap.application.bio_app.Utility;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.biomap.application.bio_app.Login.AccountActivity;
import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.SerialBuffer;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * USB Serial helper.
 * <p>
 * Created by Vafa on 2017-07-28.
 */

public class SerialHelper {

    private static final String TAG = "SerialHelper";
    private static final String ACTION_USB_PERMISSION = "com.biomap.application.bio_app.USB_PERMISSION";
    private static final String ACTION_USB_READY = "com.biomap.application.bio_app.USB_READY";
    private static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private static final String ACTION_USB_NOT_SUPPORTED = "com.biomap.application.bio_app.USB_NOT_SUPPORTED";
    private static final String ACTION_NO_USB = "com.biomap.application.bio_app.NO_USB";
    private static final String ACTION_USB_PERMISSION_GRANTED = "com.biomap.application.bio_app.USB_PERMISSION_GRANTED";
    private static final String ACTION_USB_PERMISSION_NOT_GRANTED = "com.biomap.application.bio_app.USB_PERMISSION_NOT_GRANTED";
    private static final String ACTION_USB_DISCONNECTED = "com.biomap.application.bio_app.USB_DISCONNECTED";
    private static final String ACTION_CDC_DRIVER_NOT_WORKING = "com.biomap.application.bio_app.ACTION_CDC_DRIVER_NOT_WORKING";
    private static final String ACTION_USB_DEVICE_NOT_WORKING = "com.biomap.application.bio_app.ACTION_USB_DEVICE_NOT_WORKING";
    private static final int BAUD_RATE = 115200;
    public static boolean SERVICE_CONNECTED = false;


    private UsbDevice device;
    private UsbManager manager;
    private UsbSerialDevice serialPort;
    private String dataSet;
    private UsbDeviceConnection connection;
    private int[] serialInArray = new int[225];
    private int serialCount = 0;
    private SerialBuffer serialBuffer;
    private boolean serialPortConnected;
    private boolean firstContact = false;
    private Context context;
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) // User accepted our USB connection. Try to open the device as a serial port
                {
                    Intent intent = new Intent(ACTION_USB_PERMISSION_GRANTED);
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    arg0.sendBroadcast(intent);
                    connection = manager.openDevice(device);
                    new ConnectionThread().start();
                } else // User not accepted our USB connection. Send an Intent to the Main Activity
                {
                    Intent intent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
                    Toast.makeText(context, "USB Not granted", Toast.LENGTH_SHORT).show();

                    arg0.sendBroadcast(intent);
                }
            } else if (arg1.getAction().equals(ACTION_USB_ATTACHED)) {
                Toast.makeText(context, "USB Attached", Toast.LENGTH_SHORT).show();
                if (!serialPortConnected)
                    Log.d(TAG, "onReceive: A USB device has been attached. Try to open it as a Serial port");
                // A USB device has been attached. Try to open it as a Serial port
                findSerialPortDevice();
            } else if (arg1.getAction().equals(ACTION_USB_DETACHED)) {
                Toast.makeText(context, "USB Detached", Toast.LENGTH_SHORT).show();
                // Usb device was disconnected. send an intent to the Main Activity
                Intent intent = new Intent(ACTION_USB_DISCONNECTED);
                arg0.sendBroadcast(intent);
                if (serialPortConnected) {
                    serialPort.close();
                }
                serialPortConnected = false;
            } else if (arg1.getAction().equals(ACTION_NO_USB)) {
                Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private boolean render;
    private ArrayList<Integer> intArray;
    private ArrayList<byte[]> inputByteArrays;
    private int numSensors = 0;
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                Log.d(TAG, "onReceivedData:  length of arg: " + arg0.length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (!firstContact) {
                Log.d(TAG, "onReceivedData: data = " + data);
                if (data.equals("A")) {
                    Log.d(TAG, "onReceivedData: Received A");
                    serialBuffer.clearReadBuffer();
                    serialPort.write("A".getBytes());
                    firstContact = true;
                }
            } else {
                if (!data.equals("")) {
                    numSensors += arg0.length;
                    Log.d(TAG, "onReceivedData: Arg0 = " + arg0);
                    inputByteArrays.add(arg0);
                    if (numSensors > 224) {
                        Log.d(TAG, "onReceivedData: serial count is over 224");
                        serialPort.write("A".getBytes());
                        numSensors = 0;

                        setIntArray();

                        inputByteArrays.clear();
                        render = true;

                    }
                }
            }
        }
    };

    public SerialHelper(Context context) {

        this.context = context;

        intArray = new ArrayList<>();
        inputByteArrays = new ArrayList<>();

        //USB STUFF
        serialBuffer = new SerialBuffer(true);
        serialPortConnected = false;
        AccountActivity.SERVICE_CONNECTED = true;
        setFilter();
        manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        findSerialPortDevice();

    }

    private static int toInt(byte[] b) {
        if (b.length != 0) {
            int x = (0 << 24) | (0 << 16)
                    | ((b[1] & 0xFF) << 8) | ((b[0] & 0xFF) << 0);
            return x;
        } else {
            return -1;
        }
    }

    private void setIntArray() {
        // byteArray is iterated through
        for (byte[] bytes :
                inputByteArrays) {
            int length = bytes.length;
            for (int i = 0; i < length; i++) {
                Log.d(TAG, "setIntArray: Contents of bytes array " + (int) bytes[i]);
                intArray.add((int) bytes[i]);
            }
        }
    }

    private void readArray() {
        for (int i = 0; i < serialInArray.length; i++) {
            Log.d(TAG, "readArray: Node " + i + " = " + serialInArray[i]);
        }

    }

    private void findSerialPortDevice() {
        // This snippet will try to open the first encountered usb device connected, excluding usb root hubs
        HashMap<String, UsbDevice> usbDevices = manager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 10755) {
                    // There is a device connected to our Android device. Try to open it as a Serial Port.
                    Log.d(TAG, "findSerialPortDevice: There is a device connected to our Android device. Try to open it as a Serial Port. ");
                    requestUserPermission();
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
            if (!keep) {
                // There is no USB devices connected (but usb host were listed). Send an intent to MainActivity.
                Intent intent = new Intent(ACTION_NO_USB);
                context.sendBroadcast(intent);
            }
        } else {
            // There is no USB devices connected. Send an intent to MainActivity
            Intent intent = new Intent(ACTION_NO_USB);
            context.sendBroadcast(intent);
        }
    }

    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        context.registerReceiver(usbReceiver, filter);
    }

    /*
        * Request user permission. The response will be received in the BroadcastReceiver
        */
    private void requestUserPermission() {
        Log.d(TAG, "requestUserPermission: Sending permission request");
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        manager.requestPermission(device, mPendingIntent);
    }

    public int[] getSerialArray() {
        return serialInArray;
    }

    public boolean getRender() {
        return render;
    }

    public void setRender(boolean render) {
        this.render = render;
    }

    public ArrayList<Integer> getIntArray() {
        return intArray;
    }

    /*
     * A simple thread to open a serial port.
     * Although it should be a fast operation. moving usb operations away from UI thread is a good thing.
     */
    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            Log.d(TAG, "run: Inside ConnectionThread.");
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
            if (serialPort != null) {
                if (serialPort.open()) {
                    serialPortConnected = true;
                    serialPort.setBaudRate(BAUD_RATE);
                    serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                    serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                    serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                    serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                    serialPort.read(mCallback);

//                    serialPort.getCTS(ctsCallback);
//                    serialPort.getDSR(dsrCallback);

                    // Some Arduinos would need some sleep because firmware wait some time to know whether a new sketch is going
                    // to be uploaded or not
//                    try {
//                        Thread.sleep(2000); // sleep some. YMMV with different chips.
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

                    // Everything went as expected. Send an intent to MainActivity
                    Intent intent = new Intent(ACTION_USB_READY);
                    context.sendBroadcast(intent);
                } else {
                    // Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
                    // Send an Intent to Main Activity
                    if (serialPort instanceof CDCSerialDevice) {
                        Intent intent = new Intent(ACTION_CDC_DRIVER_NOT_WORKING);
                        context.sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(ACTION_USB_DEVICE_NOT_WORKING);
                        context.sendBroadcast(intent);
                    }
                }
            } else {
                // No driver for given device, even generic CDC driver could not be loaded
                Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
                context.sendBroadcast(intent);
            }
        }
    }
}
