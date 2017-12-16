package com.example.work.triageapp2.Bluetooth.Classic;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.example.work.triageapp2.Bluetooth.DevicesManagment.PolarIWLDataReceiver;

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
        PolarIWLDataReceiver deviceReceiverThread =
        new PolarIWLDataReceiver(mmSocket);
        deviceReceiverThread.start();
    }


}
