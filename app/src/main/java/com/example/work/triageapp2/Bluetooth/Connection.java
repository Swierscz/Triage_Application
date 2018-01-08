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
import com.example.work.triageapp2.MainPackage.DeviceConnectionClock;
import com.example.work.triageapp2.MainPackage.CalibrationFragment;

import java.util.ArrayList;

/**
 * Created by BoryS on 23.11.2017.
 */

public class Connection {
    private final static String TAG = Connection.class.getSimpleName();

    public final static String TYPE_CLASSIC = "TYPE_CLASSIC";
    public final static String TYPE_DUAL = "TYPE_DUAL";
    public final static String TYPE_LE = "TYPE_LE";
    public final static String TYPE_UNKNOWN = "TYPE_UNKNOWN";

    public final static String NAME_SOLDIER_DEVICE = "Soldier Device";
    public final static String ADDRESS_SOLDIER_DEVICE = "84:68:3E:00:17:38";

    public final static String NAME_POLAR_IWL_DEVICE = "Polar iWL";
    public final static String ADDRESS_POLAR_IWL_DEVICE = "00:22:D0:00:C8:C7";



    public BluetoothManagement bluetoothManagement;
    BluetoothAdapter mBluetoothAdapter;
    public BroadcastReceiver mReceiver;
    public ClassicConnection classicConnection;
    public DeviceConnectionClock deviceConnectionClock;
    public static ArrayList<Device> listOfFoundDevices = new ArrayList<Device>();
    public static ArrayList<Device> listOfAllDevices = new ArrayList<Device>();


    public Connection(BluetoothManagement bluetoothManagement, BluetoothAdapter mBluetoothAdapter){
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.bluetoothManagement = bluetoothManagement;
        setReceiverForScanAndConnectLEDevices();
        deviceConnectionClock = new DeviceConnectionClock(this);
        deviceConnectionClock.start();
        listOfAllDevices.add(new Device(NAME_SOLDIER_DEVICE, ADDRESS_SOLDIER_DEVICE, TYPE_LE ));
        listOfAllDevices.add(new Device(NAME_POLAR_IWL_DEVICE, ADDRESS_POLAR_IWL_DEVICE, TYPE_CLASSIC ));
    }


    public void setReceiverForScanAndConnectLEDevices(){
        if(!bluetoothManagement.mBluetoothAdapter.isDiscovering()) {
            bluetoothManagement.mBluetoothAdapter.startDiscovery();
        }

        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    Log.i(TAG,device.getName() + " found");
                    addDevice(device);
                }
                if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                    Log.i(TAG,"Action discovery started");
                    listOfFoundDevices.clear();
                }
                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    Log.i(TAG,"Action discovery finished");
                    connectToSoldierDevices();
                    setFoundStatus();
                    sendListRefreshEvent();
                }
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                    setPairedIfDeviceHasBeenBounded(device,state,prevState);
                    setUnpairedIfDeviceLostBond(device,state,prevState);
                    sendListRefreshEvent();
                }

            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        bluetoothManagement.getMainActivity().registerReceiver(mReceiver, filter);
    }

    public void setPairedIfDeviceHasBeenBounded(BluetoothDevice device, int state, int prevState){
        if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
            for(Device device1 : listOfAllDevices){
                if(device.getAddress().equals(device1.deviceAddress)){
                    device1.setPaired(true);
                }
            }
        }
    }

    public void setUnpairedIfDeviceLostBond(BluetoothDevice device,int state, int prevState){
        if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
            for(Device device1 : listOfAllDevices){
                if(device.getAddress().equals(device1.deviceAddress)){
                    device1.setPaired(false);
                }
            }
        }
    }

    public void setFoundStatus(){
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
    }

    public void sendListRefreshEvent(){
        Intent intent = new Intent();
        intent.setAction(CalibrationFragment.REFRESH_DEVICE_LIST_EVENT);
        bluetoothManagement.getMainActivity().sendBroadcast(intent);
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
            listOfFoundDevices.add(new Device(deviceName, deviceHardwareAddress, TYPE_CLASSIC));
        }
        else if(device.getType() == device.DEVICE_TYPE_DUAL) {
            listOfFoundDevices.add(new Device(deviceName, deviceHardwareAddress, TYPE_DUAL));
        }
        else if(device.getType() == device.DEVICE_TYPE_LE) {
            listOfFoundDevices.add(new Device(deviceName, deviceHardwareAddress, TYPE_LE));
        }
        else {
            listOfFoundDevices.add(new Device(deviceName, deviceHardwareAddress, TYPE_UNKNOWN));
        }
    }

    public void connectToSoldierDevices(){
        for(Device dC : listOfFoundDevices){
            if(dC.deviceAddress.equals(ADDRESS_SOLDIER_DEVICE)){
                checkDeviceKindAndLaunchResponsibleThread(dC);
            }
        }

    }

    public void checkDeviceKindAndLaunchResponsibleThread(Device dC){
        if(dC.deviceKind.equals(TYPE_CLASSIC)){
            createClassicConnection(dC);
        }else if(dC.deviceKind.equals(TYPE_DUAL)){
            Log.e(this.getClass().getName() + "","The type of device is DUAL");
        }else if(dC.deviceKind.equals(TYPE_LE)){
            bindLEService(dC);
        }else if(dC.deviceKind.equals(TYPE_UNKNOWN)){
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

    public void stopDeviceConnectionClock(){
            DeviceConnectionClock.running = false;
            try {
                deviceConnectionClock.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    public void startDeviceConnectionClock(){
        DeviceConnectionClock.running = true;
        deviceConnectionClock = new DeviceConnectionClock(this);
        deviceConnectionClock.start();
    }
}
