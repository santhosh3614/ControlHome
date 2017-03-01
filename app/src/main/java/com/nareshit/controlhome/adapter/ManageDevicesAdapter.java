package com.nareshit.controlhome.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nareshit.controlhome.R;
import com.nareshit.controlhome.utils.AppConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Santhosh on 2/24/2017.
 */

public class ManageDevicesAdapter extends RecyclerView.Adapter<ManageDevicesAdapter.ManageDeviceHolder> {

    private final List<String> list;

    public ManageDevicesAdapter(Context context) {
        SharedPreferences mPref = context.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        Set<String> devicesSet = mPref.getStringSet(AppConstants.ADDED_DEVICES, null);
        list = new ArrayList();
        if (devicesSet == null) {
            String[] deviceList = context.getResources().getStringArray(R.array.devices_list);
            devicesSet = new HashSet(Arrays.asList(deviceList));
            mPref.edit().putStringSet(AppConstants.ADDED_DEVICES, devicesSet).apply();
        }
        list.addAll(devicesSet);
        Collections.sort(list);
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

    public void addDevice(String deviceName) {
        list.add(deviceName);
        Collections.sort(list);
        notifyDataSetChanged();
    }

    class ManageDeviceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView addDeviceTextView;
        private final SwitchCompat deviceSwitch;

        public ManageDeviceHolder(View itemView) {
            super(itemView);
            addDeviceTextView = (TextView) itemView.findViewById(R.id.deviceTextView);
            deviceSwitch = (SwitchCompat) itemView.findViewById(R.id.deviceSwitch);
        }

        private void setDeviceInfo(String device) {
            addDeviceTextView.setText(device);
        }

        @Override
        public void onClick(View v) {
        }

    }

}
