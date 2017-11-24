package jp.co.flight.incredist;

import android.Manifest;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import jp.co.flight.incredist.android.IncredistV2TestApp.R;
import jp.co.flight.incredist.android.IncredistV2TestApp.databinding.FragmentMainBinding;
import jp.co.flight.incredist.model.IncredistModel;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * MainFragment.
 */
@RuntimePermissions
public class MainFragment extends Fragment implements DeviceListDialogFragment.Listener, DisplayMessageDialogFragment.Listener {

    private static final String DIALOG_TAG_SELECT_DEVICE = "dialog_tag_select_device";
    private static final String DIALOG_TAG_EMV_MESSAGE = "dialog_tag_emv_message";
    private static final String DIALOG_TAG_TFP_MESSAGE = "dialog_tag_tfp_message";
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_EMV_MESSAGE = 2;
    private static final int REQUEST_TFP_MESSAGE = 3;

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

    public interface OnFragmentInteractionListener {
        IncredistModel getModel();
    }
}
