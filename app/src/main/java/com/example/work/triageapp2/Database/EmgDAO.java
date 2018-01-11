package com.example.work.triageapp2.Database;

import java.util.ArrayList;

/**
 * Created by BoryS on 11.01.2018.
 */

public interface EmgDAO {
    void insertEmg(double emg);
    ArrayList<Double> getAllEmgs();
}
