package com.example.work.triageapp2.Bluetooth.DevicesManagment;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import com.example.work.triageapp2.Bluetooth.Classic.ClassicConnection;
import com.example.work.triageapp2.Bluetooth.Connection;
import com.example.work.triageapp2.Bluetooth.Device;
import com.example.work.triageapp2.Bluetooth.StatusConnectionClock;
import com.example.work.triageapp2.MainPackage.CalibrationFragment;
import com.example.work.triageapp2.MainPackage.DataStorage;

import java.io.IOException;
import java.io.InputStream;

import static android.content.ContentValues.TAG;

/**
 * Created by BoryS on 15.09.2017.
 * Pairing code: 0000
 */

public class PolarIWLDataReceiver extends Thread {
    private DataStorage dataStorage;
    private ClassicConnection classicConnection;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private boolean running = true;

    public PolarIWLDataReceiver(ClassicConnection classicConnection, BluetoothSocket mmSocket){
        this.classicConnection = classicConnection;
        this.mmSocket = mmSocket;
        dataStorage = DataStorage.getInstance();
        InputStream tmpIn = null;
        try {
            tmpIn = mmSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        mmInStream = tmpIn;
    }


    public void run() {
        mmBuffer = new byte[1024];
        int numBytes = 0; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs.
        while (running) {
          try {
              readAndDecodeStreamData(numBytes);
              setStatusForHeartRate();
          } catch (IOException e) {
              e.printStackTrace();
              System.out.println("Polaczenia input zostalo zerwane");
              setConnectedAndSendRefreshListEvent();
              close();
              running = false;
          }
        }
    }

    private void readAndDecodeStreamData(int numBytes) throws IOException {
        numBytes = mmInStream.read(mmBuffer);

        final StringBuilder sb = new StringBuilder();
        sb.append("Received ").append(numBytes).append(" bytes: ");

        if (numBytes == 7) {
            for (int i = 0; i < numBytes; i++) {
                sb.append(Integer.toHexString(((int) mmBuffer[i]) & 0xff)).append(", ");
            }
            sb.append(Integer.toHexString(((int) mmBuffer[4] * 2) & 0xff)).append(", ");
            Log.v("Tetno: ", "" + mmBuffer[4]);
            Log.v("result", sb.toString());
        }
    }

    private void setStatusForHeartRate(){
        dataStorage.setCurrentHeartRate(mmBuffer[4]);
        if(String.valueOf(mmBuffer[4]).equals("0")){
            StatusConnectionClock.isHeartRateActive = false;
        }
        else{
            classicConnection.getConnection().getBluetoothManagement().getMainActivity().setHr(mmBuffer[4]);
            StatusConnectionClock.resetTimerForHeartRate();
            if(StatusConnectionClock.isHeartRateActive == false)
                StatusConnectionClock.isHeartRateActive = true;
        }
    }

    private void setConnectedAndSendRefreshListEvent(){
        for(Device d : Connection.listOfAllDevices) {
            if (classicConnection.getDeviceAddress().equals(d.getAddress())) {
                d.setConnected(false);
                Intent intent1 = new Intent();
                intent1.setAction(CalibrationFragment.REFRESH_DEVICE_LIST_EVENT);
                classicConnection.getConnection().getBluetoothManagement().getMainActivity().sendBroadcast(intent1);
            }
        }
    }

    private void close() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
