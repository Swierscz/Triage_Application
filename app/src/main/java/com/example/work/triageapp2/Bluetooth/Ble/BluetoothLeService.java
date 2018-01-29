/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.work.triageapp2.Bluetooth.Ble;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.work.triageapp2.Bluetooth.Connection;
import com.example.work.triageapp2.Bluetooth.Device;
import com.example.work.triageapp2.MainPackage.CalibrationFragment;
import com.example.work.triageapp2.MainPackage.DataStorage;
import com.example.work.triageapp2.MainPackage.EmgFragment;
import com.example.work.triageapp2.MainPackage.MainActivity;
import com.example.work.triageapp2.Bluetooth.StatusConnectionClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends IntentService {
    private final static String TAG = BluetoothLeService.class.getSimpleName();


    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private DataStorage dataStorage;
    private MainActivity mainActivity;
    public void setMainActivityReference(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    private  Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<BluetoothGattDescriptor>();

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    public final static UUID UUID_MYOWARE_MUSCLE_MEASURMENT =
            UUID.fromString(SampleGattAttributes.MYOWARE_MUSCLE_SENSOR_CHARACTERISTIC);

    int emgRowCount = 1;
    boolean isEmgDataWrittenToDataBase = false;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);

                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
                for(Device d : Connection.listOfAllDevices){
                    if(device.getAddress().equals(d.getAddress())){
                        d.setConnected(true);
                        Intent intent1 = new Intent();
                        intent1.setAction(CalibrationFragment.REFRESH_DEVICE_LIST_EVENT);
                        mainActivity.sendBroadcast(intent1);
                    }
                }

                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;

                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
                for(Device d : Connection.listOfAllDevices){
                    if(device.getAddress().equals(d.getAddress())){
                        d.setConnected(false);
                        Intent intent1 = new Intent();
                        intent1.setAction(CalibrationFragment.REFRESH_DEVICE_LIST_EVENT);
                        mainActivity.sendBroadcast(intent1);
                    }}

                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt,descriptor,status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Callback: Wrote GATT Descriptor successfully.");
            }
            else{
                Log.d(TAG, "Callback: Error writing GATT Descriptor: "+ status);
            }
            descriptorWriteQueue.remove();
            //if there is more to write, do it!
            if(descriptorWriteQueue.size() > 0){
                mBluetoothGatt.writeDescriptor(descriptorWriteQueue.element());
            }
        }

    };

    public BluetoothLeService(){
        super("BluetoothLeService");
        dataStorage = DataStorage.getInstance();

    }

    public BluetoothLeService(String name) {
        super(name);
        dataStorage = DataStorage.getInstance();
    }

    //region _____broadcast update_____

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int heartRate = formatIncomingDataForHeartRate(characteristic);
            dataStorage.setCurrentHeartRate(heartRate);
            Log.i(TAG,"heartRate is: " + String.valueOf(heartRate) );
            dataStorage.addHeartRateValue(heartRate);
            mainActivity.setHr(heartRate);
            StatusConnectionClock.resetTimerForHeartRate();
            if(StatusConnectionClock.isHeartRateActive == false)
                StatusConnectionClock.isHeartRateActive = true;
        }
        else if(UUID_MYOWARE_MUSCLE_MEASURMENT.equals(characteristic.getUuid())){

            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
            }
            final int muscleRate = characteristic.getIntValue(format, 1);
            EmgFragment.fillPlotValues(muscleRate);
            if(mainActivity.getEmgFragment()!=null){
                mainActivity.getEmgFragment().refreshPlot();
            }
        }
        else
        {
            String s  = formatIncomingDataForOtherDevices(characteristic);
            Log.i(TAG,s);
        }

        sendBroadcast(intent);
    }


    //region _____standard service functions_____
    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }

        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect(String address) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }


//endregion

    //region _____set notification and descriptor_____
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        if (checkIfItIsNecessarryCharacteristic(characteristic)){
            writeGattDescriptor(characteristic);
        }
    }

    public void writeGattDescriptor(BluetoothGattCharacteristic characteristic){
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        //put the descriptor into the write queue
        descriptorWriteQueue.add(descriptor);
        //if there is only 1 item in the queue, then write it.  If more than 1, we handle asynchronously in the callback above
        if(descriptorWriteQueue.size() == 1){
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }
    private boolean checkIfItIsNecessarryCharacteristic(BluetoothGattCharacteristic characteristic){
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid()))       {   return true;    }
        if(UUID_MYOWARE_MUSCLE_MEASURMENT.equals(characteristic.getUuid()) )    {   return true;    }
        return false;
    }
    //endregion

    //region _____format functions_____
    private int formatIncomingDataForHeartRate(BluetoothGattCharacteristic characteristic){
        int flag = characteristic.getProperties();
        int format = -1;
        if ((flag & 0x01) != 0) {
            format = BluetoothGattCharacteristic.FORMAT_UINT16;
        } else {
            format = BluetoothGattCharacteristic.FORMAT_UINT8;
        }
        final int heartRate = characteristic.getIntValue(format, 1);
        return heartRate;
    }


    private String formatIncomingDataForOtherDevices(BluetoothGattCharacteristic characteristic){
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));

            return new String(data) + "\n" + stringBuilder.toString();
            //   intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }
        return "";
    }

    private float formatIncomingDataForMuscleDevice(BluetoothGattCharacteristic characteristic) {
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            float f1 = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            return f1;
        }
        return 0;
    }
    //endregion

    //region ______DataStorage functions_____s


//    public void addEmgToDatabase(double f1, int size){
//        if(!isEmgDataWrittenToDataBase){
//            dbAdapter.insertEmg(f1);
//            emgRowCount++;
//            if(emgRowCount == size) {
//                isEmgDataWrittenToDataBase = true;
//                emgRowCount = 1;
//            }
//        }else{
//            dbAdapter.updateEmgTable(emgRowCount,f1);
//            emgRowCount++;
//            if(emgRowCount == size){
//                emgRowCount = 1;
//            }
//        }
//    }
//////endregion
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
