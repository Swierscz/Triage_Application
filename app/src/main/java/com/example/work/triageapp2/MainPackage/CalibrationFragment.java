package com.example.work.triageapp2.MainPackage;

import android.bluetooth.BluetoothDevice;
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

import com.example.work.triageapp2.Bluetooth.Connection;
import com.example.work.triageapp2.Bluetooth.CustomBluetoothListAdapter;
import com.example.work.triageapp2.Bluetooth.Device;
import com.example.work.triageapp2.Database.DBAdapter;
import com.example.work.triageapp2.R;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by BoryS on 06.08.2017.
 */

public class CalibrationFragment extends Fragment implements OnBackPressedListener, IfMainScreenCheck {
    private final static String TAG = CalibrationFragment.class.getSimpleName();
    Connection connection;
    private ListView listOfDevices;
    private ArrayAdapter<String> adapter;

    private final BroadcastReceiver listRefreshReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("REFRESH_DEVICE_LIST")) {
                refreshDeviceListViewAndSetListener();
            }
        }
    };


    public CalibrationFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calibration, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("CalibrationFragment");
        Log.i(TAG, "Constructor");

        connection = ((MainActivity) getActivity()).bluetoothManagement.getConnection();
        listOfDevices = (ListView) getActivity().findViewById(R.id.bluetoothDevicesList);

        ((MainActivity) getActivity()).registerReceiver(listRefreshReceiver, new IntentFilter("REFRESH_DEVICE_LIST"));
        setHasOptionsMenu(true);
        refreshDeviceListViewAndSetListener();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        //super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_bluetooth_settings:
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivityForResult(intent, getActivity().getResources().getInteger(R.integer.REQUEST_BLUETOOTH_SETTINGS));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        ((MainActivity) getActivity()).unregisterReceiver(listRefreshReceiver);
        setIfItIsMainScreen((MainActivity) getActivity(), true);
        getActivity().getSupportFragmentManager().beginTransaction().
                remove(getActivity().getSupportFragmentManager().findFragmentByTag("CALIBRATION_FRAGMENT")).commit();
        getActivity().getSupportFragmentManager().popBackStackImmediate();
    }


    public void refreshDeviceListViewAndSetListener() {
        Log.i(TAG, "refreshDeviceListViewAndSetListener function has started");

        for(Device d : connection.listOfAllDevices){
            if(d.deviceKind.equals("CLASSIC")){
                if(isListOfDevicesContainAddress(connection.getmBluetoothAdapter().getBondedDevices(),d.deviceAddress)){
                    d.setPaired(true);
                }else{
                    d.setPaired(false);
                }
            }
        }

        setListAdapter(connection.listOfAllDevices);

        listOfDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, "item has been clicked");

                if (((MainActivity) getActivity()).bluetoothManagement.mBluetoothAdapter != null && ((MainActivity) getActivity()).bluetoothManagement.mBluetoothAdapter.isEnabled()) {
                    startOrStopConnection(i);
                }
            }
        });
    }

    public boolean isListOfDevicesContainAddress(Set<BluetoothDevice> list, String address){
        for(BluetoothDevice d : list){
            if(d.getAddress().equals(address)){
                return true;
            }
        }
        return false;
    }

    public void startOrStopConnection(int i) {
        for (Device dC : connection.listOfAllDevices) {
            if (listOfDevices.getItemAtPosition(i).equals(dC)) {
                if(dC.isConnected()) {
                    if (dC.deviceKind.equals("LE")) {
                        if (dC.isConnected()) {
                            ((MainActivity) getActivity()).bluetoothManagement.mBluetoothLeService.disconnect(dC.deviceAddress);
                            refreshDeviceListViewAndSetListener();
//                        ((MainActivity) getActivity()).unbindService(((MainActivity) getActivity()).bluetoothManagement.mServiceConnection);
                        } else {
//                        ((MainActivity) getActivity()).bluetoothManagement.connection.bindLEService(dC);
                            ((MainActivity) getActivity()).bluetoothManagement.mBluetoothLeService.connect(dC.deviceAddress);
                            refreshDeviceListViewAndSetListener();
                        }
                    } else if (dC.deviceKind.equals("CLASSIC")) {
                        if (dC.isConnected()) {
                            ((MainActivity) getActivity()).bluetoothManagement.connection.classicConnection.closeSocket();
                        } else {
                            ((MainActivity) getActivity()).bluetoothManagement.connection.createClassicConnection(dC);
                        }
                    }
                }
            }
        }
    }

    public void setListAdapter(ArrayList<Device> deviceList) {
        Log.i(TAG, "setListAdapter function has started");
        Device[] devices = deviceList.toArray(new Device[deviceList.size()]);
        adapter = new CustomBluetoothListAdapter(getActivity(), devices);
        listOfDevices.setAdapter(adapter);
    }

}
