package com.example.work.triageapp2.ActivitiesAndFragments;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

import com.example.work.triageapp2.Bluetooth.Ble.BluetoothLeService;

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

    private final BroadcastReceiver surfaceCreationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals("SURFACE_CREATED")) {
                mainActivity.isSurfaceCreated = true;
                if(mainActivity.isFragmentWorking){
                    mainActivity.setIfItIsTriageScreen(false);
                }else{
                    mainActivity.setIfItIsTriageScreen(true);
                }
            }else if(action.equals("SURFACE_DESTROYED")){
                mainActivity.isSurfaceCreated = false;
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

    private final BroadcastReceiver bleDeviceClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            if (action.equals("BLE_DEVICE_NAME_AND_ADDRESS")) {
                Intent gattServiceIntent = new Intent(mainActivity.getApplicationContext(), BluetoothLeService.class);
                mainActivity.mDeviceAddress = intent.getExtras().getString(MainActivity.EXTRAS_DEVICE_ADDRESS);
                mainActivity.bindService(gattServiceIntent, mainActivity.mServiceConnection,Context.BIND_AUTO_CREATE);

            }
        }
    };

    //region _____filters_____
    private IntentFilter createIntentFilterForBLEDeviceClick(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("BLE_DEVICE_NAME_AND_ADDRESS");
        return  intentFilter;
    };

    private IntentFilter createIntentFilterForSurfaceCreationReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SURFACE_CREATED");
        intentFilter.addAction("SURFACE_DESTROYED");
        return intentFilter;
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
        mainActivity.registerReceiver(surfaceCreationReceiver,createIntentFilterForSurfaceCreationReceiver());
        mainActivity.registerReceiver(bluetoothStateChangeReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mainActivity.registerReceiver(mGattUpdateReceiver, createGATTIntentFilter());
        mainActivity.registerReceiver(bleDeviceClickReceiver, createIntentFilterForBLEDeviceClick());
    }

    public void unregisterReceivers(){
        mainActivity.unregisterReceiver(surfaceCreationReceiver);
        mainActivity.unregisterReceiver(bluetoothStateChangeReceiver);
        mainActivity.unregisterReceiver(mGattUpdateReceiver);
        mainActivity.unregisterReceiver(bleDeviceClickReceiver);
    }
    //endregion

}
