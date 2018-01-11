package com.example.work.triageapp2.MainPackage;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.work.triageapp2.Database.DBAdapter;
import com.example.work.triageapp2.Bluetooth.BluetoothManagement;
import com.example.work.triageapp2.R;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final static String TAG = MainActivity.class.getSimpleName();

    private MainViewBehaviour mainViewBehaviour;
    private DBAdapter dbAdapter;
    private EmgFragment emgFragment = null;
    private BluetoothManagement bluetoothManagement;
    private Receivers receivers;
    private DataStorage dataStorage;

    private Toolbar toolbar;
    private NavigationView navigationView;
    private Button emgButton, triageButton;
    private TextView hrText, hrTextLabel;
    public  ImageView disableBluetoothIcon, hrView;


    private boolean isTriageScreenVisible = true;

    //region _____init_____
    private void initViews(){
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
    }

    private void initComponents(){
        mainViewBehaviour = new MainViewBehaviour(this);
        mainViewBehaviour.start();
        bluetoothManagement = new BluetoothManagement(this);
        dataStorage = DataStorage.getInstance();
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
    private void initReceivers() {
        receivers = new Receivers(this);
        receivers.registerReceivers();
    }
    private void initDataBase(){
        dbAdapter = DBAdapter.getInstance();
        dbAdapter.open(getApplicationContext());
    }

    //endregion

    //region _____on* Methods_____
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initComponents();
        initAndHandleEmgButton();
        initReceivers();
        initDataBase();
        setBluetoothIconVisibility();

    }

    @Override
    protected void onResume() {
        Log.i(TAG,"onResume");
        super.onResume();
        dbAdapter.open(getApplicationContext());
        bluetoothManagement.printConnectRequest();
    }

    @Override
    protected void onPause() {
        Log.i(TAG,"onPause");
        super.onPause();
        dbAdapter.close();
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG,"onBackPressed");
        manageDrawerBehaviourAndOnBackPressedFunctionForFragments();
    }

    private void manageDrawerBehaviourAndOnBackPressedFunctionForFragments(){
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
        unregisterReceiver(bluetoothManagement.getConnection().getDeviceAndDiscoveryStatusReceiver());
        bluetoothManagement.unbindCurrentWorkingService();
        dbAdapter.deleteDataBase(getApplicationContext());
    }

    //endregion

    //region _____menu and fragments code_____

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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
                setBackgroundComponentVisibility(false);
                setIsTriageScreenVisible(false);
                fragment = new CalibrationFragment();
                replaceFragment(fragment, "CALIBRATION_FRAGMENT");
                break;
            case R.id.nav_assesment:
                new ManualAssessment(MainActivity.this);
                break;

            case R.id.nav_alarm:
                new SoldierAlarm(getApplicationContext());
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void replaceFragment(Fragment fragment,String tag){
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

    //region _____emg handle_____
    public void setEmgFragmentToNull(){
        emgFragment = null;
    }

    public EmgFragment getEmgFragment(){
        return emgFragment;
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

    //region _____hrView_____
    public ImageView getHrView(){
        return hrView;
    }


    public void setIsHrViewHasWholeHeartImage(boolean b){
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

    public void setIsHrViewShouldBeVisible(boolean b){
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

    public void setBluetoothIconVisibility(){
        if(!bluetoothManagement.isBluetoothEnabled()){
            disableBluetoothIcon.setVisibility(View.VISIBLE);
        }else{
            disableBluetoothIcon.setVisibility(View.INVISIBLE);
        }
    }

    public boolean isTriageScreenVisible() {
        return isTriageScreenVisible;
    }

    public BluetoothManagement getBluetoothManagement() {
        return bluetoothManagement;
    }

}
