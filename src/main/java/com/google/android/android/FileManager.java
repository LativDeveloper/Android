package com.google.android.android;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;

import org.json.simple.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by user-pc on 20.05.2017.
 */

public class FileManager {

    //возвращает список ВСЕХ файлов, включая директории
    public JSONArray getFiles(File dirFile) {
        //File test = Environment.getExternalStorageDirectory();
        String[] fileList = dirFile.list();
        JSONArray jsonArray = new JSONArray();
        if (fileList == null) return jsonArray;
        for (int i = 0; i < fileList.length; i++)
            jsonArray.add(fileList[i]);
        return jsonArray;
    }

    public boolean deleteFile(String dir) {
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

    public boolean clearDir(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) return false;
        File[] files = dirFile.listFiles();
        if (files == null || files.length == 0) return false;
        for (File file : files) {
            deleteFile(file.getAbsolutePath());
        }
        return true;
    }

    public boolean renameFile(String dir, String newDir) {
        File file = new File(dir);
        File newFile = new File(newDir);
        return file.renameTo(new File(newDir));
    }

    public boolean copyFile(String dir, String newDir) {
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

    public long getSize(String dir) {
        /*File file = new File(dir);
        return file.length();*/
        return getLength(new File(dir), 0);
    }

    private long getLength(File file, long size) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                size = getLength(f, size);
            }
        } else {
            size += file.length();
        }
        return size;
    }

    public String getLastModified(String dir) {
        File file = new File(dir);
        Date date = new Date(file.lastModified());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    public boolean makeDir(String dir) {
        File newDir = new File(dir);
        return newDir.mkdir();
    }

    public boolean buildZip(String dirPath, String zipPath, int level) {
        try {
            File dirFile = new File(dirPath);
            if (!dirFile.isDirectory()) return false;
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipPath + dirFile.getName() + ".zip"));
            zipOut.setLevel(level); // compression from 0 (min) to 9 (max)
            doZip(dirFile, zipOut);
            zipOut.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void doZip(File dirFile, ZipOutputStream zipOut) throws IOException {
        for (File file: dirFile.listFiles()) {
            if (file.isDirectory())
                doZip(file, zipOut);
            else {
                zipOut.putNextEntry(new ZipEntry(file.getPath()));
                write(new FileInputStream(file), zipOut);
            }
        }
    }

    private void write(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        in.close();
    }
}
