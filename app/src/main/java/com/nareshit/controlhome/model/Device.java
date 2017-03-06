package com.nareshit.controlhome.model;

/**
 * Created by Santhosh on 3/1/2017.
 */

public class Device {

    private String deviceName;
    private boolean status;

    public Device() {
    }

    public Device(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

}
