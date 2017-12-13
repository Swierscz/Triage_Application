package com.example.work.triageapp2.ActivitiesAndFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

/**
 * Created by BoryS on 08.12.2017.
 */

public class ManualAssesment {

    Context context;

    public ManualAssesment(Context context) {
        this.context = context;

        Dialog dialog = onCreateDialogSingleChoice();
        dialog.show();

    }

    public Dialog onCreateDialogSingleChoice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        CharSequence[] array = {"5", "4", "3","2","1"};
        builder.setTitle("Oceń swój stan zdrowia")
                .setSingleChoiceItems(array, 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }


}


