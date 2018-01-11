package com.example.work.triageapp2.Database;

/**
 * Created by BoryS on 11.01.2018.
 */

public class EmgTable {
    public static final String EMG_TABLE = "MUSCLE_EMG";

    public static final String KEY_ID = "id";
    public static final String ID_OPTIONS = "INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final int ID_COLUMN = 0;
    public static final String EMG_VALUE = "heart_rate";
    public static final String EMG_OPTIONS = "REAL DEFAULT 0";
    public static final int EMG_VALUE_COLUMN = 1;

    public static final String DB_CREATE_MUSCLE_TABLE =
            "CREATE TABLE " + EMG_TABLE + "( " +
                    KEY_ID + " " + ID_OPTIONS + ", " +
                    EMG_VALUE + " " + EMG_OPTIONS +
                    ");";
    public static final String DROP_EMG_TABLE =
            "DROP TABLE IF EXISTS " + EMG_TABLE;
}
