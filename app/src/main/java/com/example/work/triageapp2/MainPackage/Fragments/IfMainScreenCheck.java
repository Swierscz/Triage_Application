package com.example.work.triageapp2.MainPackage.Fragments;

import com.example.work.triageapp2.MainPackage.Activities.MainActivity;

/**
 * Created by BoryS on 16.12.2017.
 */

public interface IfMainScreenCheck {
    public default void setIfItIsMainScreen(MainActivity mA, boolean b){   mA.setIfItIsMainScreen(b);  }

}
