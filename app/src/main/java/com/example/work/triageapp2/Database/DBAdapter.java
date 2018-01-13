package com.example.work.triageapp2.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

/**
 * Created by BoryS on 29.10.2017.
 */

public class DBAdapter {
    private static DBAdapter INSTANCE;
    private SQLiteDatabase db;
//    private Context context;
    private DatabaseHelper dbHelper;
    private static boolean doesDatabaseExist(Context context) {
        File dbFile = context.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }

    public EmgDAOImpl emgDataBase;
    public HeartRateDAOImpl heartRateDataBase;


    private DBAdapter(){
    }

    public static DBAdapter getInstance(){
        if(INSTANCE == null)
            synchronized (DBAdapter.class){
                if(INSTANCE == null)
                    INSTANCE = new DBAdapter();
            }
        return INSTANCE;
    }

    private static final String DEBUG_TAG = "SqLiteDBManager";
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "database.db";


    public DBAdapter open(Context context){
        dbHelper = new DatabaseHelper(context,DB_NAME,null,DB_VERSION);
        try{
            db = dbHelper.getWritableDatabase();
        }catch(SQLException e){
            db = dbHelper.getReadableDatabase();
        }
        emgDataBase = new EmgDAOImpl(db);
        heartRateDataBase  = new HeartRateDAOImpl(db);
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    public void deleteDataBase(Context context){

        if(doesDatabaseExist(context)){
            context.deleteDatabase(DB_NAME);
            Log.d("Dbadapter","DB has been deleted");
        }else{
            Log.d("Dbadapter","There is no such database");
        }

    }



    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(HeartRateTable.DB_CREATE_HEARTRATE_TABLE);
            db.execSQL(EmgTable.DB_CREATE_MUSCLE_TABLE);
            Log.d(DEBUG_TAG, "Database creating...");
            Log.d(DEBUG_TAG, "Table " + HeartRateTable.HEARTRATE_TABLE + " ver." + DB_VERSION + " created");
            Log.d(DEBUG_TAG, "Table " + EmgTable.EMG_TABLE+ " ver." + DB_VERSION + " created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(HeartRateTable.DROP_HEARTRATE_TABLE);
            db.execSQL(EmgTable.DROP_EMG_TABLE);
            Log.d(DEBUG_TAG, "Database updating...");
            Log.d(DEBUG_TAG, "Table " + HeartRateTable.HEARTRATE_TABLE + " updated from ver." + oldVersion + " to ver." + newVersion);
            Log.d(DEBUG_TAG, "Table " + EmgTable.EMG_TABLE + " updated from ver." + oldVersion + " to ver." + newVersion);
            Log.d(DEBUG_TAG, "All data is lost.");
            onCreate(db);
        }




    }

    public SQLiteDatabase getDb() {
        return db;
    }
}
