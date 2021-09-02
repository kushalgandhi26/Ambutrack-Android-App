package com.example.ambutrack.Callback;

import com.example.ambutrack.ui.DriverGeoModel;

public interface IFirebaseDriverInfoListener {
    void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel);
}
