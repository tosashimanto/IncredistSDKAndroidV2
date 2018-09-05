package jp.co.flight.incredist;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import jp.co.flight.incredist.android.IncredistV2TestApp.R;
import jp.co.flight.incredist.model.IncredistModel;

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener {

    private static final String FRAGMENT_TAG_MAIN = "fragment_tag_main";

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
                            // 本来はここでパーミッション取得後の処理を書く, 現状は Presenter 周りの実装があまり良くないので一旦何もしない
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    private PendingIntent mPermissionIntent;

    private IncredistModel.Impl mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

        mModel = new IncredistModel.Impl(this);

        MainFragment fragment = MainFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.container, fragment, FRAGMENT_TAG_MAIN);
        ft.commit();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mUsbReceiver);

        super.onDestroy();
    }

    @Override
    public IncredistModel getModel() {
        return mModel;
    }

    @Override
    public boolean checkUsbPermission(UsbDevice device) {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        if (usbManager != null) {
            if (usbManager.hasPermission(device)) {
                return true;
            }

            usbManager.requestPermission(device, mPermissionIntent);
        }

        return false;
    }
}
