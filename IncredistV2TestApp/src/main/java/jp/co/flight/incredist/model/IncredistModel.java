package jp.co.flight.incredist.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.preference.PreferenceManager;

import java.util.List;

import jp.co.flight.incredist.android.Incredist;
import jp.co.flight.incredist.android.IncredistManager;
import jp.co.flight.incredist.android.IncredistV2TestApp.BR;
import jp.co.flight.incredist.android.OnFailureFunction;
import jp.co.flight.incredist.android.OnSuccessFunction;
import jp.co.flight.incredist.android.OnSuccessVoidFunction;
import jp.co.flight.incredist.android.model.DeviceInfo;
import jp.co.flight.incredist.android.model.FelicaCommandResult;

/**
 * Incredist テストプログラム用モデル.
 */
public interface IncredistModel extends Observable {

    void newIncredistObject();

    void bleStartScan(OnSuccessFunction<List<String>> success, OnFailureFunction failure);

    List<String> getDeviceList();

    void connect(OnSuccessFunction<Incredist> success, OnFailureFunction failure);

    void disconnect(OnSuccessFunction<Incredist> success, OnFailureFunction failure);

    void getDeviceInfo(OnSuccessFunction<DeviceInfo> success, OnFailureFunction failure);

    void felicaOpen(boolean withLed, OnSuccessVoidFunction success, OnFailureFunction failure);

    void felicaSendCommand(OnSuccessFunction<FelicaCommandResult> success, OnFailureFunction failure);

    void felicaClose(OnSuccessVoidFunction success, OnFailureFunction failure);

    void release();

    void clearIncredist();

    @Bindable
    String getSelectedDevice();

    void setSelectedDevice(String deviceName);

    String getApiVersion();

    void auto(OnSuccessFunction<String> success, OnFailureFunction failure);

    class Impl extends BaseObservable implements IncredistModel {
        private static final String PREFERENCE_KEY_DEVICE_NAME = "device_name";

        private final Context mContext;
        private IncredistManager mIncredistManager;
        private Incredist mIncredist;

        private List<String> mDeviceList;
        private String mSelectedDevice;

        public Impl(Context context) {
            mContext = context;

            SharedPreferences sp = getSharedPreference();
            mSelectedDevice = sp.getString(PREFERENCE_KEY_DEVICE_NAME, "not selected");
        }

        //-- methods from IncredistModel.
        @Override
        public void newIncredistObject() {
            mIncredistManager = new IncredistManager(mContext);
        }

        @Override
        public void bleStartScan(OnSuccessFunction<List<String>> success, OnFailureFunction failure) {
            mIncredistManager.bleStartScan(null, 5000, (deviceList) -> {
                mDeviceList = deviceList;
                success.onSuccess(deviceList);
            }, failure);
        }

        @Override
        public List<String> getDeviceList() {
            return mDeviceList;
        }

        @Override
        public void connect(OnSuccessFunction<Incredist> success, OnFailureFunction failure) {
            mIncredistManager.connect(mSelectedDevice, 3000, (incredist) -> {
                mIncredist = incredist;
                success.onSuccess(incredist);
            }, failure);
        }

        @Override
        public void disconnect(OnSuccessFunction<Incredist> success, OnFailureFunction failure) {
            if (mIncredist != null) {
                mIncredist.disconnect(success, failure);
            } else {
                failure.onFailure(-1);
            }
        }

        @Override
        public void getDeviceInfo(OnSuccessFunction<DeviceInfo> success, OnFailureFunction failure) {
            if (mIncredist != null) {
//                mIncredist.getDeviceInfo(success, failure);
            } else {
                failure.onFailure(-1);
            }
        }

        @Override
        public void felicaOpen(boolean withLed, OnSuccessVoidFunction success, OnFailureFunction failure) {
            if (mIncredist != null) {
//                mIncredist.felicaOpen(withLed, success, failure);
            } else {
                failure.onFailure(-1);
            }
        }

        @Override
        public void felicaSendCommand(OnSuccessFunction<FelicaCommandResult> success, OnFailureFunction failure) {
            if (mIncredist != null) {
                byte[] felicaCommand = {(byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00};
//                mIncredist.felicaSendCommand(felicaCommand, success, failure);
            } else {
                failure.onFailure(-1);
            }
        }

        @Override
        public void felicaClose(OnSuccessVoidFunction success, OnFailureFunction failure) {
            if (mIncredist != null) {
//                mIncredist.felicaClose(success, failure);
            } else {
                failure.onFailure(-1);
            }
        }

        @Override
        public void release() {
            mIncredistManager.release();
        }

        @Override
        public void clearIncredist() {
            mIncredistManager = null;
        }

        //-- methods for DataBinding.
        public String getSelectedDevice() {
            return mSelectedDevice;
        }

        public void setSelectedDevice(String device) {
            mSelectedDevice = device;
            SharedPreferences sp = getSharedPreference();
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(PREFERENCE_KEY_DEVICE_NAME, device);
            editor.apply();

            notifyPropertyChanged(BR.selectedDevice);
        }

        @Override
        public String getApiVersion() {
            return mIncredistManager.getApiVersion();
        }

        @Override
        public void auto(OnSuccessFunction<String> success, OnFailureFunction failure) {
            mIncredistManager.connect(mSelectedDevice, 3000, (incredist) -> {
                incredist.getSerialNumber(success, failure);
            }, failure);
        }

        private SharedPreferences getSharedPreference() {
            return PreferenceManager.getDefaultSharedPreferences(mContext);
        }
    }
}
