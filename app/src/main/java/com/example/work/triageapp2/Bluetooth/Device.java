package com.example.work.triageapp2.Bluetooth;

import com.example.work.triageapp2.R;

/**
 * Created by BoryS on 05.09.2017.
 */

public class Device {
    private final static String TAG = Device.class.getSimpleName();
    private String name;
    private String address;
    private String kind;
    private boolean isFound = false;
    private boolean isConnected = false;
    private boolean isPaired = true;
    public Integer connectedImage;
    public Integer disconnectedImage;
    public Integer notFoundImage;
    public Integer nonPairedImage;

    public Device(String name, String address, String kind){
        this.name = name;
        this.address = address;
        this.kind = kind;
        notFoundImage = R.drawable.not_found_icon;
        connectedImage = R.mipmap.ok_icon;
        disconnectedImage = R.mipmap.no_icon;
        nonPairedImage = R.drawable.not_paired_icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String deviceName) {
        this.name = deviceName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String deviceAddress) {
        this.address = deviceAddress;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String deviceKind) {
        this.kind = deviceKind;
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
