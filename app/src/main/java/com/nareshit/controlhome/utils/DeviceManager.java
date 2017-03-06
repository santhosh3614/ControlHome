package com.nareshit.controlhome.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.nareshit.controlhome.MainActivity;
import com.nareshit.controlhome.model.Device;
import com.nareshit.controlhome.model.DeviceControlChangeListener;
import com.nareshit.controlhome.model.DeviceStatusChangeListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Santhosh on 3/1/2017.
 */
public class DeviceManager implements ChangeListener {

    private static final String TAG = DeviceManager.class.getSimpleName();
    private static final String DEVICE_STATUS_FILE_NAME = "DeviceStatus.txt";
    private static final String DEVICE_CONTROL_FILE_NAME = "DeviceControl.txt";
    private static final short START_WORD = 0x5555;
    private static final short DEVICE_ON = 0x1234;
    private static final short DEVICE_OFF = 0x4321;

    private final GoogleApiClient mGoogleApiClient;
    private final DeviceControlChangeListener mDeviceControlChangeListener;
    private DriveId mDriveStatusFileId;
    private DriveId mDriveControlFileId;
    private PendingResult<DriveApi.DriveContentsResult> driveControlContentResult;
    private DeviceStatusChangeListener deviceStatusChangeListener;
    private DriveFile mDriveStatusFile;

    public DeviceManager(GoogleApiClient mGoogleApiClient, DeviceControlChangeListener mDeviceControlChangeListener) {
        this.mGoogleApiClient = mGoogleApiClient;
        this.mDeviceControlChangeListener = mDeviceControlChangeListener;
    }

    private void createDeviceStatusFile(final List<Device> devices, final DeviceStatusChangeListener mDeviceStatusChangeListener) {
        Log.i(TAG, "Creating new contents.");
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(final DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new IOT file contents.");
                            return;
                        }
                        // Otherwise, we can write our data to the new contents.
                        Log.i(TAG, "New IOT created.");
                        DriveContents driveContents = result.getDriveContents();
                        setDeviceControlData(driveContents, devices);
                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("text/plain")
                                .setTitle(DEVICE_STATUS_FILE_NAME).build();
                        // Create a file in the root folder
                        Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                .createFile(mGoogleApiClient, metadataChangeSet, result.getDriveContents())
                                .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                    @Override
                                    public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                                        Log.d(TAG, "result..." + driveFileResult.toString());
                                        mDriveStatusFileId = driveFileResult.getDriveFile().getDriveId();
                                        mDriveStatusFile = mDriveStatusFileId.asDriveFile();
                                        if (driveFileResult.getStatus().isSuccess()) {
                                            mDeviceStatusChangeListener.onDeviceStatusResultSuccess(null);
                                        } else {
                                            mDeviceStatusChangeListener.onDeviceStatusResultFailed();
                                        }
                                    }
                                });
                    }
                });
    }

    private void createDeviceControlFile(final List<Device> devices, final DeviceControlChangeListener mDeviceControlChangeListener) {
        Log.i(TAG, "Creating new contents.");
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(final DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new IOT file contents.");
                            return;
                        }
                        // Otherwise, we can write our data to the new contents.
                        Log.i(TAG, "New IOT created.");
                        DriveContents driveContents = result.getDriveContents();
                        setDeviceControlData(driveContents, devices);
                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("text/plain")
                                .setTitle(DEVICE_CONTROL_FILE_NAME).build();
                        // Create a file in the root folder
                        Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                .createFile(mGoogleApiClient, metadataChangeSet, result.getDriveContents())
                                .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                    @Override
                                    public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                                        Log.d(TAG, "result..." + driveFileResult.toString());
                                        mDriveControlFileId = driveFileResult.getDriveFile().getDriveId();
                                        if (driveFileResult.getStatus().isSuccess()) {
                                            mDeviceControlChangeListener.onDeviceControlsChangeSuccess();
                                        } else {
                                            mDeviceControlChangeListener.onDeviceControlsChangeFailed();
                                        }
                                    }
                                });
                    }
                });
    }

    private void searchDeviceControlFile(final List<Device> devices, final DeviceControlChangeListener mDeviceControlChangeListener) {
        Log.d(TAG, "searchDeviceControlFile......called");
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                .addFilter(Filters.eq(SearchableField.TITLE, DEVICE_CONTROL_FILE_NAME))
                .build();
        Drive.DriveApi.query(mGoogleApiClient, query)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult result) {
                        Log.d(TAG, "onResult......" + result.getStatus().isSuccess());
                        int count = 0;
                        if (result.getStatus().isSuccess()) {
                            Iterator<Metadata> iterator = result.getMetadataBuffer().iterator();
                            while (iterator.hasNext()) {
                                count++;
                                Metadata next = iterator.next();
                                mDriveControlFileId = next.getDriveId();
                                Log.d(TAG, "mDriveControlFileId:" + mDriveControlFileId);
                            }
                            if (count == 0) {
                                createDeviceControlFile(devices, mDeviceControlChangeListener);
                            } else {
                                setDeviceControl(devices, mDeviceControlChangeListener);
                            }
                        } else {
                            createDeviceControlFile(devices, mDeviceControlChangeListener);
                        }
                    }
                });
    }

    private void searchDeviceStatusFile(final List<Device> devices, final DeviceStatusChangeListener deviceStatusChangeListener) {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                .addFilter(Filters.eq(SearchableField.TITLE, DEVICE_STATUS_FILE_NAME))
                .build();
        Drive.DriveApi.query(mGoogleApiClient, query)
                .setResultCallback(
                        new ResultCallback<DriveApi.MetadataBufferResult>() {
                            @Override
                            public void onResult(DriveApi.MetadataBufferResult result) {
                                Log.d(TAG, "onResult......" + result.getStatus().isSuccess());
                                int count = 0;
                                if (result.getStatus().isSuccess()) {
                                    Iterator<Metadata> iterator = result.getMetadataBuffer().iterator();
                                    while (iterator.hasNext()) {
                                        count++;
                                        Metadata next = iterator.next();
                                        mDriveStatusFileId = next.getDriveId();
                                        mDriveStatusFile = mDriveStatusFileId.asDriveFile();
                                        registerDeviceStatusFile(mDriveStatusFile);
                                        Log.d(TAG, "mDriveStatusFileId:" + mDriveStatusFileId);
                                    }
                                    if (count != 0) {
                                        getDeviceStatus(devices, deviceStatusChangeListener);
                                    } else {
                                        createDeviceStatusFile(devices, deviceStatusChangeListener);
                                    }
                                }
                            }
                        });
    }

    private void registerDeviceStatusFile(DriveFile driveFile) {
        driveFile.addChangeListener(mGoogleApiClient, this);
    }

    public void getDeviceStatus(final List<Device> defaultDevices, final DeviceStatusChangeListener deviceStatusChangeListener) {
        if (mDriveStatusFileId == null) {
            searchDeviceStatusFile(defaultDevices, deviceStatusChangeListener);
            return;
        }
        mDriveStatusFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (result.getStatus().isSuccess()) {
                            DriveContents contents = result.getDriveContents();
                            List<Boolean> deviceStatusData = getDeviceStatusData(contents);
                            deviceStatusChangeListener.onDeviceStatusResultSuccess(deviceStatusData);
                        } else {
                            deviceStatusChangeListener.onDeviceStatusResultFailed();
                        }
                    }
                });
        registerDeviceStatusFile(mDriveStatusFile);
    }

    public void setDeviceControl(final List<Device> devices, final DeviceControlChangeListener mDeviceControlChangeListener) {
        Log.d(TAG, "setDeviceControl.......called");
        if (mDriveControlFileId == null) {
            searchDeviceControlFile(devices, mDeviceControlChangeListener);
        } else {
            DriveFile file = mDriveControlFileId.asDriveFile();
            if (driveControlContentResult != null) {
                driveControlContentResult.cancel();
            }
            driveControlContentResult = file.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null);
            driveControlContentResult.setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                    DriveContents contents = result.getDriveContents();
                    setDeviceControlData(contents, devices);
                    contents.commit(mGoogleApiClient, null).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status result) {
                            Log.d(TAG, "onResult..." + result);
                            if (result.isSuccess()) {
                                mDeviceControlChangeListener.onDeviceControlsChangeSuccess();
                            } else {
                                mDeviceControlChangeListener.onDeviceControlsChangeFailed();
                            }
                        }
                    });
                }
            });
        }
    }

    private List<Boolean> getDeviceStatusData(DriveContents contents) {
        List<Boolean> devicesStatus = new ArrayList(5);
        InputStream inputStream = null;
        DataInputStream reader = null;
        try {
            inputStream = contents.getInputStream();
            reader = new DataInputStream(inputStream);
            short startWord = reader.readShort();
            if (startWord == START_WORD) {
                short deviceCount = reader.readShort();
                short checkSum = 0;
                for (int i = 0; i < 5; i++) {
                    short deviceStatus = reader.readShort();
                    if (i < deviceCount) {
                        if (deviceStatus == DEVICE_ON) {
                            devicesStatus.add(true);
                            checkSum += DEVICE_ON;
                        } else if (deviceStatus == DEVICE_OFF) {
                            devicesStatus.add(false);
                            checkSum += DEVICE_OFF;
                        }
                    }
                }
                short originalCheckSum = reader.readShort();
                if (originalCheckSum == checkSum) {
                    return devicesStatus;
                } else {
                    return null;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return devicesStatus;
    }

    private void setDeviceControlData(DriveContents contents, List<Device> devices) {
        Log.d(TAG, "setDeviceControlData.....device_size:" + devices.size());
        try {
            OutputStream outputStream = contents.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeShort(START_WORD);
            short deviceCount = (devices == null) ? 0x0000 : (short) devices.size();
            dataOutputStream.writeShort(deviceCount);
            short checkSum = 0;
            for (int i = 0; i < 5; i++) {
                if (devices != null && i < devices.size()) {
                    Device device = devices.get(i);
                    if (device.isStatus()) {
                        dataOutputStream.writeShort(DEVICE_ON);
                        checkSum += DEVICE_ON;
                    } else {
                        dataOutputStream.writeShort(DEVICE_OFF);
                        checkSum += DEVICE_OFF;
                    }
                } else {
                    dataOutputStream.writeShort(0x0000);
                }
            }
            dataOutputStream.writeShort(checkSum);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }

    public void registerStatusChangeListener(DeviceStatusChangeListener deviceStatusChangeListener) {
        this.deviceStatusChangeListener = deviceStatusChangeListener;
    }

    public void unregisterStatusChangeListener(DeviceStatusChangeListener deviceStatusChangeListener) {
        DriveFile file = mDriveStatusFile;
        if (mGoogleApiClient.isConnected()) {
            file.removeChangeListener(mGoogleApiClient, this);
        }
        deviceStatusChangeListener = null;
    }

    @Override
    public void onChange(ChangeEvent changeEvent) {
        Log.d(TAG, "onChange.....called");
//        getDeviceStatus(null, deviceStatusChangeListener);
    }


}
