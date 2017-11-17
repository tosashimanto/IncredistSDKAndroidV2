package jp.co.flight.incredist;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;

import jp.co.flight.incredist.databinding.FragmentDialogDeviceListBinding;

public class DeviceListDialogFragment extends DialogFragment implements DeviceRecyclerViewAdapter.Listener {

    private static final String ARG_KEY_DEVICE_LIST = "arg_key_device_list";

    interface Listener {
        void onSelectDevice(int requestCode, String deviceName);
    }

    public DeviceListDialogFragment() {
    }

    public static DeviceListDialogFragment newInstance(ArrayList<String> deviceList) {
        DeviceListDialogFragment fragment = new DeviceListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(ARG_KEY_DEVICE_LIST, deviceList);
        fragment.setArguments(bundle);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        FragmentDialogDeviceListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_dialog_device_list, null, false);
        builder.setView(binding.getRoot());

        Bundle args = getArguments();
        ArrayList<String> deviceList = args.getStringArrayList(ARG_KEY_DEVICE_LIST);

        DeviceRecyclerViewAdapter adapter = new DeviceRecyclerViewAdapter(deviceList, this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));

        return builder.create();
    }

    @Override
    public void onClickDeviceItem(View view, String deviceName) {
        Fragment target = getTargetFragment();
        if (target != null && target instanceof Listener) {
            ((Listener) target).onSelectDevice(getTargetRequestCode(), deviceName);
        }
        dismiss();
    }
}
