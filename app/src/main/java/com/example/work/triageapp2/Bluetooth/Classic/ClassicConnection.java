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
    private ClassicConnectionManager classicConnectionManager;
    private  Connection connection;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;

    private String deviceName;
    private String deviceAddress;





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

    private void setDevice(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices){
                if(deviceName.equals(device.getName()) && deviceAddress.equals(device.getAddress())){
                    mmDevice = device;

                }
            }
        }
    }

    private void connectSocket(){
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
                if(device.getAddress().equals(mmDevice.getAddress())){
                    device.setConnected(true);
                }
            }

            connection.sendListRefreshEvent();

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
            classicConnectionManager.getPolarIWLDataReceiver().setRunning(false);
            for(Device device : Connection.listOfAllDevices){
                if(device.getAddress().equals(mmDevice.getAddress())){
                    device.setConnected(false);
                }
            }
            connection.sendListRefreshEvent();
            mmSocket.close();
            Log.i(TAG,"Socket have been closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }
}
