package com.nareshit.controlhome;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.nareshit.controlhome.adapter.ManageDevicesAdapter;
import com.nareshit.controlhome.model.Device;
import com.nareshit.controlhome.model.DeviceControlChangeListener;
import com.nareshit.controlhome.model.DeviceStatusChangeListener;
import com.nareshit.controlhome.utils.DeviceManager;
import com.nareshit.controlhome.utils.GoogleClient;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DeviceControlChangeListener, DeviceStatusChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 123;
    private RecyclerView recyclerView;
    private ManageDevicesAdapter manageDevicesAdapter;
    private ProgressDialog progressDialog;
    private DeviceManager deviceManager;
    private GoogleApiClient mGoogleApiClient;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Log.d(TAG, "onCreate...........");
        handler = new Handler();
        mGoogleApiClient = GoogleClient.getInstance(this);
        mGoogleApiClient.registerConnectionCallbacks(this);
        mGoogleApiClient.registerConnectionFailedListener(this);

        deviceManager = new DeviceManager(mGoogleApiClient, this);
//        deviceManager.registerStatusChangeListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Initializing....");
        progressDialog.setCancelable(false);
        progressDialog.show();

        manageDevicesAdapter = new ManageDevicesAdapter(this, deviceManager, this, progressDialog);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(manageDevicesAdapter);
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(this, R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        recyclerView.addItemDecoration(horizontalDecoration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            progressDialog.show();
            handler.removeCallbacks(mRunnable);
            deviceManager.getDeviceStatus(manageDevicesAdapter.getList(), this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected...........");
        deviceManager.getDeviceStatus(manageDevicesAdapter.getList(), this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed......connectionResult.hasResolution()" + connectionResult.toString());
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                Log.d(TAG, "onConnectionFailed......SendIntentException" + e.getMessage());
                // Unable to resolve, message user appropriately
                e.printStackTrace();
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        handler.removeCallbacks(mRunnable);
//        deviceManager.unregisterStatusChangeListener(this);
        mGoogleApiClient.unregisterConnectionCallbacks(this);
        mGoogleApiClient.unregisterConnectionFailedListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult......Request" + requestCode + " ResultCode:" + resultCode + " data:" + data);
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            deviceManager.getDeviceStatus(manageDevicesAdapter.getList(), MainActivity.this);
        }
    };

    @Override
    public void onDeviceControlsChangeSuccess() {
        progressDialog.hide();
        Toast.makeText(this, "May take some time to update in IOT device", Toast.LENGTH_SHORT).show();
        handler.removeCallbacks(mRunnable);
        handler.postDelayed(mRunnable, 60000);
    }

    @Override
    public void onDeviceControlsChangeFailed() {
        progressDialog.hide();
    }

    @Override
    public void onDeviceStatusResultSuccess(List<Boolean> devicesStatus) {
        Log.d(TAG, "onDeviceStatusResultSuccess......called");
        if (devicesStatus != null && !devicesStatus.isEmpty()) {
            List<Device> list = manageDevicesAdapter.getList();
            for (int i = 0; i < devicesStatus.size(); i++) {
                Boolean status = devicesStatus.get(i);
                list.get(i).setStatus(status);
            }
            manageDevicesAdapter.notifyDataSetChanged();
        }
        progressDialog.dismiss();
    }

    @Override
    public void onDeviceStatusResultFailed() {
        progressDialog.hide();
    }

}
