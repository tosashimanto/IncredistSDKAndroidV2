package jp.co.flight.incredist.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.BaseObservable;
import android.hardware.usb.UsbDevice;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import jp.co.flight.android.bluetooth.le.BluetoothPeripheral;
import jp.co.flight.incredist.TestAppContract;
import jp.co.flight.incredist.android.Incredist;
import jp.co.flight.incredist.android.IncredistManager;
import jp.co.flight.incredist.android.IncredistV2TestApp.BR;
import jp.co.flight.incredist.android.OnFailureFunction;
import jp.co.flight.incredist.android.OnSuccessFunction;
import jp.co.flight.incredist.android.OnSuccessVoidFunction;
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
import jp.co.flight.incredist.android.model.PinEntry;
import jp.co.flight.incredist.android.model.ProductInfo;

public class IncredistModel extends BaseObservable implements TestAppContract.Model {
    private static final String PREFERENCE_KEY_DEVICE_NAME = "device_name";
    private static final String PREFERENCE_KEY_EMV_MESSAGE_TYPE = "emv_message_type";
    private static final String PREFERENCE_KEY_EMV_MESSAGE_STRING = "emv_message_string";
    private static final String PREFERENCE_KEY_TFP_MESSAGE_TYPE = "tfp_message_type";
    private static final String PREFERENCE_KEY_TFP_MESSAGE_STRING = "tfp_message_string";

    private final Context mContext;
    private IncredistManager mIncredistManager;
    private Incredist mIncredist;

    private List<BluetoothPeripheral> mDeviceList;
    private String mSelectedDevice;
    private int mEmvMessageType;
    private String mEmvMessageString;
    private int mTfpMessageType;
    private String mTfpMessageString;
    private String mSerialNumber;
    private UsbDevice mUsbDevice;

    public IncredistModel(Context context) {
        mContext = context;

        SharedPreferences sp = getSharedPreference();
        mSelectedDevice = sp.getString(PREFERENCE_KEY_DEVICE_NAME, "not selected");
        mEmvMessageType = sp.getInt(PREFERENCE_KEY_EMV_MESSAGE_TYPE, 0);
        mEmvMessageString = sp.getString(PREFERENCE_KEY_EMV_MESSAGE_STRING, null);
        mTfpMessageType = sp.getInt(PREFERENCE_KEY_TFP_MESSAGE_TYPE, 0);
        mTfpMessageString = sp.getString(PREFERENCE_KEY_TFP_MESSAGE_STRING, null);
    }

    //-- methods from Model.
    @Override
    public void newIncredistObject() {
        mIncredistManager = new IncredistManager(mContext);
    }

    @Override
    public void bleStartScan(OnSuccessFunction<List<BluetoothPeripheral>> success, OnFailureFunction failure) {
        mIncredistManager.bleStartScan(null, 5000, (deviceList) -> {
            mDeviceList = deviceList;
            success.onSuccess(deviceList);
        }, failure);
    }

    @Override
    public List<BluetoothPeripheral> getDeviceList() {
        return mDeviceList;
    }

    @Override
    public void connect(IncredistManager.IncredistConnectionListener listener) {
        mIncredistManager.connect(mSelectedDevice, 3000, 5000, new IncredistManager.IncredistConnectionListener() {
            @Override
            public void onConnectIncredist(Incredist incredist) {
                mIncredist = incredist;
                if (listener != null) {
                    listener.onConnectIncredist(incredist);
                }
            }

            @Override
            public void onConnectFailure(int errorCode) {
                mIncredist = null;
                if (listener != null) {
                    listener.onConnectFailure(errorCode);
                }
            }

            @Override
            public void onDisconnectIncredist(Incredist incredist) {
                mIncredist.release();
                mIncredist = null;
                if (listener != null) {
                    listener.onDisconnectIncredist(incredist);
                }
            }
        });
    }

    @Override
    public void connect(UsbDevice device, IncredistManager.IncredistConnectionListener listener) {
        mIncredistManager.connect(device, new IncredistManager.IncredistConnectionListener() {
            @Override
            public void onConnectIncredist(Incredist incredist) {
                mIncredist = incredist;
                if (listener != null) {
                    listener.onConnectIncredist(incredist);
                }
            }

            @Override
            public void onConnectFailure(int errorCode) {
                mIncredist = null;
                if (listener != null) {
                    listener.onConnectFailure(errorCode);
                }
            }

            @Override
            public void onDisconnectIncredist(Incredist incredist) {
                mIncredist = null;
                if (listener != null) {
                    listener.onDisconnectIncredist(incredist);
                }
            }
        });
    }

    @Override
    public void disconnect() {
        if (mIncredist != null) {
            mIncredist.disconnect();
        }
    }

    @Override
    public void getDeviceInfo(OnSuccessFunction<DeviceInfo> success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.getDeviceInfo(success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void getProductInfo(OnSuccessFunction<ProductInfo> success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.getProductInfo(success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void getBootloaderVersion(OnSuccessFunction<BootloaderVersion> success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.getBootloaderVersion(success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void felicaOpen(boolean withLed, OnSuccessVoidFunction success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.felicaOpen(withLed, success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void felicaLedColor(LedColor color, OnSuccessVoidFunction success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.felicaLedColor(color, success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void felicaSendCommand(OnSuccessFunction<FelicaCommandResult> success, OnFailureFunction failure) {
        if (mIncredist != null) {
            byte[] felicaCommand = {(byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00};
            mIncredist.felicaSendCommand(felicaCommand, 200, success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void felicaClose(OnSuccessVoidFunction success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.felicaClose(success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void emvDisplayMessage(int emvMessageType, String emvMessageString, OnSuccessVoidFunction success, OnFailureFunction failure) {
        if (mIncredist != null) {
            SharedPreferences sp = getSharedPreference();
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(PREFERENCE_KEY_EMV_MESSAGE_TYPE, emvMessageType);
            editor.putString(PREFERENCE_KEY_EMV_MESSAGE_STRING, emvMessageString);
            editor.apply();
            mEmvMessageType = emvMessageType;
            mEmvMessageString = emvMessageString;

            mIncredist.emvDisplayMessage(emvMessageType, emvMessageString, success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void tfpDisplayMessage(int tfpMessageType, String tfpMessageString, OnSuccessVoidFunction success, OnFailureFunction failure) {
        if (mIncredist != null) {
            SharedPreferences sp = getSharedPreference();
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(PREFERENCE_KEY_TFP_MESSAGE_TYPE, tfpMessageType);
            editor.putString(PREFERENCE_KEY_TFP_MESSAGE_STRING, tfpMessageString);
            editor.apply();
            mTfpMessageType = tfpMessageType;
            mTfpMessageString = tfpMessageString;

            mIncredist.tfpDisplayMessage(tfpMessageType, tfpMessageString, success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void setEncryptionMode(EncryptionMode mode, OnSuccessVoidFunction success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.setEncryptionMode(mode, success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void scanMagnetic(long timeout, OnSuccessFunction<DecodedMagCard> success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.scanMagneticCard(timeout, (magCard) -> {
                success.onSuccess(new DecodedMagCard(magCard));
            }, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void pinEntryD(PinEntryDParam setting, OnSuccessFunction<PinEntry.Result> success, OnFailureFunction failure) {
        if (mIncredist != null) {
            PinEntry.Mode pinMode = PinEntry.Mode.values()[setting.getPinMode()];
            int min = (pinMode == PinEntry.Mode.DebitScramble) ? setting.getLength() : 1;
            mIncredist.pinEntryD(PinEntry.Type.ISO9564, pinMode, PinEntry.MaskMode.values()[setting.getMaskMode()],
                    min, setting.getLength(), PinEntry.Alignment.values()[setting.getDirection()], setting.getPosition(), 30000, success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void pinEntryI( OnSuccessFunction<PinEntry.Result> success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.pinEntryI(PinEntry.Type.ISO9564, success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void setLedColor(LedColor color, boolean isOn, OnSuccessVoidFunction success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.setLedColor(color, isOn, success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void scanCreditCard(EnumSet<CreditCardType> cardTypeSet, long amount, EmvTagType tagType,
                               int aidSetting, EmvTransactionType transactionType, boolean fallback, long timeout,
                               OnSuccessFunction<EmvPacket> emvSuccess,
                               OnSuccessFunction<MagCard> magSuccess,
                               OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.scanCreditCard(cardTypeSet, amount, tagType, aidSetting, transactionType, fallback, timeout, emvSuccess, magSuccess, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void checkCardStatus(OnSuccessFunction<ICCardStatus> success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.emvCheckCardStatus(success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void rtcGetTime(OnSuccessFunction<Calendar> success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.rtcGetTime(success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void rtcSetTime(Calendar cal, OnSuccessVoidFunction success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.rtcSetTime(cal, success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void rtcSetCurrentTime(OnSuccessVoidFunction success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.rtcSetCurrentTime(success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void emoneyBlink(boolean isBlink, LedColor color, int duration, OnSuccessFunction<Boolean> success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.emoneyBlink(isBlink, color, duration, success, failure);
        } else {
            failure.onFailure(-1);
        }
    }


    @Override
    public void stop(OnSuccessVoidFunction success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.stop(success, failure);
        } else {
            failure.onFailure(-1);
        }
    }

    @Override
    public void cancel(OnSuccessVoidFunction success, OnFailureFunction failure) {
        if (mIncredist != null) {
            mIncredist.cancel(success, failure);
        } else {
            failure.onFailure(-1);
        }
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
    public int getEmvMessageType() {
        return mEmvMessageType;
    }

    @Override
    public String getEmvMessageString() {
        return mEmvMessageString;
    }

    @Override
    public int getTfpMessageType() {
        return mTfpMessageType;
    }

    @Override
    public String getTfpMessageString() {
        return mTfpMessageString;
    }

    @Override
    public void auto(OnSuccessFunction<String> success, OnFailureFunction failure) {
        mIncredistManager.connect(mSelectedDevice, 3000, 5000, new IncredistManager.IncredistConnectionListener() {
            @Override
            public void onConnectIncredist(Incredist incredist) {
                incredist.getSerialNumber((serialNumberResult) -> {
                    mSerialNumber = serialNumberResult;
                    incredist.disconnect();
                }, (errorCode) -> {
                    incredist.disconnect();

                    if (failure != null) {
                        failure.onFailure(errorCode);
                    }
                });
            }

            @Override
            public void onConnectFailure(int errorCode) {
                if (failure != null) {
                    failure.onFailure(errorCode);
                }
            }

            @Override
            public void onDisconnectIncredist(Incredist incredist) {
                incredist.release();
                mIncredist = null;
                if (success != null) {
                    success.onSuccess(mSerialNumber);
                }
            }
        });
    }

    @Override
    public void restart(OnSuccessVoidFunction success, OnFailureFunction failure) {
        mIncredistManager.restartAdapter(success, failure);
        mIncredist = null;
    }

    @Override
    public UsbDevice findUsbDevice() {
        mUsbDevice = mIncredistManager.findUsbIncredist();
        return mUsbDevice;
    }

    @Override
    public UsbDevice getUsbDevice() {
        return mUsbDevice;
    }

    private SharedPreferences getSharedPreference() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }
}
