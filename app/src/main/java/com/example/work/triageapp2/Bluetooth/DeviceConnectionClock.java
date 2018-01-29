package com.example.work.triageapp2.Bluetooth;

/**
 * Created by BoryS on 06.01.2018.
 */

public class DeviceConnectionClock extends Thread {
    private static final String TAG = DeviceConnectionClock.class.getName();
    private final int interval = 10000;
    private Connection connection;

    public static boolean running = true;

    public DeviceConnectionClock(Connection connection){
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(interval * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(running){
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connectNotConnectedDevices();
        }
    }

    private void connectNotConnectedDevices(){
        for(Device device : connection.listOfAllDevices){
            if(device.isFound()){
                if(device.isPaired()){
                    if(!device.isConnected()){
                        if(device.getKind().equals(Connection.TYPE_LE)){
                            connection.getBluetoothManagement().getmBluetoothLeService().connect(device.getAddress());
                            connection.sendListRefreshEvent();
                        }
                        if(device.getKind().equals(Connection.TYPE_CLASSIC)){
//                            connection.getBluetoothManagement().getConnection().createClassicConnection(device);
//                            connection.sendListRefreshEvent();
                        }
                    }
                }
            }
        }
    }

}
