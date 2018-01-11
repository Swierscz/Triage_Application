package com.example.work.triageapp2.MainPackage;

import java.util.ArrayList;

/**
 * Created by BoryS on 10.01.2018.
 */

public class Triage extends Thread{
    TriageCategory triageCategory = TriageCategory.T1;
    DataStorage dataStorage;
    boolean running = true;

    public Triage(){
        dataStorage = DataStorage.getInstance();
    }

    @Override
    public void run() {
        while(running){
           assessTriageCategory();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private TriageCategory assessTriageCategory(){
        TriageCategory triageCategory = TriageCategory.T1;
        int averageHeartRate = getAverageHeartRate(dataStorage.getHrData());

        if(averageHeartRate < 65 && averageHeartRate>30) triageCategory = TriageCategory.T1;
        else if(averageHeartRate < 75 && averageHeartRate > 30) triageCategory = TriageCategory.T2;
        else if (averageHeartRate < 85 && averageHeartRate > 30) triageCategory = TriageCategory.T3;
        else triageCategory = TriageCategory.T4;

        return triageCategory;
    }

    private int getAverageHeartRate(ArrayList<Integer> heartRateValues){
        int temp = 0;
        for(Integer value : heartRateValues){
            temp+=value;
        }
        return temp/heartRateValues.size();
    }

}
