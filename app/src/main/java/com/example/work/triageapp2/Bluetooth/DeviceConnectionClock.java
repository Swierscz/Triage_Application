package com.example.work.triageapp2.Bluetooth;

import com.example.work.triageapp2.MainPackage.SoldierStatus;

/**
 * Created by BoryS on 26.10.2017.
 */

public class DeviceConnectionClock extends Thread {

    public static volatile int heartRateConnectionTimeInSeconds;

    public DeviceConnectionClock(){
        heartRateConnectionTimeInSeconds = 0;

    }

    @Override
    public void run() {

        while(true){
            setHeartRateStatusWithSecInterval();
        }

    }


    public void setHeartRateStatusWithSecInterval(){
        if(SoldierStatus.isHeartRateActive)
            heartRateConnectionTimeInSeconds ++;


        if(heartRateConnectionTimeInSeconds == 20){
            SoldierStatus.isHeartRateActive = false;
            SoldierStatus.heartRate = 0;
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void resetTimerForHeartRate(){
        heartRateConnectionTimeInSeconds = 0;
    }

}
