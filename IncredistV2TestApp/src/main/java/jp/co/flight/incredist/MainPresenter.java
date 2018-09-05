package jp.co.flight.incredist;

import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.text.Layout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import jp.co.flight.android.bluetooth.le.BluetoothPeripheral;
import jp.co.flight.incredist.android.Incredist;
import jp.co.flight.incredist.android.IncredistManager;
import jp.co.flight.incredist.android.IncredistV2TestApp.BuildConfig;
import jp.co.flight.incredist.android.IncredistV2TestApp.databinding.FragmentMainBinding;
import jp.co.flight.incredist.android.model.CreditCardType;
import jp.co.flight.incredist.android.model.EmvTagType;
import jp.co.flight.incredist.android.model.EmvTransactionType;
import jp.co.flight.incredist.android.model.EncryptionMode;
import jp.co.flight.incredist.android.model.LedColor;
import jp.co.flight.incredist.model.IncredistModel;
import jp.co.flight.incredist.model.PinEntryDParam;

/**
 * MainActivity 用 Presenter インタフェース.
 */
public interface MainPresenter {
    void onUsbDeviceList();

    void onStartScan();

    void onSelectDevice();

    void setSelectedDevice(String deviceName);

    void onFindUsbDevice();

    void onConnect();

    void onConnectUsb();

    void onGetDeviceInfo();

    void onGetProductInfo();

    void onDisconnect();

    void onStop();

    void onCancel();

    void onRelease();

    void onRestart();

    void onAuto();

    void onFelicaOpen();

    void onFelicaLedColor();

    void felicaLedColor(LedColor color);

    void onFelicaOpenWithoutLed();

    void onFelicaSend();

    void onFelicaClose();

    void onEmvMessage();

    void onTfpMessage();

    void onSetLed();

    void emvDisplayMessage(int type, String message);

    void tfpDisplayMessage(int type, String message);

    void setLedColor(LedColor color, boolean isOn);

    void onSdm();

    void onCredit();

    void scanCreditCard(EnumSet<CreditCardType> cardType, long amount, EmvTagType tagType, int aidSetting, EmvTransactionType transactionType, boolean fallback, long timeout);

    void onCheckCardStatus();

    void setEncryptionMode(EncryptionMode mode);

    void onMj2();

    void onPinD();

    void pinEntryD(PinEntryDParam setting);

    void onRtcGetTime();

    void onRtcSetTime();

    void onRtcSetCurrent();

    void setDateTime(Calendar cal);

    void onEmoneyBlink();

    void emoneyBlink(boolean isBlink, LedColor color, int duration);

    void addLog(String message);


    /**
     * MainActivity 用 Presenter 実体クラス.
     */
    class Impl implements MainPresenter {
        private final MainFragment mFragment;
        private final FragmentMainBinding mBinding;
        private final IncredistModel mIncredist;
        private final Handler mMainThreadHandler;

        IncredistManager.IncredistConnectionListener mConnectionListener = new IncredistManager.IncredistConnectionListener() {
            @Override
            public void onConnectIncredist(Incredist incredist) {
                addLog(String.format(Locale.JAPANESE, "connected: %s", incredist.getDeviceName()));
            }

            @Override
            public void onConnectFailure(int errorCode) {
                addLog(String.format(Locale.JAPANESE, "connect failure %d", errorCode));
            }

            @Override
            public void onDisconnectIncredist(Incredist incredist) {
                addLog(String.format(Locale.JAPANESE, "disconnected: %s", incredist.getDeviceName()));
            }
        };

        Impl(MainFragment fragment, FragmentMainBinding binding, IncredistModel model) {
            mFragment = fragment;
            mBinding = binding;
            mIncredist = model;

            mIncredist.newIncredistObject();
            mMainThreadHandler = new Handler(Looper.getMainLooper());

            addLog(String.format("%s:%s API:%s", BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME, mIncredist.getApiVersion()));
        }

        @Override
        public void onUsbDeviceList() {
            mFragment.usbDeviceList();
        }

        @Override
        public void onStartScan() {
            addLog("bleStartScan");
            mIncredist.bleStartScan((List<BluetoothPeripheral> scanResult) -> {
                addLog(String.format(Locale.JAPANESE, "bleStartScan result %d", scanResult.size()));
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "bleStartScan failure %d", errorCode));
            });
        }

        @Override
        public void onSelectDevice() {
            addLog("selectDevice");
            List<BluetoothPeripheral> peripherals = mIncredist.getDeviceList();

            if (peripherals == null) {
                addLog("no device list");
                return;
            }

            ArrayList<String> deviceNames = new ArrayList<>();
            for (BluetoothPeripheral peripheral : peripherals) {
                deviceNames.add(peripheral.getDeviceName());
            }
            if (peripherals.size() > 0) {
                mFragment.showDeviceListDialog(deviceNames);
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
        public void onFindUsbDevice() {
            addLog("findUsbDevice");
            UsbDevice device = mIncredist.findUsbDevice();
            if (device == null) {
                addLog("UsbDevice is null");
            } else {
                addLog("found UsbDevice");
            }
        }

        @Override
        public void onConnect() {
            addLog("connect");
            mIncredist.connect(mConnectionListener);
        }

        @Override
        public void onConnectUsb() {
            addLog("usbConenct");
            UsbDevice device = mIncredist.getUsbDevice();
            if (device == null) {
                addLog("UsbDevice is null");
            } else {
                if (mFragment.checkUsbPermission(device)) {
                    mIncredist.connect(device, mConnectionListener);
                } else {
                    addLog("UsbPermission failed");
                }
            }
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
        public void onGetProductInfo() {
            addLog("getProductInfo");
            mIncredist.getProductInfo(product -> {
                addLog(String.format(Locale.JAPANESE, "serial: %s firm: %s type: %s", product.getSerialNumber(), product.getFirmwareVersion(), product.getProductType().name()));
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "getProductInfo failure %d", errorCode));
            });
        }

        @Override
        public void onDisconnect() {
            addLog("disconnect");
            mIncredist.disconnect();
        }

        @Override
        public void onStop() {
            addLog("stop");
            mIncredist.stop(() -> {
                addLog("stop success");
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "stop failure %d", errorCode));
            });
        }

        @Override
        public void onCancel() {
            addLog("cancel");
            mIncredist.cancel(() -> {
                addLog("cancel success");
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "cancel failure %d", errorCode));
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
        public void onFelicaOpen() {
            addLog("felicaOpen");
            mIncredist.felicaOpen(true, () -> {
                addLog("felicaOpen success");
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "felicaOpen failure %d", errorCode));
            });
        }

        @Override
        public void onFelicaLedColor() {
            addLog("felica LED setting");
            mFragment.showFelicaLedColorDialog();
        }

        @Override
        public void felicaLedColor(LedColor color) {
            addLog("felicaLedColor");
            mIncredist.felicaLedColor(color, () -> {
                addLog("felicaLedColor success");
            }, errorCode -> {
                addLog(String.format(Locale.JAPANESE, "felicaLedColor failure %d", errorCode));
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

        @Override
        public void onEmvMessage() {
            addLog("emvMessage setting");
            mFragment.showEmvDisplayMessageDialog();
        }

        @Override
        public void onTfpMessage() {
            addLog("tfpMessage setting");
            mFragment.showTfpDisplayMessageDialog();
        }

        @Override
        public void onSetLed() {
            addLog("led");
            mFragment.showSetLedColorDialog();
        }

        @Override
        public void emvDisplayMessage(int type, String message) {
            addLog("emvDisplayMessage");
            mIncredist.emvDisplayMessage(type, message, () -> {
                addLog("emvDisplayMessage success");
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "emvDisplayMessage failure %d", errorCode));
            });
        }

        @Override
        public void tfpDisplayMessage(int type, String message) {
            addLog("tfpMessage");
            mIncredist.tfpDisplayMessage(type, message, () -> {
                addLog("tfpMessage success");
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "tfpMessage failure %d", errorCode));
            });
        }

        @Override
        public void setLedColor(LedColor color, boolean isOn) {
            addLog("led");
            mIncredist.setLedColor(color, isOn, () -> {
                addLog("led success");
            }, errorCode -> {
                addLog(String.format(Locale.JAPANESE, "led failure %d", errorCode));
            });
        }

        @Override
        public void onSdm() {
            addLog("sdm setting");
            mFragment.showEncryptSettingDialog();
        }

        @Override
        public void onCredit() {
            addLog("credit setting");
            mFragment.showCreditSettingDialog();
        }

        @Override
        public void scanCreditCard(EnumSet<CreditCardType> cardTypeSet, long amount, EmvTagType tagType, int aidSetting, EmvTransactionType transactionType, boolean fallback, long timeout) {
            addLog("credit");
            mIncredist.scanCreditCard(cardTypeSet, amount, tagType, aidSetting, transactionType, fallback, timeout, (result) -> {
                addLog("credit emvsuccess");
            }, (magResult) -> {
                addLog("credit magsuccess");
            }, errorCode -> {
                addLog(String.format(Locale.JAPANESE, "credit failure %d", errorCode));
            });
        }

        @Override
        public void onCheckCardStatus() {
            addLog("checkCardStatus");
            mIncredist.checkCardStatus((inserted) -> {
                addLog(String.format(Locale.JAPANESE, "checkCardStatus %s", inserted.name()));
            }, errorCode -> {
                addLog(String.format(Locale.JAPANESE, "credit failure %d", errorCode));
            });
        }

        @Override
        public void setEncryptionMode(EncryptionMode mode) {
            addLog("sdm");
            mIncredist.setEncryptionMode(mode, () -> {
                addLog("sdm success");
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "sdm failure %d", errorCode));
            });
        }

        @Override
        public void onMj2() {
            addLog("mj2");
            mIncredist.scanMagnetic(20000, (magCard) -> {
                addLog(String.format(Locale.JAPANESE, "mj2 success : %s", magCard.getCardType().name()));

                addLog(String.format(Locale.JAPANESE, "  track1: %s", hexString(magCard.getDec1().getTrack1())));

            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "mj2 failure %d", errorCode));
            });
        }

        @Override
        public void onPinD() {
            addLog("pind setting");
            mFragment.showPinEntryDParamDialog();
        }

        @Override
        public void pinEntryD(PinEntryDParam param) {
            addLog("pind");
            mIncredist.pinEntryD(param, (pinEntry) -> {
                addLog(String.format(Locale.JAPANESE, "pind success ksn:%s pinData:%s", hexString(pinEntry.getKsn()), hexString(pinEntry.getPinData())));
            }, (errorCode) -> {
                addLog(String.format(Locale.JAPANESE, "pind failure %d", errorCode));
            });

        }

        @Override
        public void onRtcGetTime() {
            addLog("rtcGetTime");
            mIncredist.rtcGetTime(cal -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss", Locale.JAPANESE);
                addLog(String.format(Locale.JAPANESE, "rtcGetTime success %s", sdf.format(cal.getTime())));
            }, errorCode -> {
                addLog(String.format(Locale.JAPANESE, "rtcGetTime failure %d", errorCode));
            });
        }

        @Override
        public void onRtcSetTime() {
            mFragment.showDateTimeDialog();
        }

        @Override
        public void onRtcSetCurrent() {
            addLog("rtcSetCurrentTime");
            mIncredist.rtcSetCurrentTime(() -> {
                addLog("rtcSetCurrentTime success");
            }, errorCode -> {
                addLog(String.format(Locale.JAPANESE, "rtcSetCurrentTime failure %d", errorCode));
            });
        }

        @Override
        public void setDateTime(Calendar cal) {
            addLog("rtcSetTime");
            mIncredist.rtcSetTime(cal, () -> {
                addLog("rtcSetTime success");
            }, errorCode -> {
                addLog(String.format(Locale.JAPANESE, "rtcSetTime failure %d", errorCode));
            });
        }

        @Override
        public void onEmoneyBlink() {
            mFragment.showEmoneyBlinkDialog();
        }

        @Override
        public void emoneyBlink(boolean isBlink, LedColor color, int duration) {
            addLog("emoneyBlink");
            mIncredist.emoneyBlink(isBlink, color, duration, (isOn) -> {
                addLog(String.format(Locale.JAPANESE, "emoneyBlink success %s", isOn));
            }, errorCode -> {
                addLog(String.format(Locale.JAPANESE, "emoneyBlink failure %d", errorCode));
            });
        }

        public void addLog(String message) {
            String level = "-";
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.JAPANESE);
            final String logMessage = String.format(Locale.JAPANESE, "%s %d %d %s %s", sdf.format(new Date()), Process.myPid(), Process.myTid(), level, message);
            mMainThreadHandler.post(() -> {
                mBinding.textLog.append(logMessage + "\n");
                Layout layout = mBinding.textLog.getLayout();
                if (layout != null) {
                    int offsetBottom = layout.getLineBottom(layout.getLineCount() - 1);
                    int scrollY = offsetBottom - mBinding.textLog.getHeight();
                    scrollY = scrollY < 0 ? 0 : scrollY;
                    mBinding.textLog.setScrollY(scrollY);
                }
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
