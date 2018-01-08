package com.example.work.triageapp2.MainPackage;

import android.view.View;

/**
 * Created by BoryS on 26.10.2017.
 */

public class MainViewBehaviour extends Thread{
    private final static String TAG = MainViewBehaviour.class.getSimpleName();
    MainActivity mainActivity;


    boolean isHrIconEnabled = false;
    private boolean running;
    private int lastHr;

    public MainViewBehaviour(MainActivity mainActivity){
        this.mainActivity = mainActivity;

        running = true;
    }


    @Override
    public void run() {
        while(running){
            manageHeartRateView();
        }
    }

    public void manageHeartRateView(){
        if(mainActivity.isTriageScreenVisible) {
            if (SoldierStatus.isHeartRateActive) {
                if (!isHrIconEnabled) {
                    mainActivity.setIsHrViewHasWholeHeartImage(true);
                    isHrIconEnabled = true;
                }
                manageHeartRateBeep();
            } else {
                if (isHrIconEnabled) {
                    mainActivity.setIsHrViewShouldBeVisible(true);
                    mainActivity.setIsHrViewHasWholeHeartImage(false);
                    isHrIconEnabled = false;
                    mainActivity.setHr(0);
                }
            }
        }else if(mainActivity.getHrView().getVisibility() == View.VISIBLE){
            mainActivity.setIsHrViewShouldBeVisible(false);
        }
    }

    public void manageHeartRateBeep() {
        int heartRate = SoldierStatus.heartRate;
        if (heartRate > 50 && heartRate < 200) {
            lastHr = heartRate;
            manageHeartRateViewForSpecifiedRate(heartRate);
        }else{
            manageHeartRateViewForSpecifiedRate(lastHr);
        }
    }

    public void manageHeartRateViewForSpecifiedRate(int rate){
        int breakTime = 60000 / (rate * 2);
        try {
            Thread.sleep(breakTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mainActivity.getHrView().getVisibility()== View.VISIBLE) {
            mainActivity.setIsHrViewShouldBeVisible(false);
        } else {
            mainActivity.setIsHrViewShouldBeVisible(true);
        }
    }


    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

}
