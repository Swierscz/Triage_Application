package com.example.work.triageapp2.Bluetooth.Classic;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.example.work.triageapp2.Bluetooth.DevicesManagment.PolarIWLDataReceiver;

/**
 * Created by BoryS on 15.09.2017.
 */

public class ClassicConnectionManager {
    private final static String TAG = ClassicConnectionManager.class.getSimpleName();
    ClassicConnection classicConnection;
    public final BluetoothSocket mmSocket;
    public PolarIWLDataReceiver polarIWLDataReceiver;

    public ClassicConnectionManager(ClassicConnection classicConnection,BluetoothSocket mmSocket, BluetoothDevice device){
        this.classicConnection = classicConnection;
        this.mmSocket = mmSocket;
        if(device.getName().equals("Polar iWL")){
            polarIWLFunction();
        }
    }

    public void polarIWLFunction(){
        polarIWLDataReceiver =
        new PolarIWLDataReceiver(classicConnection,mmSocket);
        polarIWLDataReceiver.start();
    }


}
