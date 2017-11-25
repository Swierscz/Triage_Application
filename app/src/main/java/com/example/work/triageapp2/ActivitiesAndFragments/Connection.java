package com.example.work.triageapp2.ActivitiesAndFragments;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.work.triageapp2.Bluetooth.Ble.BluetoothLeService;
import com.example.work.triageapp2.Bluetooth.Classic.ClassicConnection;
import com.example.work.triageapp2.Bluetooth.OtherBluetoothStuff.Device;
import com.example.work.triageapp2.Bluetooth.PairedDevicesThread;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by BoryS on 23.11.2017.
 */

public class Connection {
    MainActivity mainActivity;
    BluetoothAdapter mBluetoothAdapter;

    public ArrayList<Device> listOfPairedDevices = new ArrayList<Device>();
    public ArrayList<Device> listOfConnectedDevices = new ArrayList<Device>();

    public Connection(MainActivity mainActivity, BluetoothAdapter mBluetoothAdapter){
        this.mainActivity = mainActivity;
        this.mBluetoothAdapter = mBluetoothAdapter;

        listOfPairedDevices = fillAndReturnPairedDeviceList();
        if(listOfPairedDevices!= null){
            connectToSoldierDevices();
        }

    }

    public ArrayList<Device> fillAndReturnPairedDeviceList(){
        Log.i(TAG,"fillAndReturnPairedDeviceList function has started");
        ArrayList<Device> tempArrayList = new ArrayList<Device>();
        PairedDevicesThread pairedDevicesThread;
        pairedDevicesThread = new PairedDevicesThread(mBluetoothAdapter);
        pairedDevicesThread.start();

        while(!pairedDevicesThread.isDone()) {
            tempArrayList = pairedDevicesThread.getDeviceList();
        }

        for(Device dC : tempArrayList){
            for(Device dC2 : listOfConnectedDevices){
                if(compareDeviceAttributes(dC,dC2)){
                    dC.setConnected(true);
                }
            }
        }


        return tempArrayList;
    }

    public boolean compareDeviceAttributes(Device dC,Device dC2){
        if(dC.deviceName.equals(dC2.deviceName) && dC.deviceAddress.equals(dC2.deviceAddress)){
            return true;
        }
        return false;
    }

    public boolean isDeviceConnected(Device device){
        for(Device dC : listOfConnectedDevices){
            if(compareDeviceAttributes(dC,device)){
                return true;
            }
        }
        return false;
    }

    public void connectToSoldierDevices(){
        for(Device dC : listOfPairedDevices){
            if(dC.deviceAddress.equals("84:68:3E:00:17:38")){
                checkDeviceKindAndLaunchResponsibleThread(dC);
            }
        }
    }

    public void checkDeviceKindAndLaunchResponsibleThread(Device dC){
        if(dC.deviceKind.equals("CLASSIC")){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ClassicConnection connectionObject = new ClassicConnection(mBluetoothAdapter, dC.deviceName, dC.deviceAddress);
            connectionObject.start();
        }else if(dC.deviceKind.equals("DUAL")){
            Log.e(this.getClass().getName() + "","The type of device is DUAL");
        }else if(dC.deviceKind.equals("LE")){
            Intent gattServiceIntent = new Intent(mainActivity.getApplicationContext(), BluetoothLeService.class);
            mainActivity.mDeviceAddress = dC.deviceAddress;
            mainActivity.bindService(gattServiceIntent, mainActivity.mServiceConnection, Context.BIND_AUTO_CREATE);

        }else if(dC.deviceKind.equals("UNKNOWN")){
            Log.e(this.getClass().getName() + "","The type of device is not known");
        }
    }

}
