package com.example.work.triageapp2.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.work.triageapp2.Bluetooth.Ble.BluetoothLeService;
import com.example.work.triageapp2.Bluetooth.Classic.ClassicConnection;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by BoryS on 23.11.2017.
 */

public class Connection {
//    MainActivity mainActivity;
    BluetoothManagement bluetoothManagement;
    BluetoothAdapter mBluetoothAdapter;
    BroadcastReceiver mReceiver;

    public ArrayList<Device> listOfDevices = new ArrayList<Device>();
    public ArrayList<Device> listOfConnectedDevices = new ArrayList<Device>();

    public Connection(BluetoothManagement bluetoothManagement, BluetoothAdapter mBluetoothAdapter){
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.bluetoothManagement = bluetoothManagement;
        setReceiverForScanAndConnectLEDevices();
    }


    public void setReceiverForScanAndConnectLEDevices(){
        Log.i(TAG, "setReceiverForScanAndConnectLEDevices function started");
        if(!bluetoothManagement.mBluetoothAdapter.isDiscovering()) {
            bluetoothManagement.mBluetoothAdapter.startDiscovery();
            Log.i(TAG, "discovery function started");
        }
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                //Finding devices
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    addDevice(device);
                }

                if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                    Log.i(TAG,"Action discovery started");
                }
                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    Log.i(TAG,"Action discovery finished");
                    connectToSoldierDevices();
                }
            }
        };



        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        bluetoothManagement.getMainActivity().registerReceiver(mReceiver, filter);
    }

    public void addDevice(BluetoothDevice device ){

        String deviceName = device.getName();
        String deviceHardwareAddress = device.getAddress();

        if (device.getType() == device.DEVICE_TYPE_CLASSIC) {
            listOfDevices.add(new Device(deviceName, deviceHardwareAddress, "CLASSIC"));
        }
        else if(device.getType() == device.DEVICE_TYPE_DUAL) {
            listOfDevices.add(new Device(deviceName, deviceHardwareAddress, "DUAL"));
        }
        else if(device.getType() == device.DEVICE_TYPE_LE) {
            listOfDevices.add(new Device(deviceName, deviceHardwareAddress, "LE"));
        }
        else {
            listOfDevices.add(new Device(deviceName, deviceHardwareAddress, "UNKNOWN"));
        }
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
        for(Device dC : listOfDevices){
            //earHeartRate
            if(dC.deviceAddress.equals("84:68:3E:00:17:38")){
                checkDeviceKindAndLaunchResponsibleThread(dC);
            }
            //iWL
            if(dC.deviceAddress.equals("00:22:D0:00:C8:C7")){
                Log.i(TAG,"iWL");
                checkDeviceKindAndLaunchResponsibleThread(dC);
            }
        }

    }

    public void checkDeviceKindAndLaunchResponsibleThread(Device dC){
        if(dC.deviceKind.equals("CLASSIC")){
            createClassicConnection(dC);
        }else if(dC.deviceKind.equals("DUAL")){
            Log.e(this.getClass().getName() + "","The type of device is DUAL");
        }else if(dC.deviceKind.equals("LE")){
            bindLEService(dC);
        }else if(dC.deviceKind.equals("UNKNOWN")){
            Log.e(this.getClass().getName() + "","The type of device is not known");
        }
    }


    public void createClassicConnection(Device dC){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ClassicConnection connectionObject = new ClassicConnection(mBluetoothAdapter, dC.deviceName, dC.deviceAddress);
        connectionObject.start();
    }

    public void bindLEService(Device dC){
        Intent gattServiceIntent = new Intent(bluetoothManagement.getMainActivity().getApplicationContext(), BluetoothLeService.class);
        bluetoothManagement.mDeviceAddress = dC.deviceAddress;
        bluetoothManagement.getMainActivity().bindService(gattServiceIntent, bluetoothManagement.mServiceConnection, Context.BIND_AUTO_CREATE);
    }

}
