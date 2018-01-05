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

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    MainActivity mainActivity;
    public Connection connection;


    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothLeService mBluetoothLeService;
    ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    BluetoothGattCharacteristic mNotifyCharacteristic;

    public String mDeviceAddress;
    public boolean isGattConnected;

    public ServiceConnection getmServiceConnection() {
        return mServiceConnection;
    }

    public final ServiceConnection mServiceConnection = new ServiceConnection() {
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
            mBluetoothLeService.setDbAdapter(mainActivity.dbAdapter);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    public BluetoothManagement(MainActivity mainActivity){
        this.mainActivity = mainActivity;

        initBluetooth();
        initDeviceClock();
    }

    private void initBluetooth(){

        setPermissionForBlueetoothUse();
        setBluetoothAdapter();
        startBluetoothRequest();
        connection = new Connection(this ,mBluetoothAdapter);

    }
    private void initDeviceClock(){
        DeviceConnectionClock deviceConnectionClock = new DeviceConnectionClock();
        deviceConnectionClock.start();
    }

    private void setPermissionForBlueetoothUse() {
        int permissionCheck = ContextCompat.checkSelfPermission(mainActivity,
                Manifest.permission.WRITE_CALENDAR);
        Log.e(TAG, "Permission Status: " + permissionCheck);

        if (ContextCompat.checkSelfPermission(mainActivity.getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(mainActivity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        mainActivity.getResources().getInteger(R.integer.MY_PERMISSIONS_REQUEST_LOCATION));
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

    public void displayGattServices(List<BluetoothGattService> gattServices) {
        //     Log.i(TAG,"displayGattServices");
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
            readAndNotifySelectedCharacteristic();
        }
    }

    public void readAndNotifySelectedCharacteristic(){
        //    Log.i(TAG,"readAndNotifySelectedCharacteristic");
        if (mGattCharacteristics != null) {
            for(ArrayList<BluetoothGattCharacteristic> list : mGattCharacteristics){
                for(BluetoothGattCharacteristic characteristic_temp : list){
                    if(characteristic_temp.getUuid().toString().equals(SampleGattAttributes.HEART_RATE_MEASUREMENT)){
                        readAndNotifyCharacteristic(characteristic_temp);
                    }else if(characteristic_temp.getUuid().toString().equals(SampleGattAttributes.MYOWARE_MUSCLE_SENSOR_CHARACTERISTIC)){
                        readAndNotifyCharacteristic(characteristic_temp);
                    }
                }
            }
        }
    }

    private void readAndNotifyCharacteristic(BluetoothGattCharacteristic characteristic_temp){
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

    public Connection getConnection() {
        return connection;
    }
    public void unbindCurrentService(){mainActivity.unbindService(mServiceConnection);}

    public void printConnectRequest() {
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }
}
