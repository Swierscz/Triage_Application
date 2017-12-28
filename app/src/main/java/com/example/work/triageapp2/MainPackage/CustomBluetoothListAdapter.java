package com.example.work.triageapp2.MainPackage;

import android.app.Activity;
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

    private final Activity context;
    private final Device[] devices;
    private Integer imgid;

    public CustomBluetoothListAdapter(Activity context,Device[] devices) {
        super(context,R.layout.my_list,devices);
        this.devices = devices;
        this.context=context;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.my_list, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.listBluetoothTitleTextView);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView extratxt = (TextView) rowView.findViewById(R.id.listBluetoothExtrasTextView);

        txtTitle.setText(devices[position].deviceName);
        if(devices[position].isConnected()){
            imageView.setImageResource(devices[position].connectedImage);
        }else{
            imageView.setImageResource(devices[position].disconnectedImage);
        }

        extratxt.setText("Address: "+devices[position].deviceAddress);
        return rowView;

    };

}
