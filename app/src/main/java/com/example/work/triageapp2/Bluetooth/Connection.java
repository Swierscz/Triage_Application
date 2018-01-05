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
    private final static String TAG = Connection.class.getSimpleName();
    public BluetoothManagement bluetoothManagement;
    BluetoothAdapter mBluetoothAdapter;
    public BroadcastReceiver mReceiver;
    public ClassicConnection classicConnection;
    public static ArrayList<Device> listOfFoundDevices = new ArrayList<Device>();
    public static ArrayList<Device> listOfAllDevices = new ArrayList<Device>();


    public Connection(BluetoothManagement bluetoothManagement, BluetoothAdapter mBluetoothAdapter){
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.bluetoothManagement = bluetoothManagement;
        setReceiverForScanAndConnectLEDevices();
        listOfAllDevices.add(new Device("Soldier Device", "84:68:3E:00:17:38", "LE" ));
        listOfAllDevices.add(new Device("Polar iWL", "00:22:D0:00:C8:C7", "CLASSIC" ));
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
                    Log.i(TAG,device.getName() + " found");
                    addDevice(device);
                }
                if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                    listOfFoundDevices.clear();
                    Log.i(TAG,"Action discovery started");
                }
                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    Log.i(TAG,"Action discovery finished");
                    connectToSoldierDevices();
                    for(Device d1 : listOfAllDevices){
                        for(Device d2 : listOfFoundDevices){
                            if(d1.deviceAddress.equals(d2.deviceAddress)){
                                d1.isFound = true;
                            }
                            else{
                                d1.isFound = false;
                            }
                        }
                    }
                    Intent intent1 = new Intent();
                    intent1.setAction("REFRESH_DEVICE_LIST");
                    bluetoothManagement.getMainActivity().sendBroadcast(intent1);
                }
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                        for(Device device1 : listOfAllDevices){
                            if(device.getAddress().equals(device1.deviceAddress)){
                                device1.setPaired(true);
                            }
                        }
                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                        for(Device device1 : listOfAllDevices){
                            if(device.getAddress().equals(device1.deviceAddress)){
                                device1.setPaired(false);
                            }
                        }
                    }
                    Intent intent1 = new Intent();
                    intent1.setAction("REFRESH_DEVICE_LIST");
                    bluetoothManagement.getMainActivity().sendBroadcast(intent1);
                }

            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        bluetoothManagement.getMainActivity().registerReceiver(mReceiver, filter);
    }

    public void addDevice(BluetoothDevice device ){
        String deviceName = device.getName();
        String deviceHardwareAddress = device.getAddress();
        for(Device d : listOfAllDevices){
            if(d.deviceAddress.equals(deviceHardwareAddress)){
                if(d.isConnected == false){
                    checkTypeAndAddDevice(device,deviceName,deviceHardwareAddress);
                }
            }
        }


    }

    public void checkTypeAndAddDevice(BluetoothDevice device, String deviceName, String deviceHardwareAddress){
        if (device.getType() == device.DEVICE_TYPE_CLASSIC) {
            listOfFoundDevices.add(new Device(deviceName, deviceHardwareAddress, "CLASSIC"));
        }
        else if(device.getType() == device.DEVICE_TYPE_DUAL) {
            listOfFoundDevices.add(new Device(deviceName, deviceHardwareAddress, "DUAL"));
        }
        else if(device.getType() == device.DEVICE_TYPE_LE) {
            listOfFoundDevices.add(new Device(deviceName, deviceHardwareAddress, "LE"));
        }
        else {
            listOfFoundDevices.add(new Device(deviceName, deviceHardwareAddress, "UNKNOWN"));
        }
    }

    public void connectToSoldierDevices(){
        for(Device dC : listOfFoundDevices){
            //earHeartRate
            if(dC.deviceAddress.equals("84:68:3E:00:17:38")){
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
        classicConnection = new ClassicConnection(this,mBluetoothAdapter, dC.deviceName, dC.deviceAddress);
        classicConnection.start();
    }

    public void bindLEService(Device dC){

        Intent gattServiceIntent = new Intent(bluetoothManagement.getMainActivity().getApplicationContext(), BluetoothLeService.class);
        bluetoothManagement.mDeviceAddress = dC.deviceAddress;
        bluetoothManagement.getMainActivity().bindService(gattServiceIntent, bluetoothManagement.mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public void setmBluetoothAdapter(BluetoothAdapter mBluetoothAdapter) {
        this.mBluetoothAdapter = mBluetoothAdapter;
    }


}
