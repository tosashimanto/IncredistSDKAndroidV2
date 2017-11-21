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
public class MainFragment extends Fragment implements DeviceListDialogFragment.Listener {

    private static final String DIALOG_TAG_SELECT_DEVICE = "dialog_tag_select_device";
    private static final int REQUEST_SELECT_DEVICE = 1;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    public void startSelectDevice(ArrayList<String> devices) {
        DialogFragment dialog = DeviceListDialogFragment.newInstance(devices);
        dialog.setTargetFragment(this, REQUEST_SELECT_DEVICE);
        dialog.show(getFragmentManager(), DIALOG_TAG_SELECT_DEVICE);
    }

    @Override
    public void onSelectDevice(int requestCode, String deviceName) {
        mPresenter.setSelectedDevice(deviceName);
        mPresenter.addLog(deviceName);
    }

    public interface OnFragmentInteractionListener {
        IncredistModel getModel();
    }
}
