package com.example.work.triageapp2.MainPackage;


import android.graphics.Canvas;
import android.graphics.Color;
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
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.work.triageapp2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by BoryS on 07.12.2017.
 */

public class EmgFragment extends Fragment implements OnBackPressedListener, IfMainScreenCheck {
    private final static String TAG = EmgFragment.class.getSimpleName();
    public static final int PLOT_SIZE = 300;
    public static ArrayList<Float> plotValues = new ArrayList<Float>();
    private XYPlot plot;

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

    private void initPlot(){
        plot = (XYPlot)getActivity().findViewById(R.id.plot);
        MyFadeFormatter formatter =new MyFadeFormatter(2000);

        formatter.setLegendIconEnabled(false);
        plot.setPadding(0,0,0,0);
        plot.setPlotMargins(0,0,0,0);
        plot.getGraph().setSize(new Size(-50, SizeMode.FILL, 20, SizeMode.FILL));
        plot.getGraph().position(7,HorizontalPositioning.ABSOLUTE_FROM_LEFT,0,VerticalPositioning.ABSOLUTE_FROM_TOP);
        plot.setRangeBoundaries(0, 300, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, EmgFragment.PLOT_SIZE, BoundaryMode.FIXED);
        plot.setDomainStep(StepMode.INCREMENT_BY_VAL,100);
        plot.setRangeStepValue(7);
        plot.getGraph().setLineLabelRenderer(XYGraphWidget.Edge.LEFT,new MyLineLabelRenderer2());
        plot.getGraph().setLineLabelRenderer(XYGraphWidget.Edge.BOTTOM,new MyLineLabelRenderer1());
    }

    public void refreshPlot(){
        XYSeries series1 = new SimpleXYSeries(
                plotValues, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");
        MyFadeFormatter formatter = new MyFadeFormatter(2000);
        formatter.setLegendIconEnabled(false);

        plot.clear();
        plot.addSeries(series1, formatter);
        plot.redraw();
    }

    @Override
    public void onBackPressed() {
        ((MainActivity) getActivity()).setEmgFragmentToNull();
        setIfItIsMainScreen((MainActivity)getActivity(),true);
        getActivity().getSupportFragmentManager().beginTransaction().
                remove(getActivity().getSupportFragmentManager().findFragmentByTag("EMG_FRAGMENT")).commit();
        getActivity().getSupportFragmentManager().popBackStackImmediate();

    }

    private static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {
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

    public static <T> List<T> rotate(List<T> aL, int shift) {
        List<T> newValues = new ArrayList<>(aL);
        Collections.rotate(newValues, shift);
        return newValues;
    }

    public static void fillPlotValues(float f1){
        if(plotValues.size()<PLOT_SIZE)
            plotValues.add(f1);
        else {
            plotValues.remove(0);
            plotValues.add(f1);
        }
    }

    class MyLineLabelRenderer1 extends XYGraphWidget.LineLabelRenderer {

        @Override
        protected void drawLabel(Canvas canvas, String text, Paint paint,
                                 float x, float y, boolean isOrigin) {
//            if(isOrigin) {
                // make the origin labels red:


                final Paint originPaint = new Paint(paint);
                originPaint.setColor(Color.RED);
                originPaint.setTextSize(20);
                super.drawLabel(canvas, text, originPaint, x, y , isOrigin);
//            } else {
//                super.drawLabel(canvas, text, paint, x, y , isOrigin);
//            }
        }
    }

    class MyLineLabelRenderer2 extends XYGraphWidget.LineLabelRenderer {

        @Override
        protected void drawLabel(Canvas canvas, String text, Paint paint,
                                 float x, float y, boolean isOrigin) {
            if(isOrigin) {

            final Paint originPaint = new Paint(paint);
            originPaint.setColor(Color.RED);
            originPaint.setTextSize(0);
            super.drawLabel(canvas, text, originPaint, x, y , isOrigin);
            } else {
                final Paint originPaint = new Paint(paint);
                originPaint.setColor(Color.RED);
                originPaint.setTextSize(20);
                super.drawLabel(canvas, text, originPaint, x, y , isOrigin);
            }
        }
    }

}
