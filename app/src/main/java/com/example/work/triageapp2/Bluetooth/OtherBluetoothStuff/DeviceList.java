//package com.example.work.triageapp2.Bluetooth.OtherBluetoothStuff;
//
//import android.os.Parcel;
//import android.os.Parcelable;
//
//import java.util.ArrayList;

///**
// * Created by BoryS on 05.09.2017.
// */
//
//public class DeviceList implements Parcelable {
//    ArrayList<Device> list;
//
//
//    public DeviceList(){
//        list = new ArrayList<Device>();
//    }
//
//    public DeviceList(Parcel in){
//        this.list = in.readArrayList(null);
//    }
//
//    public void addDevice(Device dC){
//        list.add(dC);
//    }
//
//    public void clearList(){
//        list.clear();;
//    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel parcel, int i) {
//        parcel.writeList(list);
//    }
//
//    public static Creator<DeviceList> CREATOR = new Creator<DeviceList>() {
//
//        @Override
//        public DeviceList createFromParcel(Parcel source) {
//            return new DeviceList(source);
//        }
//
//        @Override
//        public DeviceList[] newArray(int size) {
//            return new DeviceList[size];
//        }
//
//    };
//
//}
