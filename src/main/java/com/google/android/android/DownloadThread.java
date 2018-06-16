package com.google.android.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class DownloadThread extends Thread {

    private String filename;
    private int port;
    private String downloadPath;

    DownloadThread(String filename, int port, String downloadPath) {
        this.filename = filename;
        this.port = port;
        this.downloadPath = downloadPath;
    }

    @Override
    public void run() {
        try {
            File file = new File(downloadPath + filename);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            Socket socket = new Socket(Config.IP_ADDRESS, port);
            InputStream inputStream = socket.getInputStream();
            byte[] bytes = new byte[8 * 1024];
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, len); //receive file from server
            }

            fileOutputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
