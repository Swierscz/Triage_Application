package com.example.work.triageapp2.AppGraphic;

import com.example.work.triageapp2.SoldierParameter;

/**
 * Created by BoryS on 26.10.2017.
 */

public class TimerThreadForHeartRate extends Thread {
    AnimThread animThread;
    private boolean running;

    public TimerThreadForHeartRate(AnimThread animThread){
        this.animThread = animThread;
        running = true;
    }

    @Override
    public void run() {

        while(running) {
            int heartRate = SoldierParameter.heartRate;
            if (heartRate != 0) {
                int breakTime = 60000 / heartRate;
                try {
                    Thread.sleep(breakTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(animThread.isHeartShouldBeDrawn()==true){
                    animThread.setHeartShouldBeDrawn(false);
                }else{
                    animThread.setHeartShouldBeDrawn(true);
                }
            }else{
                if(animThread.isHeartShouldBeDrawn() == true)
                    animThread.setHeartShouldBeDrawn(false);
            }
        }
    }
}
