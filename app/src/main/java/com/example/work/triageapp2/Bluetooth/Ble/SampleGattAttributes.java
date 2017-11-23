package com.example.work.triageapp2.Bluetooth.Ble;

/**
 * Created by BoryS on 22.08.2017.
 */

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String  MYOWARE_MUSCLE_SENSOR_SERVICE = "19b10010-e8f2-537e-4f6c-d104768a1214";
    public static String MYOWARE_MUSCLE_SENSOR_CHARACTERISTIC = "19b10012-e8f2-537e-4f6c-d104768a1214";

    static {
        // Sample Services.
        attributes.put(HEART_RATE_SERVICE, "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");

        attributes.put(MYOWARE_MUSCLE_SENSOR_SERVICE, "MyoWare Service");
        attributes.put(MYOWARE_MUSCLE_SENSOR_CHARACTERISTIC, "Muscle Measurment");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
