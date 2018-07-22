package com.google.android.android;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;

import org.json.simple.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by user-pc on 20.05.2017.
 */

public class Utils {

    //возвращает список ВСЕХ файлов, включая директории
    public static JSONArray getFiles(File dirFile) {
        //File test = Environment.getExternalStorageDirectory();
        String[] fileList = dirFile.list();
        JSONArray jsonArray = new JSONArray();
        if (fileList == null) return jsonArray;
        for (int i = 0; i < fileList.length; i++)
            jsonArray.add(fileList[i]);
        return jsonArray;
    }

    public static boolean deleteFile(String dir) {
        File dirFile = new File(dir);
        if (dirFile.delete()) return true;
        File[] files = dirFile.listFiles();
        if (files == null) return false;

        boolean isDeleted = true;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory())
                deleteFile(files[i].getPath());
            else if (!files[i].delete()) isDeleted = false;
        }
        return dirFile.delete() && isDeleted;
    }

    public static boolean renameFile(String dir, String newDir) {
        File file = new File(dir);
        File newFile = new File(newDir);
        return file.renameTo(new File(newDir));
    }

    public static boolean copyFile(String dir, String newDir) {
        if (dir.equals(newDir)) return false;

        File sourceFile = new File(dir);
        File targetFile = new File(newDir);
        FileChannel source = null;
        FileChannel target = null;
        try {

            targetFile.createNewFile();

            source = new FileInputStream(sourceFile).getChannel();
            target = new FileOutputStream(targetFile).getChannel();
            target.transferFrom(source, 0, source.size());
        }
        catch (Exception e) {
            try {
                source.close();
                target.close();
            } catch (Exception exception) {
                return false;
            }
            return false;
        }
        return true;
    }

    public static long getSize(String dir) {
        File file = new File(dir);
        return file.length();
    }

    public static String getLastModified(String dir) {
        File file = new File(dir);
        Date date = new Date(file.lastModified());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    public static boolean makeDir(String dir) {
        File newDir = new File(dir);
        return newDir.mkdir();
    }
}
