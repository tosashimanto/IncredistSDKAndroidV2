package jp.co.flight.incredist;

import android.Manifest;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jp.co.flight.incredist.android.IncredistV2TestApp.R;
import jp.co.flight.incredist.android.IncredistV2TestApp.databinding.FragmentMainBinding;
import jp.co.flight.incredist.android.model.CreditCardType;
import jp.co.flight.incredist.android.model.EmvTagType;
import jp.co.flight.incredist.android.model.EmvTransactionType;
import jp.co.flight.incredist.android.model.EncryptionMode;
import jp.co.flight.incredist.android.model.LedColor;
import jp.co.flight.incredist.model.IncredistModel;
import jp.co.flight.incredist.model.PinEntryDParam;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * MainFragment.
 */
@RuntimePermissions
public class MainFragment extends Fragment
        implements DeviceListDialogFragment.Listener, DisplayMessageDialogFragment.Listener,
        EncryptionSettingDialogFragment.Listener, PinEntryDParamDialogFragment.Listener,
        LedColorDialogFragment.Listener, DateTimeDialogFragment.Listener,
        CreditPaymentSettingDialogFragment.Listener, EmoneyBlinkDialogFragment.Listener {

    private static final String DIALOG_TAG_SELECT_DEVICE = "dialog_tag_select_device";
    private static final String DIALOG_TAG_EMV_MESSAGE = "dialog_tag_emv_message";
    private static final String DIALOG_TAG_TFP_MESSAGE = "dialog_tag_tfp_message";
    private static final String DIALOG_TAG_ENCRYPTION_SETTING = "dialog_tag_encryption_setting";
    private static final String DIALOG_TAG_PIN_D_PARAM = "dialog_tag_pin_d_setting";
    private static final String DIALOG_TAG_SET_LED_COLOR = "dialog_tag_set_led_color";
    private static final String DIALOG_TAG_FELICA_LED_COLOR = "dialog_tag_felica_led_color";
    private static final String DIALOG_TAG_DATETIME = "dialog_tag_datetime";
    private static final String DIALOG_TAG_CREDIT_SETTING = "dialog_tag_credit_setting";
    private static final String DIALOG_TAG_EMONEY_BLINK = "dialog_tag_emoney_blink";

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_EMV_MESSAGE = 2;
    private static final int REQUEST_TFP_MESSAGE = 3;
    private static final int REQUEST_ENCRYPTION = 4;
    private static final int REQUEST_PIN_D_PARAM = 5;
    private static final int REQUEST_SET_LED_COLOR = 6;
    private static final int REQUEST_FELICA_LED_COLOR = 7;
    private static final int REQUEST_DATETIME = 8;
    private static final int REQUEST_CREDIT_SETTING = 9;
    private static final int REQUEST_EMONEY_BLINK = 10;

    private OnFragmentInteractionListener mListener;
    private FragmentMainBinding mBinding;
    private IncredistModel mModel;
    private MainPresenter.Impl mPresenter;

    public MainFragment() {
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);

        mBinding.textLog.setMovementMethod(new ScrollingMovementMethod());

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mModel = mListener.getModel();
        mPresenter = new MainPresenter.Impl(this, mBinding, mModel);

        mBinding.setIncredist(mModel);
        mBinding.setPresenter(mPresenter);

        MainFragmentPermissionsDispatcher.showPermissionStubWithPermissionCheck(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    public void showPermissionStub() {
        // do nothing.
    }

    void showDeviceListDialog(ArrayList<String> devices) {
        DialogFragment dialog = DeviceListDialogFragment.newInstance(devices);
        dialog.setTargetFragment(this, REQUEST_SELECT_DEVICE);
        dialog.show(getFragmentManager(), DIALOG_TAG_SELECT_DEVICE);
    }

    @Override
    public void onSelectDevice(int requestCode, String deviceName) {
        mPresenter.setSelectedDevice(deviceName);
        mPresenter.addLog(deviceName);
    }

    void showEmvDisplayMessageDialog() {
        DialogFragment dialog = DisplayMessageDialogFragment.newInstance("EMV message",
                mModel.getEmvMessageType(), mModel.getEmvMessageString());
        dialog.setTargetFragment(this, REQUEST_EMV_MESSAGE);
        dialog.show(getFragmentManager(), DIALOG_TAG_EMV_MESSAGE);
    }

    void showTfpDisplayMessageDialog() {
        DialogFragment dialog = DisplayMessageDialogFragment.newInstance("TFP message",
                mModel.getTfpMessageType(), mModel.getTfpMessageString());
        dialog.setTargetFragment(this, REQUEST_TFP_MESSAGE);
        dialog.show(getFragmentManager(), DIALOG_TAG_TFP_MESSAGE);
    }

    @Override
    public void onDisplayMessage(int requestCode, int type, String message) {
        switch (requestCode) {
            case REQUEST_EMV_MESSAGE:
                mPresenter.emvDisplayMessage(type, message);
                break;

            case REQUEST_TFP_MESSAGE:
                mPresenter.tfpDisplayMessage(type, message);
                break;

            default:
                break;
        }
    }

    public void showEncryptSettingDialog() {
        DialogFragment dialog = EncryptionSettingDialogFragment.newInstance(null);
        dialog.setTargetFragment(this, REQUEST_ENCRYPTION);
        dialog.show(getFragmentManager(), DIALOG_TAG_ENCRYPTION_SETTING);
    }

    @Override
    public void onSetEncryptionSetting(int requestCode, EncryptionMode mode) {
        mPresenter.setEncryptionMode(mode);
    }

    public void showPinEntryDParamDialog() {
        DialogFragment dialog = PinEntryDParamDialogFragment.newInstance(null);
        dialog.setTargetFragment(this, REQUEST_PIN_D_PARAM);
        dialog.show(getFragmentManager(), DIALOG_TAG_PIN_D_PARAM);
    }

    @Override
    public void onSetPinEntryDParam(int requestCode, PinEntryDParam param) {
        mPresenter.pinEntryD(param);
    }

    public void showSetLedColorDialog() {
        DialogFragment dialog = LedColorDialogFragment.newInstance(true);
        dialog.setTargetFragment(this, REQUEST_SET_LED_COLOR);
        dialog.show(getFragmentManager(), DIALOG_TAG_SET_LED_COLOR);
    }

    public void showFelicaLedColorDialog() {
        DialogFragment dialog = LedColorDialogFragment.newInstance(false);
        dialog.setTargetFragment(this, REQUEST_FELICA_LED_COLOR);
        dialog.show(getFragmentManager(), DIALOG_TAG_FELICA_LED_COLOR);
    }

    @Override
    public void onSetLedColor(int requestCode, LedColor color, boolean isOn) {
        if (requestCode == REQUEST_SET_LED_COLOR) {
            mPresenter.setLedColor(color, isOn);
        } else if (requestCode == REQUEST_FELICA_LED_COLOR) {
            mPresenter.felicaLedColor(color);
        }
    }

    public void showDateTimeDialog() {
        DialogFragment dialog = DateTimeDialogFragment.newInstance();
        dialog.setTargetFragment(this, REQUEST_DATETIME);
        dialog.show(getFragmentManager(), DIALOG_TAG_DATETIME);
    }

    @Override
    public void onSetDateTime(int requestCode, Calendar cal) {
        mPresenter.setDateTime(cal);
    }

    public void showCreditSettingDialog() {
        DialogFragment dialog = CreditPaymentSettingDialogFragment.newInstance();
        dialog.setTargetFragment(this, REQUEST_CREDIT_SETTING);
        dialog.show(getFragmentManager(), DIALOG_TAG_CREDIT_SETTING);
    }

    @Override
    public void onSetCreditPaymentSetting(int requestCode, EnumSet<CreditCardType> cardTypeSet, long amount, EmvTagType tagType, int aidSetting, EmvTransactionType transactionType, boolean fallback, long timeout) {
        mPresenter.scanCreditCard(cardTypeSet, amount, tagType, aidSetting, transactionType, fallback, timeout);
    }

    public void showEmoneyBlinkDialog() {
        DialogFragment dialog = EmoneyBlinkDialogFragment.newInstance();
        dialog.setTargetFragment(this, REQUEST_EMONEY_BLINK);
        dialog.show(getFragmentManager(), DIALOG_TAG_EMONEY_BLINK);
    }

    @Override
    public void onSetEmoneyBlink(int requestCode, boolean isBlink, LedColor color, int duration) {
        mPresenter.emoneyBlink(isBlink, color, duration);
    }

    public void startUsb() {
        Context context = getContext();
        if (context != null) {
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

            mPresenter.addLog(String.format(Locale.US, "usb devicelist:%d", deviceList.size()));
            for (Map.Entry<String, UsbDevice> entry : deviceList.entrySet()) {
                UsbDevice device = entry.getValue();
                mPresenter.addLog(String.format(Locale.US, "key:%s vid:%x pid:%x name:%s", entry.getKey(), device.getVendorId(), device.getProductId(), device.getProductName()));
            }

        }
    }

    public interface OnFragmentInteractionListener {
        IncredistModel getModel();
    }
}
