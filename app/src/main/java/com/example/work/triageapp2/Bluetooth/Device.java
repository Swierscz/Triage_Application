package com.example.work.triageapp2.Bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.work.triageapp2.Bluetooth.Ble.BluetoothLeService;
import com.example.work.triageapp2.R;

/**
 * Created by BoryS on 05.09.2017.
 */

public class Device {
    private final static String TAG = Device.class.getSimpleName();
    public String deviceName;
    public String deviceAddress;
    public String deviceKind;
    boolean isFound = false;
    boolean isConnected = false;
    boolean isPaired = true;
    public Integer connectedImage;
    public Integer disconnectedImage;
    public Integer notFoundImage;
    public Integer nonPairedImage;

    public Device(String deviceName, String deviceAddress, String deviceKind){
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.deviceKind = deviceKind;
        notFoundImage = R.drawable.not_found_icon;
        connectedImage = R.mipmap.ok_icon;
        disconnectedImage = R.mipmap.no_icon;
        nonPairedImage = R.drawable.not_paired_icon;
    }


    public boolean isPaired() {
        return isPaired;
    }

    public void setPaired(boolean paired) {
        isPaired = paired;
    }

    public boolean isFound() {
        return isFound;
    }

    public void setFound(boolean found) {
        isFound = found;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

}
