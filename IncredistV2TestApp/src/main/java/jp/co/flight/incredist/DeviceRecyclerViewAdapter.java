package jp.co.flight.incredist;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import jp.co.flight.incredist.android.IncredistV2TestApp.R;
import jp.co.flight.incredist.android.IncredistV2TestApp.databinding.FragmentDialogDeviceItemBinding;

/**
 * RecyclerView.Adapter
 */
public class DeviceRecyclerViewAdapter extends RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder> {

    interface Listener {
        void onClickDeviceItem(View view, String deviceName);
    }

    private final List<String> mDeviceList;
    private final Listener mListener;

    public DeviceRecyclerViewAdapter(@NonNull List<String> deviceList, @NonNull Listener listener) {
        mDeviceList = deviceList;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolder.newInstance(this, parent);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.bindData(mDeviceList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    public void onClickDeviceItem(View view, String deviceName) {
        mListener.onClickDeviceItem(view, deviceName);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final DeviceRecyclerViewAdapter mAdapter;
        private final FragmentDialogDeviceItemBinding mBinding;

        static ViewHolder newInstance(DeviceRecyclerViewAdapter adapter, ViewGroup parent) {
            FragmentDialogDeviceItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.fragment_dialog_device_item, parent, false);
            return new ViewHolder(adapter, binding);
        }

        ViewHolder(DeviceRecyclerViewAdapter adapter, FragmentDialogDeviceItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mAdapter = adapter;
        }

        void bindData(String deviceName) {
            mBinding.setDeviceName(deviceName);
            mBinding.setAdapter(mAdapter);
            mBinding.executePendingBindings();
        }
    }
}
