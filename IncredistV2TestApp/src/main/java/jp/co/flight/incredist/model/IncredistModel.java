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
import jp.co.flight.incredist.android.model.FelicaCommandResult;

/**
 * Incredist テストプログラム用モデル.
 */
public interface IncredistModel extends Observable {

    void newIncredistObject();

    void bleStartScan(OnSuccessFunction<List<String>> success, OnFailureFunction<Void> failure);

    List<String> getDeviceList();

    void connect(OnSuccessFunction<Incredist> success, OnFailureFunction<Void> failure);

    void disconnect(OnSuccessFunction<Incredist> success, OnFailureFunction<Incredist> failure);

    void getSerialNumber(OnSuccessFunction<String> success, OnFailureFunction<Void> failure);

    void felicaOpen(boolean withLed, OnSuccessFunction<Void> success, OnFailureFunction<Void> failure);

    void felicaSendCommand(OnSuccessFunction<FelicaCommandResult> success, OnFailureFunction<Void> failure);

    void felicaClose(OnSuccessFunction<Void> success, OnFailureFunction<Void> failure);

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
        public void bleStartScan(OnSuccessFunction<List<String>> success, OnFailureFunction<Void> failure) {
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
        public void connect(OnSuccessFunction<Incredist> success, OnFailureFunction<Void> failure) {
            mIncredistManager.connect(mSelectedDevice, 3000, (incredist) -> {
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
        public void felicaOpen(boolean withLed, OnSuccessFunction<Void> success, OnFailureFunction<Void> failure) {
            if (mIncredist != null) {
                mIncredist.felicaOpen(withLed, success, failure);
            } else {
                failure.onFailure(-1, null);
            }
        }

        @Override
        public void felicaSendCommand(OnSuccessFunction<FelicaCommandResult> success, OnFailureFunction<Void> failure) {
            if (mIncredist != null) {
                byte[] felicaCommand = {(byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00};
                mIncredist.felicaSendCommand(felicaCommand, success, failure);
            } else {
                failure.onFailure(-1, null);
            }
        }

        @Override
        public void felicaClose(OnSuccessFunction<Void> success, OnFailureFunction<Void> failure) {
            if (mIncredist != null) {
                mIncredist.felicaClose(success, failure);
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
