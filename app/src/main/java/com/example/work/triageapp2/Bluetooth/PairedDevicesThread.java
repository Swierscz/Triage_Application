package com.example.work.triageapp2.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;

import com.example.work.triageapp2.Bluetooth.OtherBluetoothStuff.Device;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by BoryS on 06.09.2017.
 */

public class PairedDevicesThread extends Thread {
    private static final String DEVICE_LIST_KEY = "DEVICE_LIST_KEY" ;
    private final String TAG = this.getClass().getName();
    BluetoothAdapter mBluetoothAdapter;
    boolean isDone = false;


    ArrayList<Device> deviceList = new ArrayList<Device>();
    Bundle bundle = new Bundle();

    public PairedDevicesThread(BluetoothAdapter mBluetoothAdapter){
        this.mBluetoothAdapter = mBluetoothAdapter;
    }

    public void run(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        Log.i(TAG,"run function has started");
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                if(device.getType() == device.DEVICE_TYPE_CLASSIC)
                    deviceList.add(new Device(deviceName,deviceHardwareAddress,"CLASSIC"));
                else if(device.getType() == device.DEVICE_TYPE_DUAL)
                    deviceList.add(new Device(deviceName,deviceHardwareAddress,"DUAL"));
                else if(device.getType() == device.DEVICE_TYPE_LE)
                    deviceList.add(new Device(deviceName,deviceHardwareAddress,"LE"));
                else
                    deviceList.add(new Device(deviceName,deviceHardwareAddress,"UNKNOWN"));
            }

            setDone(true);
        }
        else{
            setDone(true);
        }
    }

    public ArrayList<Device> getDeviceList(){
        return deviceList;
    }
    public boolean isDone() {
        return isDone;
    }
    public void setDone(boolean done) {
        isDone = done;
    }

}
