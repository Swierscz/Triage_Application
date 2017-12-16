package com.example.work.triageapp2.MainPackage.Activities;

import android.view.View;

import com.example.work.triageapp2.MainPackage.Activities.MainActivity;
import com.example.work.triageapp2.Bluetooth.SoldierStatus;

/**
 * Created by BoryS on 26.10.2017.
 */

public class MainViewBehaviour extends Thread{
    MainActivity mainActivity;


    boolean isHrIconEnabled = false;
    private boolean running;

    public MainViewBehaviour(MainActivity mainActivity){
        this.mainActivity = mainActivity;
//        triagePanel = (Button) mainActivity.findViewById(R.id.triageButton);


        running = true;
    }


    @Override
    public void run() {
        while(running){
            managerHeartRateView();
        }
    }

    public void managerHeartRateView(){
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
