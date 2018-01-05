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

    private SQLiteDatabase db;
    private Context context;
    private DatabaseHelper dbHelper;
    private static boolean doesDatabaseExist(Context context) {
        File dbFile = context.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }

    public DBAdapter(Context context){
        this.context = context;
    }
    private static final String DEBUG_TAG = "SqLiteDBManager";

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "database.db";
    private static final String DB_MUSCLE_EMG_TABLE = "MUSCLE_EMG";


    public static final String KEY_ID = "id";
    public static final String ID_OPTIONS = "INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final int ID_COLUMN = 0;
    public static final String EMG = "heart_rate";
    public static final String EMG_OPTIONS = "REAL DEFAULT 0";
    public static final int EMG_COLUMN = 1;

    private static final String DB_CREATE_MUSCLE_TABLE =
            "CREATE TABLE " + DB_MUSCLE_EMG_TABLE + "( " +
                    KEY_ID + " " + ID_OPTIONS + ", " +
                    EMG + " " + EMG_OPTIONS +
                    ");";
    private static final String DROP_TODO_TABLE =
            "DROP TABLE IF EXISTS " + DB_MUSCLE_EMG_TABLE;

    public DBAdapter open(){
        dbHelper = new DatabaseHelper(context,DB_NAME,null,DB_VERSION);
        try{
            db = dbHelper.getWritableDatabase();
        }catch(SQLException e){
            db = dbHelper.getReadableDatabase();
        }
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    public static void deleteDataBase(Context context){

        if(doesDatabaseExist(context)){
            context.deleteDatabase(DB_NAME);
            Log.d("Dbadapter","DB has been deleted");
        }else{
            Log.d("Dbadapter","There is no such database");
        }

    }

    public boolean updateEmgTable(long id, double emg) {
        String where = KEY_ID + "=" + id;
        ContentValues updateEmgValues = new ContentValues();
        updateEmgValues.put(EMG, emg);
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

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_MUSCLE_TABLE);
            Log.d(DEBUG_TAG, "Database creating...");
            Log.d(DEBUG_TAG, "Table " + DB_MUSCLE_EMG_TABLE + " ver." + DB_VERSION + " created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TODO_TABLE);

            Log.d(DEBUG_TAG, "Database updating...");
            Log.d(DEBUG_TAG, "Table " + DB_MUSCLE_EMG_TABLE + " updated from ver." + oldVersion + " to ver." + newVersion);
            Log.d(DEBUG_TAG, "All data is lost.");

            onCreate(db);
        }




    }


}
