package com.example.work.triageapp2.MainPackage;

import android.view.View;

/**
 * Created by BoryS on 26.10.2017.
 */

public class MainViewBehaviour extends Thread{
    MainActivity mainActivity;


    boolean isHrIconEnabled = false;
    private boolean running;

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
        if(SoldierStatus.isHeartRateActive){
            if(!isHrIconEnabled){
                mainActivity.setDisabledOrNotHrView(true);
                isHrIconEnabled = true;
            }
            manageHeartRateBeep();
        }else{
            if(isHrIconEnabled){
                mainActivity.hideOrShowHrView(true);
                mainActivity.setDisabledOrNotHrView(false);
                isHrIconEnabled = false;
            }
        }
    }

    public void manageHeartRateBeep() {
        int heartRate = SoldierStatus.heartRate;
        if (heartRate != 0) {
            int breakTime = 60000 / heartRate;
            try {
                Thread.sleep(breakTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mainActivity.getHrView().getVisibility()== View.VISIBLE) {
                mainActivity.hideOrShowHrView(false);
            } else {
                mainActivity.hideOrShowHrView(true);
            }
        }else{

        }
    }


    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

}
