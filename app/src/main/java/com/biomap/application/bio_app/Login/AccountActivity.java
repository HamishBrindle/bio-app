package com.biomap.application.bio_app.Login;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.CustomFontTextView;
import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.SerialBuffer;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";
    private static final String ACTION_USB_PERMISSION = "com.biomap.application.bio_app.USB_PERMISSION";
    public static final String ACTION_USB_READY = "com.biomap.application.bio_app.USB_READY";
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_NOT_SUPPORTED = "com.biomap.application.bio_app.USB_NOT_SUPPORTED";
    public static final String ACTION_NO_USB = "com.biomap.application.bio_app.NO_USB";
    public static final String ACTION_USB_PERMISSION_GRANTED = "com.biomap.application.bio_app.USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "com.biomap.application.bio_app.USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_USB_DISCONNECTED = "com.biomap.application.bio_app.USB_DISCONNECTED";
    public static final String ACTION_CDC_DRIVER_NOT_WORKING = "com.biomap.application.bio_app.ACTION_CDC_DRIVER_NOT_WORKING";
    public static final String ACTION_USB_DEVICE_NOT_WORKING = "com.biomap.application.bio_app.ACTION_USB_DEVICE_NOT_WORKING";
    private static final int BAUD_RATE = 115200;
    public static boolean SERVICE_CONNECTED = false;


    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private String[] name;
    private UsbDevice device;
    private UsbManager manager;
    private ImageView testBtn;
    private UsbSerialDevice serialPort;
    private String dataSet;
    private UsbDeviceConnection connection;
    private TextView textView;
    boolean send;
    int[] serialInArray = new int[225];
    int serialCount = 0;
    private SerialBuffer serialBuffer;
    private CustomFontTextView mProfileName;
    private boolean serialPortConnected;
    private boolean firstContact = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);


        mProfileName = (CustomFontTextView) findViewById(R.id.profile_name);
        setupFirebase();
        setUpButtons();
        testBtn = (ImageView) findViewById(R.id.logo_login_register_imageview);
        textView = (TextView) findViewById(R.id.account_textView);

        //USB STUFF
        serialBuffer = new SerialBuffer(true);
        serialPortConnected = false;
        AccountActivity.SERVICE_CONNECTED = true;
        setFilter();
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        findSerialPortDevice();

    }

    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            char data = (char) arg0[0];
            if (!firstContact) {
                if (data == 'A') {
                    serialBuffer.clearReadBuffer();
                    serialPort.write("A".getBytes());
                    firstContact = true;
                }
            } else {
                serialInArray[serialCount] = toInt(arg0);
                serialCount++;
                if (serialCount > 224) {
                    serialPort.write("A".getBytes());
                    serialCount = 0;
                    readArray();
                }
            }
        }
    };

    private void readArray() {
        for (int i = 0; i < serialInArray.length; i++) {
            Log.d(TAG, "readArray: Node " + i + " = " + serialInArray[i]);
        }

    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(text);

            }
        });
    }


    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Log.d(TAG, "onReceive: Receiving something");
            if (arg1.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) // User accepted our USB connection. Try to open the device as a serial port
                {
                    Intent intent = new Intent(ACTION_USB_PERMISSION_GRANTED);
                    Toast.makeText(getApplicationContext(), "USB Ready", Toast.LENGTH_SHORT).show();
                    arg0.sendBroadcast(intent);
                    connection = manager.openDevice(device);
                    new ConnectionThread().start();
                } else // User not accepted our USB connection. Send an Intent to the Main Activity
                {
                    Intent intent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
                    Toast.makeText(getApplicationContext(), "USB Not granted", Toast.LENGTH_SHORT).show();

                    arg0.sendBroadcast(intent);
                }
            } else if (arg1.getAction().equals(ACTION_USB_ATTACHED)) {
                Toast.makeText(getApplicationContext(), "USB Attached", Toast.LENGTH_SHORT).show();
                if (!serialPortConnected)
                    Log.d(TAG, "onReceive: A USB device has been attached. Try to open it as a Serial port");
                // A USB device has been attached. Try to open it as a Serial port
                findSerialPortDevice();
            } else if (arg1.getAction().equals(ACTION_USB_DETACHED)) {
                Toast.makeText(getApplicationContext(), "USB Detached", Toast.LENGTH_SHORT).show();
                // Usb device was disconnected. send an intent to the Main Activity
                Intent intent = new Intent(ACTION_USB_DISCONNECTED);
                arg0.sendBroadcast(intent);
                if (serialPortConnected) {
                    serialPort.close();
                }
                serialPortConnected = false;
            } else if (arg1.getAction().equals(ACTION_NO_USB)) {
                Toast.makeText(getApplicationContext(), "No USB connected", Toast.LENGTH_SHORT).show();

            }
        }
    };

    private void setUpButtons() {
        Button signOutBtn = (Button) findViewById(R.id.account_signOut_button);
        Button updateBtn = (Button) findViewById(R.id.account_update_profile);

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                setupFirebase();
                Intent signOutIntent = new Intent(getApplicationContext(), LoginRegisterActivity.class);
                signOutIntent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(signOutIntent);
                finish();
            }
        });
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent profileUpdateIntent = new Intent(getApplicationContext(), ProfileActivity.class);
//                startActivity(profileUpdateIntent);
//                finish();
                textView.setText(dataSet);
            }
        });

    }


    private void setupFirebase() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();

        FirebaseAuth.AuthStateListener mAuthListener = firebaseAuth -> {

            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged.Main:signed_in:" + user.getUid());
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String fullName = dataSnapshot.child("Users").child(mAuth.getCurrentUser().getUid()).child("Name").getValue().toString();
                        name = fullName.split(" ");
                        mProfileName.setText(name[0]);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged.Main:signed_out");
                // Create the logout activity intent.
                Intent register_login_intent = new Intent(this, LoginRegisterActivity.class);
                register_login_intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(register_login_intent);
                finish();

            }
        };

        // Get the user's authentication credentials and check if signed in or not.
        mAuth = FirebaseAuth.getInstance();
        mAuthListener.onAuthStateChanged(mAuth);

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
                sendBroadcast(intent);
            }
        } else {
            // There is no USB devices connected. Send an intent to MainActivity
            Intent intent = new Intent(ACTION_NO_USB);
            sendBroadcast(intent);
        }
    }

    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver, filter);
    }

    /*
        * Request user permission. The response will be received in the BroadcastReceiver
        */
    private void requestUserPermission() {
        Log.d(TAG, "requestUserPermission: Sending permission request");
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        manager.requestPermission(device, mPendingIntent);
    }

    /*
     * A simple thread to open a serial port.
     * Although it should be a fast operation. moving usb operations away from UI thread is a good thing.
     */
    private class ConnectionThread extends Thread {
        @Override
        public void run() {
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
                    //
                    // Some Arduinos would need some sleep because firmware wait some time to know whether a new sketch is going
                    // to be uploaded or not
                    //Thread.sleep(2000); // sleep some. YMMV with different chips.

                    // Everything went as expected. Send an intent to MainActivity
                    Intent intent = new Intent(ACTION_USB_READY);
                    getApplicationContext().sendBroadcast(intent);
                } else {
                    // Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
                    // Send an Intent to Main Activity
                    if (serialPort instanceof CDCSerialDevice) {
                        Intent intent = new Intent(ACTION_CDC_DRIVER_NOT_WORKING);
                        getApplicationContext().sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(ACTION_USB_DEVICE_NOT_WORKING);
                        getApplicationContext().sendBroadcast(intent);
                    }
                }
            } else {
                // No driver for given device, even generic CDC driver could not be loaded
                Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
                getApplicationContext().sendBroadcast(intent);
            }
        }
    }

    private static int toInt(byte[] b) {
        int x = (0 << 24) | (0 << 16)
                | ((b[1] & 0xFF) << 8) | ((b[0] & 0xFF) << 0);
        return x;
    }

}
