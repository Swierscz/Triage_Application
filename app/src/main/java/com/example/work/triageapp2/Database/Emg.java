package com.example.work.triageapp2.Database;

/**
 * Created by BoryS on 29.10.2017.
 */

public class Emg {
    private long id;
    private double value;

    public Emg(long id, double value){
        this.id = id;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
