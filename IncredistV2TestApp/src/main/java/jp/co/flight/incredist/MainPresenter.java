package jp.co.flight.incredist;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jp.co.flight.incredist.databinding.FragmentMainBinding;
import jp.co.flight.incredist.model.IncredistModel;

/**
 * MainActivity 用 Presenter インタフェース.
 */
public interface MainPresenter {
    void onStartScan();

    void onSelectDevice();

    void onConnect();

    void onGetSerial();

    void onDisconnect();

    void setSelectedDevice(String deviceName);

    void onFelicaOpen();

    void onFelicaSend();

    void onFelicaClose();

    void addLog(String message);

    /**
     * MainActivity 用 Presenter 実体クラス.
     */
    class Impl implements MainPresenter {
        private final MainFragment mFragment;
        private final FragmentMainBinding mBinding;
        private final IncredistModel mIncredist;
        private final Handler mMainThreadHandler;

        Impl(MainFragment fragment, FragmentMainBinding binding, IncredistModel model) {
            mFragment = fragment;
            mBinding = binding;
            mIncredist = model;

            mIncredist.newIncredistObject();
            mMainThreadHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void onStartScan() {
            addLog("bleStartScan");
            mIncredist.bleStartScan((List<String> scanResult) -> {
                addLog(String.format(Locale.JAPANESE, "onStartScan result %d", scanResult.size()));
            }, (errorCode, failure) -> {
                addLog(String.format(Locale.JAPANESE, "onStartScan failure %d", errorCode));
            });
        }

        @Override
        public void onSelectDevice() {
            addLog("selectDevice");
            List<String> devices = mIncredist.getDeviceList();
            mFragment.startSelectDevice((ArrayList<String>) devices);
        }

        @Override
        public void onConnect() {
            addLog("connect");
            mIncredist.connect((incredist) -> {
                addLog(String.format(Locale.JAPANESE, "connected: %s", incredist.getDeviceName()));
            }, (errorCode, failure) -> {
                addLog(String.format(Locale.JAPANESE, "connect failure %d", errorCode));
            });
        }

        @Override
        public void onGetSerial() {
            addLog("getSerialNumber");
            mIncredist.getSerialNumber(serial -> {
                addLog(String.format(Locale.JAPANESE, "serial: %s", serial));
            }, (errorCode, failure) -> {
                addLog(String.format(Locale.JAPANESE, "getSerialNumber failure %d", errorCode));
            });
        }

        @Override
        public void onDisconnect() {
            addLog("disconnect");
            mIncredist.disconnect(incredist -> {
                addLog("disconnected");
            }, (errorCode, incredist) -> {
                addLog(String.format(Locale.JAPANESE, "disconnect failure %d", errorCode));
            });
        }

        @Override
        public void setSelectedDevice(String deviceName) {
            addLog(String.format("setSelectedDevice:%s", deviceName));
            mIncredist.setSelectedDevice(deviceName);
        }

        @Override
        public void onFelicaOpen() {
            addLog("felicaOpen");
            mIncredist.felicaOpen(success -> {
                addLog("felicaOpen success");
            }, (errorCode, failure) -> {
                addLog(String.format(Locale.JAPANESE, "felicaOpen failure %d", errorCode));
            });
        }

        @Override
        public void onFelicaSend() {
            addLog("felicaSend");
            mIncredist.felicaSendCommand(success -> {
                addLog(String.format(Locale.JAPANESE, "felicaSend success status1:%d status2:%d result:%s",
                        success.getStatus1(), success.getStatus2(), hexString(success.getResultData())));
            }, (errorCode, failure) -> {
                addLog(String.format(Locale.JAPANESE, "felicaSend failure %d", errorCode));
            });
        }

        @Override
        public void onFelicaClose() {
            addLog("felicaClose");
            mIncredist.felicaClose(success -> {
                addLog("felicaClose success");
            }, (errorCode, failure) -> {
                addLog(String.format(Locale.JAPANESE, "felicaClose failure %d", errorCode));
            });
        }

        public void addLog(String message) {
            String level = "-";
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.JAPANESE);
            final String logMessage = String.format(Locale.JAPANESE, "%s %d %d %s %s", sdf.format(new Date()), Process.myPid(), Process.myTid(), level, message);
            mMainThreadHandler.post(() -> mBinding.textLog.append(logMessage + "\n"));
        }

        private String hexString(byte[] bytes) {
            StringBuilder sb = new StringBuilder();

            for (byte b : bytes) {
                sb.append(String.format("%02x:", b));
            }

            return sb.toString();
        }
    }
}
