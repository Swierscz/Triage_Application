package com.example.work.triageapp2.Bluetooth.DevicesManagment;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.work.triageapp2.Bluetooth.DeviceConnectionClock;
import com.example.work.triageapp2.MainPackage.SoldierStatus;

import java.io.IOException;
import java.io.InputStream;

import static android.content.ContentValues.TAG;

/**
 * Created by BoryS on 15.09.2017.
 * Pairing code: 0000
 */

public class PolarIWLDataReceiver extends Thread {
    public byte[] mmBuffer; // mmBuffer store for the stream
    public final BluetoothSocket mmSocket;
    public final InputStream mmInStream;

    public PolarIWLDataReceiver(BluetoothSocket mmSocket){
        this.mmSocket = mmSocket;
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
        while (true) {
          try {
              readAndDecodeStreamData(numBytes);
              setStatusForHeartRate();
          } catch (IOException e) {
              e.printStackTrace();
              System.out.println("Polaczenia input zostalo zerwane");
          }
        }
    }


    public void readAndDecodeStreamData(int numBytes) throws IOException {
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

    public void setStatusForHeartRate(){
        SoldierStatus.heartRate = mmBuffer[4];
        if(String.valueOf(mmBuffer[4]).equals("0")){
            SoldierStatus.isHeartRateActive = false;
        }
        else{
            DeviceConnectionClock.resetTimerForHeartRate();
            if(SoldierStatus.isHeartRateActive == false)
                SoldierStatus.isHeartRateActive = true;
        }
    }

    public void setInputStream(){

    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}
