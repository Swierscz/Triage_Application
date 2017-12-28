package com.example.work.triageapp2.MainPackage;

import java.util.ArrayList;

/**
 * Created by BoryS on 16.12.2017.
 */

public class PlotEMG {
    public static final int PLOT_SIZE = 300;

    public PlotEMG(){

    }


    ArrayList<Float> plotList = new ArrayList<Float>();
    public ArrayList<Float> getPlotList(){
        return plotList;
    }

    public void fillPlotValues(float f1){
        if(plotList.size()<PLOT_SIZE)
            plotList.add(f1);
        else {
            plotList.remove(0);
            plotList.add(f1);
        }
    }
}
