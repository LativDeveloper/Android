package com.google.android.android;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import org.json.simple.JSONArray;

import java.util.Iterator;
import java.util.List;

public class MyWifiManager {

    private WifiManager wifiManager;

    MyWifiManager() {
        wifiManager = (WifiManager) MainService.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    boolean setWifiEnabled(boolean enabled) {
        wifiManager.setWifiEnabled(enabled);
        return wifiManager.isWifiEnabled();
    }

    boolean reconnect() {
        return wifiManager.reconnect();
    }

    boolean disconnect() {
        return wifiManager.disconnect();
    }

    boolean connectToWifi(final String ssid, final String password) {
        if (!wifiManager.isWifiEnabled()) wifiManager.setWifiEnabled(true);

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = String.format("\"%s\"", ssid);
        conf.preSharedKey = String.format("\"%s\"", password);

        int netId = wifiManager.addNetwork(conf);
        if (netId == -1) return false;
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        return true;
    }

    JSONArray getWifiList() {
        if (!wifiManager.isWifiEnabled()) wifiManager.setWifiEnabled(true);
        JSONArray wifiList = new JSONArray();
        List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
        if (networks == null) return wifiList;
        Iterator<WifiConfiguration> iterator = networks.iterator();
        while (iterator.hasNext()) {
            WifiConfiguration wifiConfig = iterator.next();
            wifiList.add(wifiConfig.SSID.substring(1, wifiConfig.SSID.length()-1));
        }

        return wifiList;
    }

    WifiManager getWifiManager() {
        return wifiManager;
    }

}