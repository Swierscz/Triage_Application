package com.example.work.triageapp2.ActivitiesAndFragments;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by BoryS on 06.08.2017.
 */

public class SoldierAlarm {
    Context context;

    public SoldierAlarm(Context context) {
        this.context = context;
        Toast.makeText(context, "Alarm został wysłany", Toast.LENGTH_SHORT).show();
    }

    }