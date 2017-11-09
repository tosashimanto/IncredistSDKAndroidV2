package jp.co.flight.incredist.model;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;

import java.util.List;

import jp.co.flight.incredist.BR;
import jp.co.flight.incredist.android.Incredist;
import jp.co.flight.incredist.android.IncredistManager;
import jp.co.flight.incredist.android.OnFailureFunction;
import jp.co.flight.incredist.android.OnSuccessFunction;

/**
 * Incredist テストプログラム用モデル.
 */
public interface IncredistModel extends Observable {

    void newIncredistObject();
    void startScan(OnSuccessFunction<List<String>> success, OnFailureFunction<Void> failure);
    List<String> getDeviceList();
    void connect(OnSuccessFunction<Incredist> success, OnFailureFunction<Void> failure);
    void disconnect(OnSuccessFunction<Incredist> success, OnFailureFunction<Incredist> failure);
    void getSerialNumber(OnSuccessFunction<String> success, OnFailureFunction<Void> failure);
    void release();
    void clearIncredist();

    @Bindable
    String getSelectedDevice();
    void setSelectedDevice(String deviceName);

    class Impl extends BaseObservable implements IncredistModel {
        private final Context mContext;
        private IncredistManager mIncredistManager;
        private Incredist mIncredist;

        private List<String> mDeviceList;
        private String mSelectedDevice = "SamilF40726DF1105";

        public Impl(Context context) {
            mContext = context;
        }

        //-- methods from IncredistModel.
        @Override
        public void newIncredistObject() {
            mIncredistManager = new IncredistManager(mContext);
        }

        @Override
        public void startScan(OnSuccessFunction<List<String>> success, OnFailureFunction<Void> failure) {
            mIncredistManager.startScan(null, 5000, (deviceList)->{
                mDeviceList = deviceList;
                success.onSuccess(deviceList);
            }, failure);
        }

        @Override
        public List<String> getDeviceList() {
            return mDeviceList;
        }

        @Override
        public void connect(OnSuccessFunction<Incredist> success, OnFailureFunction<Void> failure) {
            mIncredistManager.connect(mSelectedDevice, 3000, (incredist)->{
                mIncredist = incredist;
                success.onSuccess(incredist);
            }, failure);
        }

        @Override
        public void disconnect(OnSuccessFunction<Incredist> success, OnFailureFunction<Incredist> failure) {
            if (mIncredist != null) {
                mIncredist.disconnect(success, failure);
            } else {
                failure.onFailure(-1, null);
            }
        }

        @Override
        public void getSerialNumber(OnSuccessFunction<String> success, OnFailureFunction<Void> failure) {
            if (mIncredist != null) {
                mIncredist.getSerialNumber(success, failure);
            } else {
                failure.onFailure(-1, null);
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
            notifyPropertyChanged(BR.selectedDevice);
        }

    }
}
