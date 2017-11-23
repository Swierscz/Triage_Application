package com.example.work.triageapp2.ActivitiesAndFragments;

import android.Manifest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.example.work.triageapp2.Bluetooth.Classic.ClassicConnection;
import com.example.work.triageapp2.Bluetooth.PairedDevicesThread;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

/**
 * Created by BoryS on 06.08.2017.
 */

public class Calibration extends Fragment implements OnBackPressedListener{

//    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 101;
//    private static final int REQUEST_PAIR_DEVICE = 102 ;
//    private static final int REQUEST_ENABLE_BT = 103;

    PairedDevicesThread pairedDevicesThread;


    private BluetoothAdapter mBluetoothAdapter;


    private ListView listOfDevices;
    private ArrayList<Device> deviceList = new ArrayList<Device>();
    private ArrayAdapter<String> adapter;

    //region _____receiver_____
    private final BroadcastReceiver deviceConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.i(TAG, device.getName() + "    Bluetooth Device Connected");
                for (Device dC : deviceList) {

                    if (device.getName().equals(dC.deviceName) && device.getAddress().equals(dC.deviceAddress)) {
                        dC.setConnected(true);
                        setListAdapter(deviceList);
                    }
                }


            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.i(TAG, device.getName() + "    Bluetooth Device Disconnected");
                for (Device dC : deviceList) {
                    if (device.getName().equals(dC.deviceName) && device.getAddress().equals(dC.deviceAddress)) {
                        dC.setConnected(false);
                        setListAdapter(deviceList);
                    }

                }
            }
        }
    };
    private final BroadcastReceiver bluetoothOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF) {
                    for (Device dCC : deviceList) {
                        dCC.setConnected(false);
                    }
                    setListAdapter(deviceList);
                }
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

        //region COMPONENTS
        listOfDevices = (ListView) getActivity().findViewById(R.id.bluetoothDevicesList);
        //endregion
        // region SETTINGS
        setHasOptionsMenu(true);
        //endregion
        //region PERMISSIONS
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_CALENDAR);
        Log.e(TAG, "Permission Status: " + permissionCheck);

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        getActivity().getResources().getInteger(R.integer.MY_PERMISSIONS_REQUEST_LOCATION));
            }
        }
        //endregion

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent((BluetoothAdapter.ACTION_REQUEST_ENABLE));
            startActivityForResult(enableBtIntent,getActivity().getResources().getInteger(R.integer.REQUEST_ENABLE_BT));
            Log.i(TAG,"request enable bluetooth has started");
        }else{
            refreshDeviceListViewAndSetListener();
        }

        createIntentFilter();

    }

    public void createIntentFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(deviceConnectionReceiver,filter);
        getActivity().registerReceiver(bluetoothOffReceiver,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
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
        getActivity().unregisterReceiver(deviceConnectionReceiver);
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
        if(requestCode == getActivity().getResources().getInteger(R.integer.REQUEST_ENABLE_BT)){
            Log.i(TAG,"enable bluetooth request has been received");
            if(resultCode == RESULT_OK)
                refreshDeviceListViewAndSetListener();
        }
    }


    public void refreshDeviceListViewAndSetListener(){
        Log.i(TAG,"refreshDeviceListViewAndSetListener function has started");
        deviceList = fillDeviceList(mBluetoothAdapter);

        setListAdapter(deviceList);

        listOfDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG,"item has been clicked");
                if(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()){
                    startSensorWorkOnItemClick(i);
                }

            }
        });
    }

    public void setListAdapter(ArrayList<Device> deviceList){
        Log.i(TAG,"setListAdapter function has started");
        Device[] devices = deviceList.toArray(new Device[deviceList.size()]);
        adapter = new CustomBluetoothListAdapter(getActivity(),devices);
        listOfDevices.setAdapter(adapter);
    }

    public ArrayList<Device> fillDeviceList(BluetoothAdapter mBluetoothAdapter){
        Log.i(TAG,"fillDeviceList function has started");
        ArrayList<Device> tempArrayList = new ArrayList<Device>();
        pairedDevicesThread = new PairedDevicesThread(mBluetoothAdapter);
        pairedDevicesThread.start();

        while(!pairedDevicesThread.isDone()) {
            tempArrayList = pairedDevicesThread.getDeviceList();
        }

        return tempArrayList;
    }

    public void startSensorWorkOnItemClick(int i){

        for(Device dC : deviceList){
            if(listOfDevices.getItemAtPosition(i).equals(dC)){
                checkDeviceKindAndLaunchResponsibleThread(dC);
            }
        }
    }

    public void checkDeviceKindAndLaunchResponsibleThread(Device dC){
        if(dC.deviceKind.equals("CLASSIC")){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ClassicConnection connectionObject = new ClassicConnection(mBluetoothAdapter, dC.deviceName, dC.deviceAddress);
            connectionObject.start();
        }else if(dC.deviceKind.equals("DUAL")){
            Log.e(this.getClass().getName() + "","The type of device is DUAL");
        }else if(dC.deviceKind.equals("LE")){

            final Intent intent = new Intent("BLE_DEVICE_NAME_AND_ADDRESS");
            intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, dC.deviceName);
            intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, dC.deviceAddress);
            getActivity().sendBroadcast(intent);

        }else if(dC.deviceKind.equals("UNKNOWN")){
            Log.e(this.getClass().getName() + "","The type of device is not known");
        }
    }

    public void setIfItIsTriageScreen(MainActivity mA, boolean b){   mA.setIfItIsTriageScreen(b);   }
}
