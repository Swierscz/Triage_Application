package com.example.work.triageapp2.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by BoryS on 11.01.2018.
 */

public class HeartRateDAOImpl implements HeartRateDAO {
    private SQLiteDatabase db;

    public HeartRateDAOImpl(SQLiteDatabase db){
        this.db = db;
    }

    @Override
    public void insertHeartRate(int hr) {
        ContentValues newHeartRateValues = new ContentValues();
        newHeartRateValues.put(HeartRateTable.HEARTRATE_VALUE,hr);
        db.insert(HeartRateTable.HEARTRATE_TABLE,null,newHeartRateValues);
    }

    @Override
    public ArrayList<Integer> getAllHeartRates() {
        ArrayList<Integer> temp = new ArrayList<Integer>();
        String[] columns = {HeartRateTable.KEY_ID, HeartRateTable.HEARTRATE_VALUE};
        Cursor cursor = db.query(HeartRateTable.HEARTRATE_TABLE, columns, null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                temp.add(cursor.getInt(HeartRateTable.HEARTRATE_VALUE_COLUMN));
            }while(cursor.moveToNext());
        }
        return temp;
    }
}
