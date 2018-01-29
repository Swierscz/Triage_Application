package com.example.work.triageapp2.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.work.triageapp2.Bluetooth.Ble.BluetoothLeService;
import com.example.work.triageapp2.Bluetooth.Ble.SampleGattAttributes;
import com.example.work.triageapp2.MainPackage.MainActivity;
import com.example.work.triageapp2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BoryS on 16.12.2017.
 */

public class BluetoothManagement {
    final static String TAG = BluetoothManagement.class.getSimpleName();

    public BluetoothAdapter mBluetoothAdapter;
    private MainActivity mainActivity;
    private Connection connection;
    private BluetoothLeService mBluetoothLeService;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private String mDeviceAddress;
    private boolean isGattConnected;


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mBluetoothLeService.setMainActivityReference(mainActivity);
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                mainActivity.finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    public BluetoothManagement(MainActivity mainActivity){
        this.mainActivity = mainActivity;

        initBluetooth();
        startDeviceClock();
    }

    private void initBluetooth(){

        setPermissionForBluetoothUse();
        setBluetoothAdapter();
        startBluetoothRequest();
        connection = new Connection(this ,mBluetoothAdapter);
        getmBluetoothAdapter().startDiscovery();

    }
    private void startDeviceClock(){
        StatusConnectionClock statusConnectionClock = new StatusConnectionClock();
        statusConnectionClock.start();
    }

    private void setPermissionForBluetoothUse() {
        int permissionCheck = ContextCompat.checkSelfPermission(mainActivity,
                Manifest.permission.WRITE_CALENDAR);
        Log.e(TAG, "Permission Status: " + permissionCheck);

        if (ContextCompat.checkSelfPermission(mainActivity.getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(mainActivity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        mainActivity.getResources().getInteger(R.integer.MY_PERMISSIONS_LOCATION));
            }
        }
    }

    private void setBluetoothAdapter() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mainActivity.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }


    private void startBluetoothRequest() {
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent((BluetoothAdapter.ACTION_REQUEST_ENABLE));
            mainActivity.startActivityForResult(enableBtIntent,mainActivity.getResources().getInteger(R.integer.REQUEST_ENABLE_BT));
            Log.i(TAG,"request enable bluetooth has started");
        }
    }


    public boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }

    public void readCharacteristicsFromServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
            }
            mGattCharacteristics.add(charas);
            readAndNotifyCharacteristics();
        }
    }

    private void readAndNotifyCharacteristics(){
        if (mGattCharacteristics != null) {
            for(ArrayList<BluetoothGattCharacteristic> list : mGattCharacteristics){
                for(BluetoothGattCharacteristic characteristic_temp : list){
                    if(characteristic_temp.getUuid().toString().equals(SampleGattAttributes.HEART_RATE_MEASUREMENT)){
                        readAndNotifySelectedCharacteristic(characteristic_temp);
                    }else if(characteristic_temp.getUuid().toString().equals(SampleGattAttributes.MYOWARE_MUSCLE_SENSOR_CHARACTERISTIC)){
                        readAndNotifySelectedCharacteristic(characteristic_temp);
                    }
                }
            }
        }
    }

    private void readAndNotifySelectedCharacteristic(BluetoothGattCharacteristic characteristic_temp){
        BluetoothGattCharacteristic characteristic = null;
        characteristic = characteristic_temp;
        if (characteristic != null) {
            final int charaProp = characteristic.getProperties();

            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mNotifyCharacteristic = characteristic;
                mBluetoothLeService.setCharacteristicNotification(
                        characteristic, true);
            }
        }
    }

    public void unbindCurrentWorkingService(){
         mainActivity.unbindService(mServiceConnection);}

    public void printConnectRequest() {
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public String getmDeviceAddress() {
        return mDeviceAddress;
    }

    public boolean isGattConnected() {
        return isGattConnected;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setGattConnected(boolean gattConnected) {
        isGattConnected = gattConnected;
    }

    public BluetoothLeService getmBluetoothLeService() {
        return mBluetoothLeService;
    }

    public void setmDeviceAddress(String mDeviceAddress) {
        this.mDeviceAddress = mDeviceAddress;
    }

    public ServiceConnection getmServiceConnection() {
        return mServiceConnection;
    }

}
