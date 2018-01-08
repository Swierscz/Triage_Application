package com.example.work.triageapp2.MainPackage;

import com.example.work.triageapp2.Bluetooth.Connection;
import com.example.work.triageapp2.Bluetooth.Device;

/**
 * Created by BoryS on 06.01.2018.
 */

public class DeviceConnectionClock extends Thread {
    private static final String TAG = DeviceConnectionClock.class.getName();
    Connection connection;



    public static boolean running = true;

    public DeviceConnectionClock(Connection connection){
        this.connection = connection;
    }

    @Override
    public void run() {

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while(running){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(Device device : connection.listOfAllDevices){
                if(device.isFound()){
                    if(device.isPaired()){
                        if(!device.isConnected()){
                            if(device.deviceKind.equals(Connection.TYPE_LE)){
                                connection.bluetoothManagement.mBluetoothLeService.connect(device.deviceAddress);
                                connection.sendListRefreshEvent();
                            }
                            if(device.deviceKind.equals(Connection.TYPE_CLASSIC)){
                                connection.bluetoothManagement.connection.createClassicConnection(device);
                                connection.sendListRefreshEvent();
                            }
                        }
                    }
                }
            }
        }
    }
}
