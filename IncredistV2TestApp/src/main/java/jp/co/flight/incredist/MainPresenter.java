package jp.co.flight.incredist;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.text.Layout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jp.co.flight.incredist.android.IncredistV2TestApp.BuildConfig;
import jp.co.flight.incredist.android.IncredistV2TestApp.databinding.FragmentMainBinding;
import jp.co.flight.incredist.model.IncredistModel;

/**
 * MainActivity 用 Presenter インタフェース.
 */
public interface MainPresenter {
    void onStartScan();

    void onSelectDevice();

    void setSelectedDevice(String deviceName);

    void onConnect();

    void onGetDeviceInfo();

    void onDisconnect();

    void onRestart();

    void onAuto();

    void onRelease();

    void onFelicaOpen();

    void onFelicaOpenWithoutLed();

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

            addLog(String.format("%s:%s API:%s", BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME, mIncredist.getApiVersion()));
        }

        @Override
        public void onStartScan() {
            addLog("bleStartScan");
            mIncredist.bleStartScan((List<String> scanResult) -> {
                addLog(String.format(Locale.JAPANESE, "onStartScan result %d", scanResult.size()));
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "onStartScan failure %d", errorCode));
            });
        }

        @Override
        public void onSelectDevice() {
            addLog("selectDevice");
            List<String> devices = mIncredist.getDeviceList();
            if (devices != null && devices.size() > 0) {
                mFragment.startSelectDevice((ArrayList<String>) devices);
            } else {
                addLog("not scanned result.");
            }
        }

        @Override
        public void setSelectedDevice(String deviceName) {
            addLog(String.format("setSelectedDevice:%s", deviceName));
            mIncredist.setSelectedDevice(deviceName);
        }

        @Override
        public void onConnect() {
            addLog("connect");
            mIncredist.connect((incredist) -> {
                addLog(String.format(Locale.JAPANESE, "connected: %s", incredist.getDeviceName()));
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "connect failure %d", errorCode));
            });
        }

        @Override
        public void onGetDeviceInfo() {
            addLog("getDeviceInfo");
            mIncredist.getDeviceInfo(serial -> {
                addLog(String.format(Locale.JAPANESE, "serial: %s firm: %s", serial.getSerialNumber(), serial.getFirmwareVersion()));
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "getDeviceInfo failure %d", errorCode));
            });
        }

        @Override
        public void onDisconnect() {
            addLog("disconnect");
            mIncredist.disconnect(incredist -> {
                addLog("disconnected");
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "disconnect failure %d", errorCode));
            });
        }

        @Override
        public void onRestart() {
            addLog("restart");
            mIncredist.restart(() -> {
                addLog("restart succeed");
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "restart failure %d", errorCode));
            });
        }

        @Override
        public void onAuto() {
            addLog("auto");
            mIncredist.auto((serialNumber) -> {
                addLog(String.format(Locale.JAPANESE, "auto serial: %s", serialNumber));
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "auto serial failure %d", errorCode));
            });
        }

        @Override
        public void onRelease() {
            addLog("release");
            mIncredist.release(() -> {
                addLog("release success");
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "release failure %d", errorCode));
            });
        }

        @Override
        public void onFelicaOpen() {
            addLog("felicaOpen");
            mIncredist.felicaOpen(true, () -> {
                addLog("felicaOpen success");
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "felicaOpen failure %d", errorCode));
            });
        }

        @Override
        public void onFelicaOpenWithoutLed() {
            addLog("felicaOpenWithoutLed");
            mIncredist.felicaOpen(false, () -> {
                addLog("felicaOpenWithoutLed success");
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "felicaOpen failure %d", errorCode));
            });
        }

        @Override
        public void onFelicaSend() {
            addLog("felicaSend");
            mIncredist.felicaSendCommand(success -> {
                addLog(String.format(Locale.JAPANESE, "felicaSend success status1:%d status2:%d result:%s",
                        success.getStatus1(), success.getStatus2(), hexString(success.getResultData())));
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "felicaSend failure %d", errorCode));
            });
        }

        @Override
        public void onFelicaClose() {
            addLog("felicaClose");
            mIncredist.felicaClose(() -> {
                addLog("felicaClose success");
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "felicaClose failure %d", errorCode));
            });
        }

        public void addLog(String message) {
            String level = "-";
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.JAPANESE);
            final String logMessage = String.format(Locale.JAPANESE, "%s %d %d %s %s", sdf.format(new Date()), Process.myPid(), Process.myTid(), level, message);
            mMainThreadHandler.post(() -> {
                mBinding.textLog.append(logMessage + "\n");
                Layout layout = mBinding.textLog.getLayout();
                int offsetBottom = layout.getLineBottom(layout.getLineCount() - 1);
                int scrollY = offsetBottom - mBinding.textLog.getHeight();
                scrollY = scrollY < 0 ? 0 : scrollY;
                mBinding.textLog.setScrollY(scrollY);
            });
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
