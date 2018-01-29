package com.example.work.triageapp2.MainPackage;

import android.util.Log;
import android.view.View;

import com.example.work.triageapp2.Bluetooth.StatusConnectionClock;

import static android.content.ContentValues.TAG;

/**
 * Created by BoryS on 11.01.2018.
 */

public class HearRateViewThread extends Thread {
    MainViewBehaviour mainViewBehaviour;
    private boolean running = true;
    private boolean isHrIconEnabled = false;
    private int lastHr;

    public HearRateViewThread(MainViewBehaviour mainViewBehaviour){
        this.mainViewBehaviour = mainViewBehaviour;
    }

    @Override
    public void run() {
        while(running){
            manageHeartRateView();
        }
    }

    private void manageHeartRateView(){
        if(mainViewBehaviour.getMainActivity().isTriageScreenVisible()) {
            if (StatusConnectionClock.isHeartRateActive) {
                if (!isHrIconEnabled) {
                    mainViewBehaviour.getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainViewBehaviour.getMainActivity().setIsHrViewHasWholeHeartImage(true);
                            isHrIconEnabled = true;
                        }
                    });
                }

                simulateHeartRateBeep();

            } else {
                if (isHrIconEnabled) {
                    mainViewBehaviour.getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainViewBehaviour.getMainActivity().setIsHrViewShouldBeVisible(true);
                            mainViewBehaviour.getMainActivity().setIsHrViewHasWholeHeartImage(false);
                            isHrIconEnabled = false;
                            mainViewBehaviour.getMainActivity().setHr(0);
                            mainViewBehaviour.getMainActivity().getHrText().setVisibility(View.GONE);
                        }
                    });
                }
            }
        }else if(mainViewBehaviour.getMainActivity().getHrView().getVisibility() == View.VISIBLE){
            mainViewBehaviour.getMainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainViewBehaviour.getMainActivity().setIsHrViewShouldBeVisible(false);
                    mainViewBehaviour.getMainActivity().getHrText().setVisibility(View.GONE);
                }
            });
        }
    }

    private void simulateHeartRateBeep() {
        int heartRate = mainViewBehaviour.getMainActivity().getDataStorage().getCurrentHeartRate();
        if (heartRate > 50 && heartRate < 200) {
            lastHr = heartRate;
            manageBreakForHeartRateBeep(heartRate);
            simulateSingleBeep();
        }else{
            manageBreakForHeartRateBeep(lastHr);
            simulateSingleBeep();
        }
    }

    private void manageBreakForHeartRateBeep(int rate){
        int breakTime = 60000 / (rate * 2);
        try {
            Thread.sleep(breakTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void simulateSingleBeep(){
        mainViewBehaviour.getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mainViewBehaviour.getMainActivity().getHrView().getVisibility()== View.VISIBLE) {
                    mainViewBehaviour.getMainActivity().getHrText().setVisibility(View.GONE);
                    mainViewBehaviour.getMainActivity().setIsHrViewShouldBeVisible(false);
                } else {
                    mainViewBehaviour.getMainActivity().setIsHrViewShouldBeVisible(true);
                    mainViewBehaviour.getMainActivity().getHrText().setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
