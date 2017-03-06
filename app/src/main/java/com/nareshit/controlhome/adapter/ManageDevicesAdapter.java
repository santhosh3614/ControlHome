package com.nareshit.controlhome.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nareshit.controlhome.MainActivity;
import com.nareshit.controlhome.R;
import com.nareshit.controlhome.model.Device;
import com.nareshit.controlhome.model.DeviceControlChangeListener;
import com.nareshit.controlhome.utils.AppConstants;
import com.nareshit.controlhome.utils.DeviceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static android.content.ContentValues.TAG;

/**
 * Created by Santhosh on 2/24/2017.
 */
public class ManageDevicesAdapter extends RecyclerView.Adapter<ManageDevicesAdapter.ManageDeviceHolder> {

    private final List<Device> list = new ArrayList(5);
    private final DeviceManager deviceManager;
    private final DeviceControlChangeListener deviceControlChangeListener;
    private final ProgressDialog progressDialog;

    public ManageDevicesAdapter(Context context, DeviceManager deviceManager,
                                DeviceControlChangeListener deviceControlChangeListener,
                                ProgressDialog progressDialog) {
        this.deviceManager = deviceManager;
        this.deviceControlChangeListener = deviceControlChangeListener;
        this.progressDialog = progressDialog;
        SharedPreferences mPref = context.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        String devicesSet = mPref.getString(AppConstants.ADDED_DEVICES, null);
        if (devicesSet == null) {
            String[] deviceList = context.getResources().getStringArray(R.array.devices_list);
            String devices = "";
            for (int i = 0; i < deviceList.length; i++) {
                devices += deviceList[i] + (i != deviceList.length - 1 ? "," : "");
            }
            mPref.edit().putString(AppConstants.ADDED_DEVICES, devices).apply();
            list.addAll(getDevices(deviceList));
        } else {
            String[] devices = devicesSet.split(",");
            list.addAll(getDevices(devices));
        }
    }

    private List<Device> getDevices(String[] devicesSet) {
        List<Device> devices = new ArrayList();
        for (String deviceName : devicesSet) {
            Device device = new Device();
            device.setDeviceName(deviceName);
            devices.add(device);
        }
        return devices;
    }

    public List<Device> getList() {
        return list;
    }

    @Override
    public ManageDeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ManageDeviceHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.managedevice_layout_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ManageDeviceHolder holder, int position) {
        holder.setDeviceInfo(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ManageDeviceHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

        private final TextView addDeviceTextView;
        private final SwitchCompat deviceSwitch;

        public ManageDeviceHolder(View itemView) {
            super(itemView);
            addDeviceTextView = (TextView) itemView.findViewById(R.id.deviceTextView);
            deviceSwitch = (SwitchCompat) itemView.findViewById(R.id.deviceSwitch);
        }

        private void setDeviceInfo(Device device) {
            addDeviceTextView.setText(device.getDeviceName());
            deviceSwitch.setOnCheckedChangeListener(null);
            deviceSwitch.setChecked(device.isStatus());
            deviceSwitch.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(TAG, "onCheckChangecalled.......");
            Device device = list.get(getLayoutPosition());
            device.setStatus(isChecked);
            progressDialog.show();
            deviceManager.setDeviceControl(list, deviceControlChangeListener);
        }

    }

}
