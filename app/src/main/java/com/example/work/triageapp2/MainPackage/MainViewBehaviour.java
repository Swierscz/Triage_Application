package com.example.work.triageapp2.MainPackage;

import android.util.Log;
import android.view.View;

import com.example.work.triageapp2.Bluetooth.StatusConnectionClock;

/**
 * Created by BoryS on 26.10.2017.
 */

public class MainViewBehaviour extends Thread{
    private final static String TAG = MainViewBehaviour.class.getSimpleName();
    private MainActivity mainActivity;
    private HearRateViewThread hearRateViewThread;
    private boolean running = true;

    public MainViewBehaviour(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        hearRateViewThread = new HearRateViewThread(this);
        hearRateViewThread.start();
    }

    @Override
    public void run() {
        while(running){
            if(mainActivity.getDataStorage()!=null) {
                mainActivity.setTriageImage(mainActivity.getDataStorage().getCurrentTriageCategory());
                mainActivity.refreshHistoryImages(mainActivity.getDataStorage().getTriageCategoriesHistory());
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }
}
