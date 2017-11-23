package com.example.work.triageapp2.Bluetooth.Classic;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.example.work.triageapp2.Bluetooth.DevicesManagment.PolarIWL_DataReceiver;

/**
 * Created by BoryS on 15.09.2017.
 */

public class ClassicConnectionManager {
    public final BluetoothSocket mmSocket;

    public ClassicConnectionManager(BluetoothSocket mmSocket, BluetoothDevice device){
        this.mmSocket = mmSocket;
        if(device.getName().equals("Polar iWL")){
            polarIWLFunction();
        }


    }

    public void polarIWLFunction(){
        PolarIWL_DataReceiver deviceReceiverThread =
        new PolarIWL_DataReceiver(mmSocket);
        deviceReceiverThread.start();
    }


}
