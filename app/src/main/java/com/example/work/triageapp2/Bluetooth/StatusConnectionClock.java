package com.example.work.triageapp2.Bluetooth;

import com.example.work.triageapp2.MainPackage.SoldierStatus;

/**
 * Created by BoryS on 26.10.2017.
 */

public class StatusConnectionClock extends Thread {
    private final static String TAG = StatusConnectionClock.class.getSimpleName();
    public static volatile int heartRateConnectionTimeInSeconds;
    public static boolean isHeartRateActive = false;
    private boolean running = true;


    public StatusConnectionClock(){
        heartRateConnectionTimeInSeconds = 0;
    }

    @Override
    public void run() {
        while(running){
            manageHeartRateStatus();
        }
    }

    private void manageHeartRateStatus(){
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
