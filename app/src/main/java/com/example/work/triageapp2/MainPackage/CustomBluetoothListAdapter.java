package com.example.work.triageapp2.MainPackage;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.work.triageapp2.Bluetooth.Device;
import com.example.work.triageapp2.R;

/**
 * Created by BoryS on 20.10.2017.
 */

public class CustomBluetoothListAdapter extends ArrayAdapter {
    private final static String TAG = CustomBluetoothListAdapter.class.getSimpleName();
    private final Activity context;
    private final Device[] devices;
    private Integer imgid;

    public CustomBluetoothListAdapter(Activity context,Device[] devices) {
        super(context,R.layout.list_of_devices,devices);
        this.devices = devices;
        this.context=context;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_of_devices, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.listBluetoothTitleTextView);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView extratxt = (TextView) rowView.findViewById(R.id.listBluetoothExtrasTextView);
        txtTitle.setTextColor(Color.WHITE);
        extratxt.setTextColor(Color.WHITE);

        txtTitle.setText(devices[position].getName());
        if(!devices[position].isFound()){
            imageView.setImageResource(devices[position].notFoundImage);
        }
        else if(devices[position].isPaired()) {

             if (devices[position].isConnected()) {
                imageView.setImageResource(devices[position].connectedImage);
            } else {
                imageView.setImageResource(devices[position].disconnectedImage);
            }
        }else{
            imageView.setImageResource(devices[position].nonPairedImage);
        }

        extratxt.setText("Address: "+devices[position].getAddress());
        return rowView;

    };

}
