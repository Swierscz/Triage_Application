package com.example.work.triageapp2.ActivitiesAndFragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.work.triageapp2.Database.DBAdapter;
import com.example.work.triageapp2.R;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by BoryS on 06.08.2017.
 */

public class ManualAssesment extends Fragment implements OnBackPressedListener {

    Button openDB, insertEMG, readEMG, closeDB, deleteDB;
    EditText editText;
    DBAdapter dbAdapter;
    ArrayList<Float> list = new ArrayList<Float>();
    final static String TAG = ManualAssesment.class.getSimpleName();

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

//    private final BroadcastReceiver surfaceCreationReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if (action.equals("EMG_RECEIVED")) {
//
//            }
//        }
//    };
//
//    private IntentFilter createIntentFilterForSurfaceCreationReceiver(){
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("EMG_RECEIVED");
//        return intentFilter;
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.manual_assesment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Manual Assessment");

        //region _____needless code______
        openDB = (Button) getActivity().findViewById(R.id.openDataBaseButton);
        insertEMG = (Button) getActivity().findViewById(R.id.insertButton);
        readEMG = (Button) getActivity().findViewById(R.id.getEmgButton);
        closeDB = (Button) getActivity().findViewById(R.id.closeDbButton);
        deleteDB = (Button) getActivity().findViewById(R.id.deleteDBButton);
        editText = (EditText) getActivity().findViewById(R.id.editText);


    //    dbAdapter= new DBAdapter(getContext());
    //    dbAdapter.open();

        openDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"openDB");
                dbAdapter.updateEmgTable(1,22.9);


            }
        });
        insertEMG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbAdapter.insertEmg(Double.parseDouble(String.valueOf(editText.getText())));
            }
        });
        readEMG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor cursor = dbAdapter.getAllEmg();
                if(cursor.moveToFirst()){
                    do{
                        Log.i("Value   ","" + cursor.getDouble(1));
                    }while(cursor.moveToNext());
                }
            }
        });
        closeDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbAdapter.close();
            }
        });
        deleteDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBAdapter.deleteDataBase(getContext());
            }
        });

    //endregion

        plot = (XYPlot) getActivity().findViewById(R.id.plot);
        Number[] series1Numbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64};


        MyFadeFormatter formatter =new MyFadeFormatter(2000);
        formatter.setLegendIconEnabled(false);
        plot.setRangeBoundaries(0, 1000, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, 50, BoundaryMode.FIXED);

        // reduce the number of range labels
        plot.setLinesPerRangeLabel(3);

    }

    public static <T> List<T> rotate(List<T> aL, int shift) {
        List<T> newValues = new ArrayList<>(aL);
        Collections.rotate(newValues, shift);
        return newValues;
    }

    public void updatePlot(float f1){
        if(list.size()<50)
            list.add(f1);
        else {
            list.remove(0);
            list.add(f1);
        }
        XYSeries series1 = new SimpleXYSeries(
                list, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");
        MyFadeFormatter formatter =new MyFadeFormatter(2000);
        formatter.setLegendIconEnabled(false);
        plot.clear();
        plot.addSeries(series1, formatter);
        plot.redraw();
    }

    @Override
    public void onBackPressed() {
        setIfItIsTriageScreen((MainActivity)getActivity(),true);
        getActivity().getSupportFragmentManager().beginTransaction().
                remove(getActivity().getSupportFragmentManager().findFragmentByTag("MANUAL_ASSESSMENT_FRAGMENT")).commit();
        getActivity().getSupportFragmentManager().popBackStackImmediate();
    }

    public void setIfItIsTriageScreen(MainActivity mA, boolean b){   mA.setIfItIsTriageScreen(b);   }

}
