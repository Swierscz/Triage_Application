package com.example.work.triageapp2.ActivitiesAndFragments;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.work.triageapp2.Bluetooth.Ble.BluetoothLeService;
import com.example.work.triageapp2.Bluetooth.Ble.SampleGattAttributes;
import com.example.work.triageapp2.Database.DBAdapter;
import com.example.work.triageapp2.R;
import com.example.work.triageapp2.AppGraphic.MainActivityDrawingView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final static String TAG = MainActivity.class.getSimpleName();

    NavigationView navigationView;
    MainActivityDrawingView view;
    ImageView disableBluetoothIcon;
    Toolbar toolbar;
    Receiver receiver;
    DBAdapter dbAdapter;
    DeviceConnectionClock deviceConnectionClock;
    ManualAssesment manualAssesment;

    BluetoothLeService mBluetoothLeService;
    ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    BluetoothGattCharacteristic mNotifyCharacteristic;

    String mDeviceAddress;
    boolean isFragmentWorking, isSurfaceCreated, isGattConnected;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            mBluetoothLeService.setDbAdapter(dbAdapter);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    private void initViews(){
        view = (MainActivityDrawingView) findViewById(R.id.mainActivityDrawingViewId);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        disableBluetoothIcon = (ImageView) findViewById(R.id.disableBluetoothIcon);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    //region _____on* Methods_____
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setBluetoothIconVisibility();
        deviceConnectionClock = new DeviceConnectionClock();
        deviceConnectionClock.start();
        receiver = new Receiver(this);
        receiver.registerReceivers();
        dbAdapter = new DBAdapter(getApplicationContext());
        dbAdapter.open();
    }
    @Override
    protected void onResume() {
        Log.i(TAG,"onResume");
        super.onResume();
        dbAdapter.open();
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {Log.i(TAG,"onPause");
        super.onPause();
        dbAdapter.close();
    }

    @Override
    public void onBackPressed() {Log.i(TAG,"onBackPressed");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else {
            this.setTitle(R.string.title_activity_main);
            uncheckMenuItems();
            callOnBackPressedOnEachFragment();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        receiver.unregisterReceivers();
        unbindService(mServiceConnection);
        DBAdapter.deleteDataBase(getApplicationContext());
    }

    private void callOnBackPressedOnEachFragment(){
        @SuppressLint("RestrictedApi") List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            //TODO: Perform your logic to pass back press here
            for (Fragment fragment : fragmentList) {
                if (fragment instanceof OnBackPressedListener) {
                    ((OnBackPressedListener) fragment).onBackPressed();
                }
            }
        }
    }

    //endregion


    //region _____menu and fragments code_____

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        displaySelectedScreen(id);
        return true;
    }

    @SuppressLint("RestrictedApi")
    private void displaySelectedScreen(int id) {
        Fragment fragment = null;
        switch (id) {
            case R.id.nav_calibration:
                if (getSupportFragmentManager().getFragments().size() != 0) {
                    getSupportFragmentManager().popBackStackImmediate();
                }
                view.setIsTriageScreenVisible(false);
                fragment = new Calibration();
                replaceFragment(fragment, "CALIBRATION_FRAGMENT");
                break;
            case R.id.nav_assesment:
                if (getSupportFragmentManager().getFragments().size() != 0) {
                    getSupportFragmentManager().popBackStackImmediate();
                }
                view.setIsTriageScreenVisible(false);
                manualAssesment = new ManualAssesment();
                mBluetoothLeService.setManualAssesment(manualAssesment);
                fragment = manualAssesment;
                replaceFragment(fragment, "MANUAL_ASSESSMENT_FRAGMENT");
                break;
            case R.id.nav_alarm:
                new SoldierAlarm(getApplicationContext());
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void replaceFragment(Fragment fragment,String tag){
        if(fragment != null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame,fragment,tag).addToBackStack(null);
            ft.commit();
        }

    }

    private void uncheckMenuItems(){
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }


    public void setBluetoothIconVisibility(){
        if(!isBluetoothEnabled()){
            disableBluetoothIcon.setVisibility(View.VISIBLE);
        }else{
            disableBluetoothIcon.setVisibility(View.INVISIBLE);
        }
    }

//endregion


    //region _____bluetooth code_____
    public boolean isBluetoothEnabled()
    {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
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
                     //   Log.i(TAG,"readAndNotifySelectedCharacteristicHEART_RATE");
                        readAndNotifyCharacteristic(characteristic_temp);
                    }else if(characteristic_temp.getUuid().toString().equals(SampleGattAttributes.MYOWARE_MUSCLE_SENSOR_CHARACTERISTIC)){
                    //    Log.i(TAG,"readAndNotifySelectedCharacteristic2MYO_WARE");
                        readAndNotifyCharacteristic(characteristic_temp);
                    }
                }
            }
        }
    }

    private void readAndNotifyCharacteristic(BluetoothGattCharacteristic characteristic_temp){
   //     Log.i(TAG,"readAndNotifyCharacteristic");
        BluetoothGattCharacteristic characteristic = null;
        characteristic = characteristic_temp;
        if (characteristic != null) {
            final int charaProp = characteristic.getProperties();
//            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                // If there is an active notification on a characteristic, clear
//                // it first so it doesn't update the data field on the user interface.
//                if (mNotifyCharacteristic != null) {
//                    mBluetoothLeService.setCharacteristicNotification(
//                            mNotifyCharacteristic, false);
//                    mNotifyCharacteristic = null;
//                }
//                //mBluetoothLeService.readCharacteristic(characteristic);
//            }
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mNotifyCharacteristic = characteristic;
                mBluetoothLeService.setCharacteristicNotification(
                        characteristic, true);
            }
        }
    }

    //endregion



    public void setIfItIsTriageScreen(boolean b){   view.setIsTriageScreenVisible(b);    }

    public boolean isSurfaceCreated() {
        return isSurfaceCreated;
    }

    public void setSurfaceCreated(boolean surfaceCreated) {
        isSurfaceCreated = surfaceCreated;
    }

    public boolean isFragmentWorking() {
        return isFragmentWorking;
    }

    public void setFragmentWorking(boolean fragmentWorking) {
        isFragmentWorking = fragmentWorking;
    }
}
