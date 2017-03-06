package com.nareshit.controlhome.model;

import java.util.List;

/**
 * Created by Santhosh on 3/2/2017.
 */

public interface DeviceControlChangeListener {

    void onDeviceControlsChangeSuccess();

    void onDeviceControlsChangeFailed();

}
