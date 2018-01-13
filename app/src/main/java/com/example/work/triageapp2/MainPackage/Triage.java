package com.example.work.triageapp2.MainPackage;

import android.util.Log;

import com.example.work.triageapp2.Bluetooth.StatusConnectionClock;

import java.util.ArrayList;

/**
 * Created by BoryS on 10.01.2018.
 */

public class Triage extends Thread{
    private final static String TAG = Triage.class.getSimpleName();
    private DataStorage dataStorage;
    private boolean running = true;

    public Triage(){
        dataStorage = DataStorage.getInstance();
    }

    @Override
    public void run() {
        TriageCategory currentCategory;
        while(running){
            currentCategory = assessTriageCategory();
            dataStorage.setCurrentTriageCategory(currentCategory);
            dataStorage.addCategoryToHistory(currentCategory);
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private TriageCategory assessTriageCategory(){
        TriageCategory triageCategory = TriageCategory.T1;
        int averageHeartRate = getAverageHeartRate(dataStorage.getHrData());

        if(!StatusConnectionClock.isHeartRateActive){
            triageCategory = TriageCategory.NOT_DEFINED;
        }
        else if(averageHeartRate < 65 && averageHeartRate>30) triageCategory = TriageCategory.T1;
        else if(averageHeartRate < 75 && averageHeartRate > 30) triageCategory = TriageCategory.T2;
        else if (averageHeartRate < 85 && averageHeartRate > 30) triageCategory = TriageCategory.T3;
        else triageCategory = TriageCategory.T4;
        Log.i(TAG, "Kategoria: " + triageCategory);
        return triageCategory;
    }

    private int getAverageHeartRate(ArrayList<Integer> heartRateValues){
        int temp = 0;
        for(Integer value : heartRateValues){
            temp+=value;
        }
        if(heartRateValues.size()!=0)
            Log.i(TAG,"" + temp/heartRateValues.size());
        if(heartRateValues.size()!=0)
            return temp/heartRateValues.size();
        else return 0;
    }
}
