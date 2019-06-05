package jp.co.flight.incredist;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.hardware.usb.UsbDevice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import jp.co.flight.android.bluetooth.le.BluetoothPeripheral;
import jp.co.flight.incredist.android.IncredistManager;
import jp.co.flight.incredist.android.OnFailureFunction;
import jp.co.flight.incredist.android.OnSuccessFunction;
import jp.co.flight.incredist.android.OnSuccessVoidFunction;
import jp.co.flight.incredist.android.model.PinEntryResult;
import jp.co.flight.incredist.android.model.BootloaderVersion;
import jp.co.flight.incredist.android.model.CreditCardType;
import jp.co.flight.incredist.android.model.DeviceInfo;
import jp.co.flight.incredist.android.model.EmvPacket;
import jp.co.flight.incredist.android.model.EmvTagType;
import jp.co.flight.incredist.android.model.EmvTransactionType;
import jp.co.flight.incredist.android.model.EncryptionMode;
import jp.co.flight.incredist.android.model.FelicaCommandResult;
import jp.co.flight.incredist.android.model.ICCardStatus;
import jp.co.flight.incredist.android.model.LedColor;
import jp.co.flight.incredist.android.model.MagCard;
import jp.co.flight.incredist.android.model.ProductInfo;
import jp.co.flight.incredist.model.DecodedMagCard;
import jp.co.flight.incredist.model.PinEntryDParam;

public interface TestAppContract {
    /**
     * MainActivity 用 Presenter インタフェース.
     */
    interface Presenter {
        void onUsbDeviceList();

        void onStartScan();

        void onSelectDevice();

        void setSelectedDevice(String deviceName);

        void onFindUsbDevice();

        void onConnect();

        void onConnectUsb();

        void onGetDeviceInfo();

        void onGetProductInfo();

        void onGetBootloaderVersion();

        void onDisconnect();

        void onStop();

        void onCancel();

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

        void onPinI();

        void pinEntryI();

        void onRtcGetTime();

        void onRtcSetTime();

        void onRtcSetCurrent();

        void setDateTime(Calendar cal);

        void onEmoneyBlink();

        void emoneyBlink(boolean isBlink, LedColor color, int duration);

        void addLog(String message);


    }

    interface View {
        void showDeviceListDialog(ArrayList<String> devices);

        void showEmvDisplayMessageDialog();

        void showTfpDisplayMessageDialog();

        void showEncryptSettingDialog();

        void showPinEntryDParamDialog();

        void showSetLedColorDialog();

        void showFelicaLedColorDialog();

        void showDateTimeDialog();

        void showCreditSettingDialog();

        void showEmoneyBlinkDialog();

        void usbDeviceList();

        boolean checkUsbPermission(UsbDevice device);
    }

    /**
     * Incredist テストプログラム用モデル.
     */
    interface Model extends Observable {

        void newIncredistObject();

        void bleStartScan(OnSuccessFunction<List<BluetoothPeripheral>> success, OnFailureFunction failure);

        List<BluetoothPeripheral> getDeviceList();

        void connect(IncredistManager.IncredistConnectionListener listener);

        void connect(UsbDevice device, IncredistManager.IncredistConnectionListener connectionListener);

        void disconnect();

        void getDeviceInfo(OnSuccessFunction<DeviceInfo> success, OnFailureFunction failure);

        void getProductInfo(OnSuccessFunction<ProductInfo> success, OnFailureFunction failure);

        void getBootloaderVersion(OnSuccessFunction<BootloaderVersion> success, OnFailureFunction failure);

        void felicaOpen(boolean withLed, OnSuccessVoidFunction success, OnFailureFunction failure);

        void felicaLedColor(LedColor color, OnSuccessVoidFunction success, OnFailureFunction failure);

        void felicaSendCommand(OnSuccessFunction<FelicaCommandResult> success, OnFailureFunction failure);

        void felicaClose(OnSuccessVoidFunction success, OnFailureFunction failure);

        void emvDisplayMessage(int emvMessageType, String emvMessageString, OnSuccessVoidFunction success, OnFailureFunction failure);

        void tfpDisplayMessage(int tfpMessageType, String tfpMessageString, OnSuccessVoidFunction success, OnFailureFunction failure);

        void setEncryptionMode(EncryptionMode mode, OnSuccessVoidFunction success, OnFailureFunction failure);

        void scanMagnetic(long timeout, OnSuccessFunction<DecodedMagCard> success, OnFailureFunction failure);

        void pinEntryD(PinEntryDParam setting, OnSuccessFunction<PinEntryResult> success, OnFailureFunction failure);

        void pinEntryI( OnSuccessFunction<PinEntryResult> success, OnFailureFunction failure);

        void setLedColor(LedColor color, boolean isOn, OnSuccessVoidFunction success, OnFailureFunction failure);

        void scanCreditCard(EnumSet<CreditCardType> cardTypeSet, long amount, EmvTagType tagType, int aidSetting, EmvTransactionType transactionType, boolean fallback, long timeout, OnSuccessFunction<EmvPacket> emvSuccess, OnSuccessFunction<MagCard> magSuccess, OnFailureFunction failure);

        void checkCardStatus(OnSuccessFunction<ICCardStatus> success, OnFailureFunction failure);

        void rtcGetTime(OnSuccessFunction<Calendar> success, OnFailureFunction failure);

        void rtcSetTime(Calendar cal, OnSuccessVoidFunction success, OnFailureFunction failure);

        void rtcSetCurrentTime(OnSuccessVoidFunction success, OnFailureFunction failure);

        void emoneyBlink(boolean isBlink, LedColor color, int duration, OnSuccessFunction<Boolean> success, OnFailureFunction failure);

        void stop(OnSuccessVoidFunction success, OnFailureFunction failure);

        void cancel(OnSuccessVoidFunction success, OnFailureFunction failure);

        @Bindable
        String getSelectedDevice();

        void setSelectedDevice(String deviceName);

        String getApiVersion();

        int getEmvMessageType();

        String getEmvMessageString();

        int getTfpMessageType();

        String getTfpMessageString();

        void auto(OnSuccessFunction<String> success, OnFailureFunction failure);

        void restart(OnSuccessVoidFunction success, OnFailureFunction failure);

        UsbDevice findUsbDevice();

        UsbDevice getUsbDevice();

    }
}
