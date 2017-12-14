package com.example.work.triageapp2.ActivitiesAndFragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;

import com.example.work.triageapp2.Bluetooth.Ble.BluetoothLeService;
import com.example.work.triageapp2.Bluetooth.OtherBluetoothStuff.Device;

import static android.content.ContentValues.TAG;

/**
 * Created by BoryS on 22.10.2017.
 */

public class Receiver {
    private MainActivity mainActivity;

    public Receiver(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }
    

    private final BroadcastReceiver bluetoothStateChangeReceiver = new BroadcastReceiver() {
        public void onReceive (Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_ON)
                    mainActivity.disableBluetoothIcon.setVisibility(View.INVISIBLE);

                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF)
                    mainActivity.disableBluetoothIcon.setVisibility(View.VISIBLE);
            }
        }

    };



    private final BroadcastReceiver deviceConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.i(TAG, device.getName() + "    Bluetooth Device Connected");
                for (Device dC : mainActivity.connection.listOfDevices) {

                    if (device.getName().equals(dC.deviceName) && device.getAddress().equals(dC.deviceAddress)) {
                        mainActivity.connection.listOfConnectedDevices.add(dC);
                        final Intent intent2 = new Intent("LIST_REFRESH");
                        mainActivity.getApplicationContext().sendBroadcast(intent2);
                    }
                }

            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.i(TAG, device.getName() + "    Bluetooth Device Disconnected");
                for(Device dC : mainActivity.connection.listOfConnectedDevices){
                    if(device.getName().equals(dC.deviceName) && device.getAddress().equals(dC.deviceAddress)){
                      mainActivity.connection.listOfConnectedDevices.remove(dC);
                        final Intent intent2 = new Intent("LIST_REFRESH");
                        mainActivity.getApplicationContext().sendBroadcast(intent2);
                    }
                }
            }
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mainActivity.isGattConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mainActivity.isGattConnected = false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                mainActivity.displayGattServices(mainActivity.mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
              //  mainActivity.readAndNotifySelectedCharacteristic();
            }
        }
    };



    //region _____filters_____
    private IntentFilter createIntentFilterForBLEDeviceClick(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("BLE_DEVICE_NAME_AND_ADDRESS");
        return  intentFilter;
    }

    private IntentFilter createIntentFilterForSurfaceCreationReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SURFACE_CREATED");
        intentFilter.addAction("SURFACE_DESTROYED");
        return intentFilter;
    }

    private IntentFilter createIntentFilterForDeviceConnectionReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    private static IntentFilter createGATTIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
//endregion

    //region ______register and unregister receivers_____
    public void registerReceivers(){
        mainActivity.registerReceiver(bluetoothStateChangeReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mainActivity.registerReceiver(mGattUpdateReceiver, createGATTIntentFilter());
        mainActivity.registerReceiver(deviceConnectionReceiver,createIntentFilterForDeviceConnectionReceiver());
    }

    public void unregisterReceivers(){
        mainActivity.unregisterReceiver(bluetoothStateChangeReceiver);
        mainActivity.unregisterReceiver(mGattUpdateReceiver);
        mainActivity.unregisterReceiver(deviceConnectionReceiver);
    }
    //endregion

}
