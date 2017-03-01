package com.nareshit.controlhome.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nareshit.controlhome.AddDeviceActivity;
import com.nareshit.controlhome.R;
import com.nareshit.controlhome.utils.AppConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.android.gms.internal.zzt.TAG;

/**
 * Created by Santhosh on 2/24/2017.
 */
public class AddDeviceAdapter extends RecyclerView.Adapter<AddDeviceAdapter.AddDeviceHolder> {

    private final String TAG = AddDeviceAdapter.class.getSimpleName();
    private final List<String> list;
    private final SharedPreferences mPref;
    private final List<String> deleteDevicesList;
    private final Toolbar toolBar;
    private final Context context;
    private boolean isDeleteModeEnable;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            toolBar.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((Activity) context).getWindow().setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            }
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    if (list.size() != deleteDevicesList.size()) {
                        new AlertDialog.Builder(context)
                                .setMessage("Are you sure.you want to delete Devices")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        mode.finish();
                                        list.removeAll(deleteDevicesList);
                                        mPref.edit().putStringSet(AppConstants.ADDED_DEVICES, new HashSet<String>(list)).apply();
                                        deleteDevicesList.clear();
                                        isDeleteModeEnable = false;
                                        notifyDataSetChanged();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .show();
                    } else {
                        Toast.makeText(context, "Cannot delete all items", Toast.LENGTH_LONG).show();
                    }
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isDeleteModeEnable = false;
            notifyDataSetChanged();
            toolBar.setVisibility(View.VISIBLE);
        }
    };

    public AddDeviceAdapter(Context context) {
        this.context = context;
        toolBar = (Toolbar) ((AddDeviceActivity) context).findViewById(R.id.toolbar);
        mPref = context.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        Set<String> devicesSet = mPref.getStringSet(AppConstants.ADDED_DEVICES, null);
        list = new ArrayList();
        deleteDevicesList = new ArrayList();
        if (devicesSet == null) {
            String[] deviceList = context.getResources().getStringArray(R.array.devices_list);
            devicesSet = new HashSet(Arrays.asList(deviceList));
            mPref.edit().putStringSet(AppConstants.ADDED_DEVICES, devicesSet).apply();
        }
        list.addAll(devicesSet);
        Collections.sort(list);
    }

    @Override
    public AddDeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AddDeviceHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adddevice_layout_item, parent, false));
    }

    @Override
    public void onBindViewHolder(AddDeviceHolder holder, int position) {
        holder.setDeviceInfo(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addDevice(String deviceName) {
        list.add(deviceName);
        Set<String> devicesSet = mPref.getStringSet(AppConstants.ADDED_DEVICES, null);
        devicesSet.add(deviceName);
        mPref.edit().putStringSet(AppConstants.ADDED_DEVICES, devicesSet).apply();
        Collections.sort(list);
        notifyDataSetChanged();
    }

    class AddDeviceHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener,
            CompoundButton.OnCheckedChangeListener {

        private final TextView addDeviceTextView;
        private final CheckBox deviceCheckBox;

        public AddDeviceHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            itemView.setOnLongClickListener(this);
            addDeviceTextView = (TextView) itemView.findViewById(R.id.addDeviceTextView);
            deviceCheckBox = (CheckBox) itemView.findViewById(R.id.deviceCheckBox);
            deviceCheckBox.setOnCheckedChangeListener(this);
        }

        private void setDeviceInfo(String device) {
            addDeviceTextView.setText(device);
            if (isDeleteModeEnable) {
                deviceCheckBox.setVisibility(View.VISIBLE);
                deviceCheckBox.setOnCheckedChangeListener(null);
                deviceCheckBox.setChecked(deleteDevicesList.contains(device));
                deviceCheckBox.setOnCheckedChangeListener(this);
            } else {
                deviceCheckBox.setVisibility(View.GONE);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            isDeleteModeEnable = true;
            ((AppCompatActivity) v.getContext())
                    .startSupportActionMode(mActionModeCallback);
            String device = list.get(getLayoutPosition());
            if (!deleteDevicesList.contains(device)) {
                deleteDevicesList.add(device);
            }
            mActionMode.setTitle(deleteDevicesList.size() + " selected");
            Log.d(TAG, "DeletedList..." + deleteDevicesList.size());
            notifyDataSetChanged();
            return true;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String device = list.get(getLayoutPosition());
            if (!deleteDevicesList.contains(device)) {
                deleteDevicesList.add(device);
            } else {
                deleteDevicesList.remove(device);
            }
            mActionMode.setTitle(deleteDevicesList.size() + " selected");
        }
    }

}
