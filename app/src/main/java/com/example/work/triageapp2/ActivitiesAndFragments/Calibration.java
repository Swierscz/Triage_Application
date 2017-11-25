package com.example.work.triageapp2.ActivitiesAndFragments;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.example.work.triageapp2.Bluetooth.OtherBluetoothStuff.Device;
import com.example.work.triageapp2.OwnAppObjects.CustomBluetoothListAdapter;
import com.example.work.triageapp2.R;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by BoryS on 06.08.2017.
 */

public class Calibration extends Fragment implements OnBackPressedListener{

//    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 101;
//    private static final int REQUEST_PAIR_DEVICE = 102 ;
//    private static final int REQUEST_ENABLE_BT = 103;
    Connection connection;
    private ListView listOfDevices;
    private ArrayAdapter<String> adapter;

    //region _____receiver_____

    private final BroadcastReceiver bluetoothOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF) {
                    //((MainActivity)getActivity()).unbindCurrentService();
                    final Intent intent2 = new Intent("LIST_REFRESH");
                    getContext().sendBroadcast(intent2);
                }
            }
        }
    };

    private final BroadcastReceiver listRefreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("LIST_REFRESH")) {
                refreshDeviceListViewAndSetListener();
            }
        }
    };

//endregion

    public Calibration(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calibration,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Calibration");
        Log.i(TAG,"Constructor");
        ((MainActivity)getActivity()).setFragmentWorking(true);

        connection = ((MainActivity)getActivity()).getConnection();

        listOfDevices = (ListView) getActivity().findViewById(R.id.bluetoothDevicesList);

        setHasOptionsMenu(true);
        refreshDeviceListViewAndSetListener();
        createIntentFilter();
    }

    public void createIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("LIST_REFRESH");
        getActivity().registerReceiver(bluetoothOffReceiver,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        getActivity().registerReceiver(listRefreshReceiver,intentFilter);
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
        getActivity().unregisterReceiver(bluetoothOffReceiver);
        ((MainActivity)getActivity()).setFragmentWorking(false);
        setIfItIsTriageScreen((MainActivity)getActivity(),true);
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
                    while (((MainActivity)getActivity()).isSurfaceCreated() == true){} //do nothing
                    setIfItIsTriageScreen((MainActivity) getActivity(), false);
                }
            };

            thread.start();
            refreshDeviceListViewAndSetListener();
        }

//        if(requestCode == getActivity().getResources().getInteger(R.integer.REQUEST_ENABLE_BT)){
//            Log.i(TAG,"enable bluetooth request has been received");
//            if(resultCode == RESULT_OK)
//                refreshDeviceListViewAndSetListener();
//        }
    }


    public void refreshDeviceListViewAndSetListener(){
        Log.i(TAG,"refreshconnection.listOfPairedDevicesViewAndSetListener function has started");
        connection.listOfPairedDevices = connection.fillAndReturnPairedDeviceList();

        setListAdapter(connection.listOfPairedDevices);

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
        for(Device dC : connection.listOfPairedDevices){
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


    public void setIfItIsTriageScreen(MainActivity mA, boolean b){   mA.setIfItIsTriageScreen(b);   }
}
