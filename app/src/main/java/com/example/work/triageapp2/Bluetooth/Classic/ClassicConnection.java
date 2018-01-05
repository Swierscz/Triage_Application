package com.example.work.triageapp2.Bluetooth.Classic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import com.example.work.triageapp2.Bluetooth.Connection;
import com.example.work.triageapp2.Bluetooth.Device;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by BoryS on 15.09.2017.
 */

public class ClassicConnection extends Thread {
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private final static String TAG = ClassicConnection.class.getSimpleName();
    ClassicConnectionManager classicConnectionManager;
    public Connection connection;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    String deviceName;
    String deviceAddress;





    public ClassicConnection(Connection connection,BluetoothAdapter mBluetoothAdapter, String deviceName, String deviceAddress){
        this.connection = connection;
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
            Log.e(TAG, "Socket's create() method failed", e);
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
            Log.e(TAG, "Error creating socket");
        }
        try {
            mmSocket.connect();

            for(Device device : Connection.listOfAllDevices){
                if(device.deviceAddress.equals(mmDevice.getAddress())){
                    device.setConnected(true);
                }
            }
            Intent intent1 = new Intent();
            intent1.setAction("REFRESH_DEVICE_LIST");
            connection.bluetoothManagement.getMainActivity().sendBroadcast(intent1);

            Log.e("", "Connected");
            classicConnectionManager = new ClassicConnectionManager(this,mmSocket,mmDevice);
        } catch (IOException e) {
            Log.e("", e.getMessage());
            try {
                Log.e("", "trying fallback...");
                mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
                mmSocket.connect();
                classicConnectionManager = new ClassicConnectionManager(this,mmSocket, mmDevice);
                Log.e("", "Connected");
            } catch (Exception e2) {
                Log.e("", "Couldn't establish Bluetooth connection!");
            }
        }
    }

    public void closeSocket(){
        try {
            classicConnectionManager.polarIWLDataReceiver.running = false;
            for(Device device : Connection.listOfAllDevices){
                if(device.deviceAddress.equals(mmDevice.getAddress())){
                    device.setConnected(false);
                }
            }
            Intent intent1 = new Intent();
            intent1.setAction("REFRESH_DEVICE_LIST");
            connection.bluetoothManagement.getMainActivity().sendBroadcast(intent1);
            mmSocket.close();
            Log.i(TAG,"Socket have been closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
