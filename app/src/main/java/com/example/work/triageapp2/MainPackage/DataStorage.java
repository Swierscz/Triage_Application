package com.example.work.triageapp2.MainPackage;

import android.content.Context;
import android.util.Log;

import com.example.work.triageapp2.Database.DBAdapter;

import java.util.ArrayList;

/**
 * Created by BoryS on 10.01.2018.
 */

public class DataStorage {
    private static final String TAG = DataStorage.class.getName();
    private static DataStorage INSTANCE;
    private DBAdapter dbAdapter;

    private TriageCategory currentTriageCategory = TriageCategory.T1;
    private TriageCategory[] triageCategoriesHistory = new TriageCategory[10];

    private int currentHeartRate;
    private ArrayList<Integer> heartRateData = new ArrayList<Integer>();




    private DataStorage() {
       dbAdapter = DBAdapter.getInstance();
    }

    public static DataStorage getInstance(){
        if(INSTANCE == null)
            synchronized (DBAdapter.class){
                if(INSTANCE == null)
                    INSTANCE = new DataStorage();
            }
        return INSTANCE;
    }


    public void addHeartRateValue(int heartRateValue){
        heartRateData.add(heartRateValue);
    }

    public ArrayList<Integer> getHrData(){
        ArrayList<Integer> temp = new ArrayList<Integer>(heartRateData);
        insertCollectedHrDataToDataBase(temp);
        heartRateData.clear();
        return temp;
    }

    private void insertCollectedHrDataToDataBase(ArrayList<Integer> temp){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for(Integer value : temp){
                    dbAdapter.heartRateDataBase.insertHeartRate(value);
                }
            }
        });
        thread.start();
    }

    public int getCurrentHeartRate() {
        return currentHeartRate;
    }

    public void setCurrentHeartRate(int currentHeartRate) {
        this.currentHeartRate = currentHeartRate;
    }

    public TriageCategory getCurrentTriageCategory() {
        return currentTriageCategory;
    }

    public void setCurrentTriageCategory(TriageCategory currentTriageCategory) {
        this.currentTriageCategory = currentTriageCategory;
    }

    public TriageCategory[] getTriageCategoriesHistory() {
        return triageCategoriesHistory;
    }

    public void addCategoryToHistory(TriageCategory triageCategory){
        TriageCategory temp = triageCategory;
        for(int i = 0; i<10; i++ ){
            TriageCategory lastCat = triageCategoriesHistory[i];
            triageCategoriesHistory[i] = temp;
            temp = lastCat;
        }
    }
}
