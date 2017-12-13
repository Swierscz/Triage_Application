package com.example.work.triageapp2.Bluetooth.OtherBluetoothStuff;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.work.triageapp2.R;

/**
 * Created by BoryS on 05.09.2017.
 */

public class Device {
    public String deviceName;
    public String deviceAddress;
    public String deviceKind;
    boolean isConnected = false;
    public Integer connectedImage;
    public Integer disconnectedImage;

    public Device(String deviceName, String deviceAddress, String deviceKind){
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.deviceKind = deviceKind;
        connectedImage = R.mipmap.ok_icon;
        disconnectedImage = R.mipmap.no_icon;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

}
