package com.example.work.triageapp2.Bluetooth.Classic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by BoryS on 15.09.2017.
 */

public class ClassicConnection extends Thread {
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");                         //DO POPRAWIENIA SPOSÓB POBIERANIA UUID

    ClassicConnectionManager classicConnectionManager;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    String deviceName;
    String deviceAddress;





    public ClassicConnection(BluetoothAdapter mBluetoothAdapter, String deviceName, String deviceAddress){
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }

    public void run(){
        setDevice();
        connectSocket();
    }

    public void setDevice(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices){
                if(deviceName.equals(device.getName()) && deviceAddress.equals(device.getAddress())){
                    mmDevice = device;

                }
            }
        }
    }

    public void connectSocket(){
        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(this.getClass().getName()+"_run()_socketConnection", "Socket's create() method failed", e);
        }

        mBluetoothAdapter.cancelDiscovery();
        if(mmSocket.isConnected())
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        try {
            mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        } catch (Exception e) {
            Log.e(this.getClass().getName()+"_run()_socketConnection", "Error creating socket");
        }

        try {
            mmSocket.connect();
            Log.e("", "Connected");
            classicConnectionManager = new ClassicConnectionManager(mmSocket,mmDevice);
        } catch (IOException e) {
            Log.e("", e.getMessage());
            try {
                Log.e("", "trying fallback...");
                System.out.println("a");
                mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
                mmSocket.connect();
                classicConnectionManager = new ClassicConnectionManager(mmSocket, mmDevice);

                System.out.println("aa");
                Log.e("", "Connected");
            } catch (Exception e2) {
                System.out.println("b");
                Log.e("", "Couldn't establish Bluetooth connection!");
            }
        }
    }

}
