package com.example.work.triageapp2.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by BoryS on 11.01.2018.
 */

public class EmgDAOImpl implements EmgDAO {
    private SQLiteDatabase db;

    public EmgDAOImpl(SQLiteDatabase db){
        this.db = db;
    }

    @Override
    public void insertEmg(double emg) {
        ContentValues newEmgValues = new ContentValues();
        newEmgValues.put(EmgTable.EMG_VALUE, emg);
        db.insert(EmgTable.EMG_TABLE,null,newEmgValues);
    }

    @Override
    public ArrayList<Double> getAllEmgs() {
        ArrayList<Double> temp = new ArrayList<Double>();
        String[] columns = {EmgTable.KEY_ID, EmgTable.EMG_VALUE};
        Cursor cursor = db.query(EmgTable.EMG_TABLE, columns, null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                temp.add(cursor.getDouble(EmgTable.EMG_VALUE_COLUMN));
            }while(cursor.moveToNext());
        }
        return temp;
    }
}
/*
*
*     public boolean updateEmgTable(long id, double emgValue) {
        String where = KEY_ID + "=" + id;
        ContentValues updateEmgValues = new ContentValues();
        updateEmgValues.put(EMG, emgValue);
        return db.update(DB_MUSCLE_EMG_TABLE, updateEmgValues, where, null) > 0;
    }

    public long insertEmg(double emg){
        ContentValues newEmgValues = new ContentValues();
        newEmgValues.put(EMG,emg);
        return db.insert(DB_MUSCLE_EMG_TABLE,null,newEmgValues);
    }

    public boolean deleteEmg(long id){
        String where = KEY_ID + "=" + id;
        return db.delete(DB_MUSCLE_EMG_TABLE, where, null) > 0;
    }

    public Cursor getAllEmg() {
        String[] columns = {KEY_ID, EMG};
        return db.query(DB_MUSCLE_EMG_TABLE, columns, null, null, null, null, null);
    }

    public Emg getEmg(long id) {
        String[] columns = {KEY_ID, EMG};
        String where = KEY_ID + "=" + id;
        Cursor cursor = db.query(DB_MUSCLE_EMG_TABLE, columns, where, null, null, null, null);
        Emg task = null;
        if(cursor != null && cursor.moveToFirst()) {
            double emgMeasure = cursor.getDouble(EMG_COLUMN);
            task = new Emg(id, emgMeasure);
        }
        return task;
    }
*
* */