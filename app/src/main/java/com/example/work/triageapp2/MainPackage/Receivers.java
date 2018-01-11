package com.example.work.triageapp2.MainPackage;

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

public class Receivers {
    private final static String TAG = Receivers.class.getSimpleName();
    private MainActivity mainActivity;

    public Receivers(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        registerReceivers();
    }
    
//MainActivity
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

//BleService
    private final BroadcastReceiver mGattStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mainActivity.getBluetoothManagement().setGattConnected(true);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mainActivity.getBluetoothManagement().setGattConnected(false);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                mainActivity.getBluetoothManagement().readCharacteristicsFromServices(mainActivity.getBluetoothManagement().getmBluetoothLeService().getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
              //  mainActivity.readAndNotifySelectedCharacteristic();
            }
        }
    };
//Calibration




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
        mainActivity.registerReceiver(mGattStatusReceiver, createGATTIntentFilter());
    }

    public void unregisterReceivers(){
        mainActivity.unregisterReceiver(bluetoothStateChangeReceiver);
        mainActivity.unregisterReceiver(mGattStatusReceiver);
    }
    //endregion

}
