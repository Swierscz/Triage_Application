package com.example.work.triageapp2.Database;

/**
 * Created by BoryS on 29.10.2017.
 */

public class Emg {
    private long id;
    private double emg;

    public Emg(long id, double hearRate){
        this.id = id;
        this.emg = hearRate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getEmg() {
        return emg;
    }

    public void setEmg(int emg) {
        this.emg = emg;
    }
}
