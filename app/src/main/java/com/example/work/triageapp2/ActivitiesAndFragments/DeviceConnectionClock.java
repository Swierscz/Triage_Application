package com.example.work.triageapp2.ActivitiesAndFragments;

import com.example.work.triageapp2.SoldierParameter;

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
            if(SoldierParameter.isHeartRateDeviceConnected)
                heartRateConnectionTimeInSeconds ++;


            if(heartRateConnectionTimeInSeconds == 20){
                SoldierParameter.isHeartRateDeviceConnected = false;
                SoldierParameter.heartRate = 0;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void resetTimerForHeartRate(){
        heartRateConnectionTimeInSeconds = 0;
    }

}
