package com.example.work.triageapp2.Database;

import android.content.Context;

/**
 * Created by BoryS on 11.01.2018.
 */

public class HeartRateTable {
    public static DBAdapter INSTANCE;
    public static final String HEARTRATE_TABLE = "HEARTRATE_TABLE";
    public static final String KEY_ID = "id";
    public static final String ID_OPTIONS = "INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final int ID_COLUMN = 0;
    public static final String HEARTRATE_VALUE = "heart_rate";
    public static final String HEARTRATE_VALUE_OPTIONS = "INTEGER DEFAULT 0";
    public static final int HEARTRATE_VALUE_COLUMN = 1;
    public static final String DATE_TIME = "date_time";
    public static final String DATE_TIME_OPTIONS = "DATETIME DEFAULT CURRENT_TIMESTAMP";
    public static final int DATE_TIME_COLUMN = 2;

    public static final String DB_CREATE_HEARTRATE_TABLE =
            "CREATE TABLE " + HEARTRATE_TABLE + "( " +
                   KEY_ID + " " + ID_OPTIONS + ", " +
                    HEARTRATE_VALUE + " " + HEARTRATE_VALUE_OPTIONS +
                    DATE_TIME + " " + DATE_TIME_OPTIONS +
                    ");";
    public static final String DROP_HEARTRATE_TABLE =
            "DROP TABLE IF EXISTS " + HEARTRATE_TABLE;
}
