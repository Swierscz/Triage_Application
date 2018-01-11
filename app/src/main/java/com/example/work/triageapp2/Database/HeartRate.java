package com.example.work.triageapp2.Database;

/**
 * Created by BoryS on 10.01.2018.
 */

public class HeartRate {
    private long id;
    private int value;

    public HeartRate(long id, int value) {
        this.id = id;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
