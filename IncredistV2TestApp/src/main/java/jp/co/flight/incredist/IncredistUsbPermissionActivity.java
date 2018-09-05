package jp.co.flight.incredist;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

import jp.co.flight.incredist.android.Incredist;
import jp.co.flight.incredist.android.IncredistManager;
import jp.co.flight.incredist.android.IncredistV2TestApp.R;

public class IncredistUsbPermissionActivity extends AppCompatActivity {

    private static final String TAG = "IncrUsbPerm";
    private static final String ACTION_USB_PERMISSION =
            "jp.co.flight.android.incredist.USB_PERMISSION";

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public static final String TAG = "UsbReceiver";

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication

                            connectDevice(device);
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    private PendingIntent mPermissionIntent;
    private Handler mHandler;
    private Incredist mIncredist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incredist_usb_permission);

        mHandler = new Handler();

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

        Intent intent = getIntent();
        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (device != null) {
            connectDevice(device);
        }

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mUsbReceiver);

        super.onDestroy();
    }

    void connectDevice(UsbDevice device) {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        if (usbManager == null) {
            return;
        }

        if (!usbManager.hasPermission(device)) {
            usbManager.requestPermission(device, mPermissionIntent);
            return;
        }

        IncredistManager manager = new IncredistManager(this);
        manager.connect(device, new IncredistManager.IncredistConnectionListener() {
            @Override
            public void onConnectIncredist(Incredist incredist) {
                Log.d(TAG, "onConnectIncredist");

                mIncredist = incredist;
                mHandler.post(IncredistUsbPermissionActivity.this::getSerialNumber);
            }

            @Override
            public void onConnectFailure(int errorCode) {

            }

            @Override
            public void onDisconnectIncredist(Incredist incredist) {

            }
        });
    }

    private void getSerialNumber() {
        TextView textView = findViewById(R.id.text);

        mIncredist.getSerialNumber((serialNumber) -> {
            mHandler.post(() -> {
                textView.setText(serialNumber);
            });
        }, (errorCode) -> {
            mHandler.post(() -> {
                textView.setText(String.format(Locale.US, "error %d", errorCode));
            });
        });
    }
}
