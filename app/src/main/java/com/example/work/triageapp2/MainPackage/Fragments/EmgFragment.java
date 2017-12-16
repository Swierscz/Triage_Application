package com.example.work.triageapp2.MainPackage.Fragments;


import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.Size;
import com.androidplot.ui.SizeMode;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.work.triageapp2.MainPackage.Activities.MainActivity;
import com.example.work.triageapp2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by BoryS on 07.12.2017.
 */

public class EmgFragment extends Fragment implements OnBackPressedListener, CheckIfMainScreen {

    private XYPlot plot;

    public static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {

        private int trailSize;

        public MyFadeFormatter(int trailSize) {
            this.trailSize = trailSize;
        }

        @Override
        public Paint getLinePaint(int thisIndex, int latestIndex, int seriesSize) {
            // offset from the latest index:
            int offset;
            if(thisIndex > latestIndex) {
                offset = latestIndex + (seriesSize - thisIndex);
            } else {
                offset =  latestIndex - thisIndex;
            }

            float scale = 255f / trailSize;
            int alpha = (int) (255 - (offset * scale));
            getLinePaint().setAlpha(alpha > 0 ? alpha : 0);
            return getLinePaint();
        }
    }

    public void initPlot(){
        plot = (XYPlot)getActivity().findViewById(R.id.plot);

        MyFadeFormatter formatter =new MyFadeFormatter(2000);
        formatter.setLegendIconEnabled(false);

        plot.setPadding(0,0,0,0);
        plot.setPlotMargins(0,0,0,0);

//        plot.getGraph().setSize(new Size);
        plot.getGraph().setSize(new Size(
                -10, SizeMode.FILL,
                10, SizeMode.FILL));
        plot.getGraph().position(10,HorizontalPositioning.ABSOLUTE_FROM_LEFT,35,VerticalPositioning.ABSOLUTE_FROM_TOP);

        plot.setRangeBoundaries(0, 400, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, MainActivity.PLOT_SIZE, BoundaryMode.FIXED);
        plot.setDomainStep(StepMode.INCREMENT_BY_VAL,100);
        plot.setRangeStepValue(9);
        // reduce the number of range labels
//        plot.setLinesPerRangeLabel(3);
    }

    public static <T> List<T> rotate(List<T> aL, int shift) {
        List<T> newValues = new ArrayList<>(aL);
        Collections.rotate(newValues, shift);
        return newValues;
    }


    public void refreshPlot(){
        XYSeries series1 = new SimpleXYSeries(
                ((MainActivity)getActivity()).getPlotList(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");
        MyFadeFormatter formatter =new MyFadeFormatter(2000);
        formatter.setLegendIconEnabled(false);
        plot.clear();
        plot.addSeries(series1, formatter);
        plot.redraw();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.emg_panel,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Emg Plot");
        initPlot();
        refreshPlot();

    }

    @Override
    public void onBackPressed() {
        ((MainActivity) getActivity()).setEmgFragmentToNull();
        setIfItIsMainScreen((MainActivity)getActivity(),true);
        getActivity().getSupportFragmentManager().beginTransaction().
                remove(getActivity().getSupportFragmentManager().findFragmentByTag("EMG_FRAGMENT")).commit();
        getActivity().getSupportFragmentManager().popBackStackImmediate();

    }
}
