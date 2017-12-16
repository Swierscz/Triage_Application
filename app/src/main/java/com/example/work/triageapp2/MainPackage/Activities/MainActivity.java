package com.example.work.triageapp2.MainPackage.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.work.triageapp2.Bluetooth.Ble.BluetoothLeService;
import com.example.work.triageapp2.Bluetooth.Ble.SampleGattAttributes;
import com.example.work.triageapp2.Bluetooth.Connection;
import com.example.work.triageapp2.Bluetooth.DeviceConnectionClock;
import com.example.work.triageapp2.Database.DBAdapter;
import com.example.work.triageapp2.MainPackage.Fragments.CalibrationFragment;
import com.example.work.triageapp2.MainPackage.Fragments.EmgFragment;
import com.example.work.triageapp2.MainPackage.Fragments.OnBackPressedListener;
import com.example.work.triageapp2.MainPackage.ManualAssesment;
import com.example.work.triageapp2.MainPackage.Receivers;
import com.example.work.triageapp2.MainPackage.SoldierAlarm;
import com.example.work.triageapp2.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final static String TAG = MainActivity.class.getSimpleName();

    MainViewBehaviour mainViewBehaviour;
    public Connection connection;
    NavigationView navigationView;
    DBAdapter dbAdapter;
    EmgFragment emgFragment = null;
    DeviceConnectionClock deviceConnectionClock;



    Toolbar toolbar;
    public ImageView disableBluetoothIcon, hrView;
    Button emgButton, triageButton;
    TextView hrText, hrTextLabel;

    Receivers receivers;

    ArrayList<Float> plotList = new ArrayList<Float>();



    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothLeService mBluetoothLeService;
    ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    BluetoothGattCharacteristic mNotifyCharacteristic;

    public String mDeviceAddress;
    public boolean isGattConnected;

    boolean isTriageScreenVisible = true;

    public static final int PLOT_SIZE = 300;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public ServiceConnection getmServiceConnection() {
        return mServiceConnection;
    }

    public final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mBluetoothLeService.setMainActivityReference(MainActivity.this);
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

    //region _____init_____
    private void initViews(){
//        view = (MainActivityDrawingView) findViewById(R.id.mainActivityDrawingViewId);
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

        hrView = (ImageView) findViewById(R.id.hrView);
        hrText = (TextView) findViewById(R.id.hrTextView);
        hrTextLabel = (TextView) findViewById(R.id.hrTextViewLabel);
        triageButton = (Button) findViewById(R.id.triageButton);

        mainViewBehaviour = new MainViewBehaviour(this);
        mainViewBehaviour.start();
    }

    private void initBluetooth(){
        setBluetoothIconVisibility();
        setPermissionForBlueetoothUse();
        setBluetoothAdapter();
        startBluetoothRequest();
    }


    private void initAndHandleEmgButton(){
        emgButton = (Button) findViewById(R.id.emgButton);
        emgButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                if (getSupportFragmentManager().getFragments().size() != 0) {
                    getSupportFragmentManager().popBackStackImmediate();
                }
                setBackgroundComponentVisibility(false);
                setIsTriageScreenVisible(false);
                emgFragment = new EmgFragment();
                replaceFragment(emgFragment, "EMG_FRAGMENT");
            }
        });
    }
    private void initDeviceClockAndReceivers(){
        deviceConnectionClock = new DeviceConnectionClock();
        deviceConnectionClock.start();
        receivers = new Receivers(this);
        receivers.registerReceivers();
    }
    private void initDataBase(){
        dbAdapter = new DBAdapter(getApplicationContext());
        dbAdapter.open();
    }
    private void initObjects(){
        connection = new Connection(this, mBluetoothAdapter);
    }
    //endregion

    //region _____on* Methods_____
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initBluetooth();
        initAndHandleEmgButton();
        initDeviceClockAndReceivers();
        initDataBase();
        initObjects();

    }
    @Override
    protected void onResume() {
        Log.i(TAG,"onResume");
        super.onResume();
//        setBackgroundComponentVisibility(true);
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
        manageDrawerBehaviourAndOnBackPressedFunctionForFragments();
    }

    public void manageDrawerBehaviourAndOnBackPressedFunctionForFragments(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else {
            this.setTitle(R.string.title_activity_main);
            uncheckMenuItems();
            callOnBackPressedOnEachFragment();
        }
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        receivers.unregisterReceivers();
        unbindService(mServiceConnection);
        DBAdapter.deleteDataBase(getApplicationContext());
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

    //    @SuppressLint("RestrictedApi")
    @SuppressLint("RestrictedApi")
    private void displaySelectedScreen(int id) {
        Fragment fragment = null;
        switch (id) {
            case R.id.nav_calibration:
                if (getSupportFragmentManager().getFragments().size() != 0) {
                    getSupportFragmentManager().popBackStackImmediate();
                }
                setBackgroundComponentVisibility(false);
                setIsTriageScreenVisible(false);

                fragment = new CalibrationFragment();
                replaceFragment(fragment, "CALIBRATION_FRAGMENT");
                break;
            case R.id.nav_assesment:
                new ManualAssesment(MainActivity.this);
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




//endregion

    //region _____bluetooth code_____

    public void setBluetoothIconVisibility(){
        if(!isBluetoothEnabled()){
            disableBluetoothIcon.setVisibility(View.VISIBLE);
        }else{
            disableBluetoothIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void startBluetoothRequest() {
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent((BluetoothAdapter.ACTION_REQUEST_ENABLE));
            startActivityForResult(enableBtIntent,((MainActivity)this).getResources().getInteger(R.integer.REQUEST_ENABLE_BT));
            Log.i(TAG,"request enable bluetooth has started");
        }
    }

    private void setBluetoothAdapter() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }


    private void setPermissionForBlueetoothUse() {
        int permissionCheck = ContextCompat.checkSelfPermission((MainActivity) this,
                Manifest.permission.WRITE_CALENDAR);
        Log.e(TAG, "Permission Status: " + permissionCheck);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((MainActivity) this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions((MainActivity) this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        ((MainActivity) this).getResources().getInteger(R.integer.MY_PERMISSIONS_REQUEST_LOCATION));
            }
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

    //endregion

    //region _____emg handle_____
    public void setEmgFragmentToNull(){
        emgFragment = null;
    }

    public EmgFragment getEmgFragment(){
        return emgFragment;
    }

    public void fillPlotValues(float f1){
        if(plotList.size()<PLOT_SIZE)
            plotList.add(f1);
        else {
            plotList.remove(0);
            plotList.add(f1);
        }
    }

    public ArrayList<Float> getPlotList(){
        return plotList;
    }
    //endregion

    //region _____activity and fragments handle_____
    public void setBackgroundComponentVisibility(boolean visible){
        if(emgButton!=null){
            if(visible==true){
                emgButton.setVisibility(View.VISIBLE);
                hrView.setVisibility(View.VISIBLE);
                hrText.setVisibility(View.VISIBLE);
                hrTextLabel.setVisibility(View.VISIBLE);
                triageButton.setVisibility(View.VISIBLE);
            }
            else{
                emgButton.setVisibility(View.GONE);
                hrView.setVisibility(View.GONE);
                hrText.setVisibility(View.GONE);
                hrTextLabel.setVisibility(View.GONE);
                triageButton.setVisibility(View.GONE);
            }

        }
    }
    public void setIfItIsMainScreen(boolean b){
        setIsTriageScreenVisible(b);
        setBackgroundComponentVisibility(b);
    }

    public void setIsTriageScreenVisible(boolean b){
        isTriageScreenVisible = b;
    }

    //endregion


    public Connection getConnection() {
        return connection;
    }
    public void unbindCurrentService(){unbindService(mServiceConnection);}
    //region _____hrView_____
    public ImageView getHrView(){
        return hrView;
    }


    public void setDisabledOrNotHrView(boolean b){
        final boolean tempBool=b;
        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                if(tempBool){
                    hrView.setImageResource(R.drawable.green_heart_hr);
                }else{
                    hrView.setImageResource(R.drawable.green_heart_rate_disabled);
                }
            }
        });
    }

    public void hideOrShowHrView(boolean b){
        final boolean tempBool=b;
        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                if(tempBool){
                    hrView.setVisibility(View.VISIBLE);
                }else{
                    hrView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    //endregion_____
    public void setHr(int hr){
        final int HR = hr;
        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                hrText.setText(String.valueOf(HR));
            }
        });

    }

}
