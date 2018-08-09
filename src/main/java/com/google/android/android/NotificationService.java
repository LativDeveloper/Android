package com.google.android.android;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NotificationService extends NotificationListenerService {

    Context context;

    @Override
    public void onCreate() {
            Log.i("Carter", "NotService created!");
        super.onCreate();
        context = getApplicationContext();

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pack = sbn.getPackageName();
        String ticker ="";
        if(sbn.getNotification().tickerText !=null) {
            ticker = sbn.getNotification().tickerText.toString();
        }
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text = extras.getCharSequence("android.text").toString();
        int id1 = extras.getInt(Notification.EXTRA_SMALL_ICON);
        Bitmap id = sbn.getNotification().largeIcon;


        Log.i("Package",pack);
        Log.i("Ticker",ticker);
        Log.i("Title",title);
        Log.i("Text",text);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        Calendar calendar = Calendar.getInstance();

        JSONObject notification = new JSONObject();
        notification.put("package", pack);
        notification.put("ticker", ticker);
        notification.put("title", title);
        notification.put("text", text);
        notification.put("date", simpleDateFormat.format(calendar.getTime()));

        saveLog(notification);
    }

    private void saveLog(JSONObject notification) {
        File logFile = new File(Config.NOTIFICATION_PATH + "notif-log.json");
        JSONParser jsonParser = new JSONParser();
        JSONArray notifArray = new JSONArray();
        if (logFile.exists()) {
            try {
                notifArray = (JSONArray) jsonParser.parse(new FileReader(logFile));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        notifArray.add(notification);
        try {
            FileWriter fileWriter = new FileWriter(logFile);
            fileWriter.write(notifArray.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
//        Log.i("Msg","Notification Removed");
    }
}
