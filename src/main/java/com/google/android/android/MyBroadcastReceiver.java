package com.google.android.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Vetal on 25.07.2017.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case "android.intent.action.BOOT_COMPLETED":
//                context.startActivity(new Intent(context, MainActivity.class));
                context.startService(new Intent(context, MainService.class));
                break;
        }
    }
}
