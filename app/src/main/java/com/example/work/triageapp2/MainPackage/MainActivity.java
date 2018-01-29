package com.example.work.triageapp2.MainPackage;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private Triage triage;

    private Toolbar toolbar;
    private NavigationView navigationView;
    private Button  triageView;
    private ImageButton emgButton;
    private TextView hrText;
    private LinearLayout triageHistoryContainer;
    private Button[] triageHistoryViewItems = new Button[10];
    private View line1,line2;
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
        triageView = (Button) findViewById(R.id.triageButton);
        triageView.setTextColor(Color.WHITE);

        line1 = (View) findViewById(R.id.line);
        line2 = (View) findViewById(R.id.line2);

        triageHistoryContainer = (LinearLayout) findViewById(R.id.triageHistoryContainer);
        triageHistoryViewItems[0] = (Button) findViewById(R.id.triageHistoryButton1);
        triageHistoryViewItems[1] = (Button) findViewById(R.id.triageHistoryButton2);
        triageHistoryViewItems[2] = (Button) findViewById(R.id.triageHistoryButton3);
        triageHistoryViewItems[3] = (Button) findViewById(R.id.triageHistoryButton4);
        triageHistoryViewItems[4] = (Button) findViewById(R.id.triageHistoryButton5);
        triageHistoryViewItems[5] = (Button) findViewById(R.id.triageHistoryButton6);
        triageHistoryViewItems[6] = (Button) findViewById(R.id.triageHistoryButton7);
        triageHistoryViewItems[7] = (Button) findViewById(R.id.triageHistoryButton8);
        triageHistoryViewItems[8] = (Button) findViewById(R.id.triageHistoryButton9);
        triageHistoryViewItems[9] = (Button) findViewById(R.id.triageHistoryButton10);


    }

    private void initComponents(){
        mainViewBehaviour = new MainViewBehaviour(this);
        mainViewBehaviour.start();
        bluetoothManagement = new BluetoothManagement(this);
        dataStorage = DataStorage.getInstance();
        triage = new Triage();
        triage.start();
    }

    private void initAndHandleEmgButton(){
        emgButton = (ImageButton) findViewById(R.id.emgButton);
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
        initDataBase();
        initComponents();
        initAndHandleEmgButton();
        initReceivers();
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
        unregisterReceiver(bluetoothManagement.getConnection().getDeviceAndDiscoveryStatusReceiver());
        receivers.unregisterReceivers();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == getResources().getInteger(R.integer.REQUEST_ENABLE_BT)){
            if(!bluetoothManagement.getmBluetoothAdapter().isDiscovering())
                 bluetoothManagement.getmBluetoothAdapter().startDiscovery();
            else {
                bluetoothManagement.getmBluetoothAdapter().cancelDiscovery();
                bluetoothManagement.getmBluetoothAdapter().startDiscovery();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == getResources().getInteger(R.integer.MY_PERMISSIONS_LOCATION)){

                if(!bluetoothManagement.getmBluetoothAdapter().isDiscovering())
                    bluetoothManagement.getmBluetoothAdapter().startDiscovery();
                else {
                    bluetoothManagement.getmBluetoothAdapter().cancelDiscovery();
                    bluetoothManagement.getmBluetoothAdapter().startDiscovery();
                }
        }
    }

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
                triageView.setVisibility(View.VISIBLE);
                triageHistoryContainer.setVisibility(View.VISIBLE);
                line1.setVisibility(View.VISIBLE);
                line2.setVisibility(View.VISIBLE);

            }
            else{
                emgButton.setVisibility(View.GONE);
                hrView.setVisibility(View.GONE);
                hrText.setVisibility(View.GONE);
                triageView.setVisibility(View.GONE);
                triageHistoryContainer.setVisibility(View.GONE);
                line1.setVisibility(View.GONE);
                line2.setVisibility(View.GONE);

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


    public void refreshHistoryImages(TriageCategory[] tab){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TriageCategory temp;
                for(int i=0; i<10; i++){
                    temp = tab[i];
                    if(temp!=null) {
                        switch (temp) {
                            case T1:
                                triageHistoryViewItems[i].setBackgroundColor(getResources().getColor(R.color.t1Color));
                                break;
                            case T2:
                                triageHistoryViewItems[i].setBackgroundColor(getResources().getColor(R.color.t2Color));
                                break;
                            case T3:
                                triageHistoryViewItems[i].setBackgroundColor(getResources().getColor(R.color.t3Color));
                                break;
                            case T4:
                                triageHistoryViewItems[i].setBackgroundColor(getResources().getColor(R.color.t4Color));
                                break;
                            case NOT_DEFINED:
                                triageHistoryViewItems[i].setBackgroundColor(R.drawable.not_defined_background);
                                break;
                        }
                    }
                }
            }
        });

    }

    public void setTriageImage(TriageCategory triageCategory)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(triageCategory){
                    case T1:
                        triageView.setBackgroundColor(getResources().getColor(R.color.t1Color));
                        triageView.setText("T1 - Immediate Treatment");
                        break;
                    case T2:
                        triageView.setBackgroundColor(getResources().getColor(R.color.t2Color));
                        triageView.setText("T2 - Delayed Treatment");
                        break;
                    case T3:
                        triageView.setBackgroundColor(getResources().getColor(R.color.t3Color));
                        triageView.setText("T3 - Minimal Treatment");
                        break;
                    case T4:
                        triageView.setBackgroundColor(getResources().getColor(R.color.t4Color));
                        triageView.setText("T4 - Expectant Treatment");
                        break;
                    case NOT_DEFINED:
                        triageView.setText("Triage category is not defined");
                        triageView.setBackgroundColor(R.drawable.not_defined_background);
                        break;

                }
            }
        });
    }




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

    public TextView getHrText() {
        return hrText;
    }

    public boolean isTriageScreenVisible() {
        return isTriageScreenVisible;
    }

    public BluetoothManagement getBluetoothManagement() {
        return bluetoothManagement;
    }

    public DataStorage getDataStorage() {
        return dataStorage;
    }
}
