package com.nareshit.controlhome;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.preference.MultiSelectListPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.nareshit.controlhome.adapter.AddDeviceAdapter;
import com.nareshit.controlhome.model.Device;
import com.nareshit.controlhome.model.DeviceControlChangeListener;
import com.nareshit.controlhome.model.DeviceStatusChangeListener;
import com.nareshit.controlhome.utils.AppConstants;
import com.nareshit.controlhome.utils.DeviceManager;
import com.nareshit.controlhome.utils.GoogleClient;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddDeviceActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private RecyclerView addDeviceRecyclerView;
    private AddDeviceAdapter addDeviceAdapter;
    private GoogleApiClient mGoogleApiClient;
    private DeviceManager deviceManager;
    private ProgressDialog progressDialog;
    private SharedPreferences mPref;
    private List<Device> deviceList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addDeviceFab);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mPref = getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        createDeviceList();
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Initializing....");
        progressDialog.setCancelable(false);

        mGoogleApiClient = GoogleClient.getInstance(this);
        deviceManager = new DeviceManager(mGoogleApiClient, null);
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.registerConnectionCallbacks(this);
            mGoogleApiClient.registerConnectionFailedListener(this);
        }

        addDeviceRecyclerView = (RecyclerView) findViewById(R.id.addDeviceRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        addDeviceRecyclerView.setLayoutManager(linearLayoutManager);
        addDeviceAdapter = new AddDeviceAdapter(this, fab, deviceList,deviceManager,progressDialog);
        addDeviceRecyclerView.setAdapter(addDeviceAdapter);
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(addDeviceRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(this, R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        addDeviceRecyclerView.addItemDecoration(horizontalDecoration);
        fab.setVisibility(addDeviceAdapter.getItemCount() < 5 ? View.VISIBLE : View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final EditText editText = new EditText(AddDeviceActivity.this);
                new AlertDialog.Builder(AddDeviceActivity.this)
                        .setTitle(R.string.add_device)
                        .setView(editText).setPositiveButton(R.string.add_device, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addDeviceAdapter.addDevice(editText.getText().toString());
                        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
                            progressDialog.show();
                            deviceManager.setDeviceControl(deviceList, new DeviceControlChangeListener() {
                                @Override
                                public void onDeviceControlsChangeSuccess() {
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onDeviceControlsChangeFailed() {
                                    progressDialog.dismiss();
                                }
                            });
                        }
                        Snackbar.make(view, "New Device added", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }).show();
            }
        });
    }

    private void createDeviceList() {
        String devicesSet = mPref.getString(AppConstants.ADDED_DEVICES, null);
        List<String> list = new ArrayList(5);
        if (devicesSet == null) {
            String[] deviceList = getResources().getStringArray(R.array.devices_list);
            String devices = "";
            for (int i = 0; i < deviceList.length; i++) {
                devices += deviceList[i] + (i != deviceList.length - 1 ? "," : "");
            }
            mPref.edit().putString(AppConstants.ADDED_DEVICES, devices).apply();
            list.addAll(Arrays.asList(deviceList));
        } else {
            String[] devices = devicesSet.split(",");
            for (int i = 0; i < devices.length; i++) {
                String device = devices[i];
                list.add(device);
            }
        }
        for (String deviceName : list) {
            Device device = new Device();
            device.setDeviceName(deviceName);
            deviceList.add(device);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        progressDialog.show();
        deviceManager.getDeviceStatus(null, new DeviceStatusChangeListener() {
            @Override
            public void onDeviceStatusResultSuccess(List<Boolean> devicesStatus) {
                progressDialog.dismiss();
                if (devicesStatus != null && !devicesStatus.isEmpty()) {
                    for (int i = 0; i < devicesStatus.size(); i++) {
                        Device device = deviceList.get(i);
                        device.setStatus(devicesStatus.get(i).booleanValue());
                    }
                }
            }

            @Override
            public void onDeviceStatusResultFailed() {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
