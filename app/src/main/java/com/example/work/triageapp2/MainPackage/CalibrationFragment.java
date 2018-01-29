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
import com.example.work.triageapp2.Bluetooth.Device;
import com.example.work.triageapp2.R;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by BoryS on 06.08.2017.
 */

public class CalibrationFragment extends Fragment implements OnBackPressedListener, IfMainScreenCheck {
    private final static String TAG = CalibrationFragment.class.getSimpleName();
    public final static String REFRESH_DEVICE_LIST_EVENT = "REFRESH_DEVICE_LIST_EVENT";

    private Connection connection;
    private ListView listOfDevices;
    private ArrayAdapter<String> adapter;

    private final BroadcastReceiver listRefreshReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(REFRESH_DEVICE_LIST_EVENT)) {
                setListAdapter();
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

        connection = ((MainActivity) getActivity()).getBluetoothManagement().getConnection();
        listOfDevices = (ListView) getActivity().findViewById(R.id.bluetoothDevicesList);
        ((MainActivity) getActivity()).registerReceiver(listRefreshReceiver, new IntentFilter(REFRESH_DEVICE_LIST_EVENT));
        setHasOptionsMenu(true);
        setListAdapter();
        setListenerForDeviceList();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.app_bar_content, menu);
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

    private void setListenerForDeviceList() {
        setListAdapter();

        listOfDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                connection.stopDeviceConnectionClock();
                startOrStopConnectionToItemAtSpecifiedPosition(i);
                connection.startDeviceConnectionClock();
            }
        });
    }

    private void setListAdapter(){
        setPairingStatusByBondedDevicesList();
        setListAdapter(connection.listOfAllDevices);
    }

    private void setPairingStatusByBondedDevicesList(){
        for(Device d : connection.listOfAllDevices){
            if(d.getKind().equals(Connection.TYPE_CLASSIC)){
                if(isListOfDevicesContainAddress(connection.getmBluetoothAdapter().getBondedDevices(),d.getAddress())){
                    d.setPaired(true);
                }else{
                    d.setPaired(false);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        ((MainActivity) getActivity()).unregisterReceiver(listRefreshReceiver);
        setIfItIsMainScreen((MainActivity) getActivity(), true);
        getActivity().getSupportFragmentManager().beginTransaction().
                remove(getActivity().getSupportFragmentManager().findFragmentByTag("CALIBRATION_FRAGMENT")).commit();
        getActivity().getSupportFragmentManager().popBackStackImmediate();
    }

    private boolean isListOfDevicesContainAddress(Set<BluetoothDevice> list, String address){
        for(BluetoothDevice d : list){
            if(d.getAddress().equals(address)){
                return true;
            }
        }
        return false;
    }

    private void startOrStopConnectionToItemAtSpecifiedPosition(int itemPosition) {
        if (((MainActivity) getActivity()).getBluetoothManagement().mBluetoothAdapter != null && ((MainActivity) getActivity()).getBluetoothManagement().mBluetoothAdapter.isEnabled()) {
            for (Device dC : connection.listOfAllDevices) {
                if (listOfDevices.getItemAtPosition(itemPosition).equals(dC)) {
                   if (dC.getKind().equals(Connection.TYPE_LE)) {
                       connectOrDisconnectWithLEDevice(dC);
                   } else if (dC.getKind().equals(Connection.TYPE_CLASSIC)) {
                       connectOrDisconnectWithClassicDevice(dC);
                   }
                }
            }
        }
    }

    private void connectOrDisconnectWithLEDevice(Device dC){
        if (dC.isConnected()) {
            ((MainActivity) getActivity()).getBluetoothManagement().getmBluetoothLeService().disconnect(dC.getAddress());
            setListAdapter();
//          ((MainActivity) getActivity()).unbindService(((MainActivity) getActivity()).getBluetoothManagement().mServiceConnection);
        } else {
//          ((MainActivity) getActivity()).getBluetoothManagement().connection.bindLEService(dC);
            ((MainActivity) getActivity()).getBluetoothManagement().getmBluetoothLeService().connect(dC.getAddress());
            setListAdapter();
        }
    }

    private void connectOrDisconnectWithClassicDevice(Device dC){
        if (dC.isConnected()) {
            ((MainActivity) getActivity()).getBluetoothManagement().getConnection().getClassicConnection().closeSocket();
        } else {
            ((MainActivity) getActivity()).getBluetoothManagement().getConnection().createClassicConnection(dC);
        }
    }

    private void setListAdapter(ArrayList<Device> deviceList) {
        Log.i(TAG, "setListAdapter function has started");
        Device[] devices = deviceList.toArray(new Device[deviceList.size()]);
        adapter = new CustomBluetoothListAdapter(getActivity(), devices);
        listOfDevices.setAdapter(adapter);
    }

}
