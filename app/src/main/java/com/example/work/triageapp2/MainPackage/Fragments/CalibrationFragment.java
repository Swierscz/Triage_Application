package com.example.work.triageapp2.MainPackage.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.work.triageapp2.Bluetooth.Connection;
import com.example.work.triageapp2.Bluetooth.Device;
import com.example.work.triageapp2.CustomObjects.CustomBluetoothListAdapter;
import com.example.work.triageapp2.MainPackage.Activities.MainActivity;
import com.example.work.triageapp2.R;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by BoryS on 06.08.2017.
 */

public class CalibrationFragment extends Fragment implements OnBackPressedListener,CheckIfMainScreen {

    Connection connection;
    private ListView listOfDevices;
    private ArrayAdapter<String> adapter;

    public CalibrationFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calibration,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("CalibrationFragment");
        Log.i(TAG,"Constructor");

        connection = ((MainActivity)getActivity()).getConnection();
        listOfDevices = (ListView) getActivity().findViewById(R.id.bluetoothDevicesList);

        setHasOptionsMenu(true);
        refreshDeviceListViewAndSetListener();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main,menu);
        //super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_bluetooth_settings:
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivityForResult(intent, getActivity().getResources().getInteger(R.integer.REQUEST_BLUETOOTH_SETTINGS));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setIfItIsMainScreen((MainActivity)getActivity(),true);
        getActivity().getSupportFragmentManager().beginTransaction().
                remove(getActivity().getSupportFragmentManager().findFragmentByTag("CALIBRATION_FRAGMENT")).commit();
        getActivity().getSupportFragmentManager().popBackStackImmediate();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == getActivity().getResources().getInteger(R.integer.REQUEST_BLUETOOTH_SETTINGS)) {
            Log.i(TAG,"bluetooth settings request has been received");
            Thread thread = new Thread(){
                public void run(){
//                    while (((MainActivity)getActivity()).isSurfaceCreated() == true){} //do nothing
//                    setIfItIsMainScreen((MainActivity) getActivity(), false);
                }
            };

            thread.start();
        }

    }


    public void refreshDeviceListViewAndSetListener(){
        Log.i(TAG,"refreshDeviceListViewAndSetListener function has started");
        connection.listOfDevices = connection.listOfDevices;
        setListAdapter(connection.listOfDevices);

        listOfDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG,"item has been clicked");

                if(((MainActivity)getActivity()).mBluetoothAdapter!= null && ((MainActivity)getActivity()).mBluetoothAdapter.isEnabled()){
                    startOrStopSensor(i);
                }
            }
        });
    }

    public void startOrStopSensor(int i){
        for(Device dC : connection.listOfDevices){
            if(listOfDevices.getItemAtPosition(i).equals(dC)){

                if(!connection.isDeviceConnected(dC))
                    connection.checkDeviceKindAndLaunchResponsibleThread(dC);
                else{
                    ((MainActivity)getActivity()).unbindCurrentService();
                }

            }
        }
    }

    public void setListAdapter(ArrayList<Device> deviceList){
        Log.i(TAG,"setListAdapter function has started");
        Device[] devices = deviceList.toArray(new Device[deviceList.size()]);
        adapter = new CustomBluetoothListAdapter(getActivity(),devices);
        listOfDevices.setAdapter(adapter);
    }

}
