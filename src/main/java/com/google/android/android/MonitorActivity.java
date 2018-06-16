package com.google.android.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MonitorActivity extends AppCompatActivity {
    private final String TOKEN = ":";
    private EditText _inputEditText;
    private EditText _outputEditText;
    private Button _queryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        Intent intent = new Intent(this, FileManagerActivity.class);
        startActivity(intent);

        _inputEditText = (EditText) findViewById(R.id.inputEditText);
        _outputEditText = (EditText) findViewById(R.id.outputEditText);
        _queryButton= (Button) findViewById(R.id.queryButton);
        _inputEditText.setText("getfiles /storage/sdcard0/alarms"); //for test

        String output = doQuery("rename /storage/sdcard0/alarms/min.txt,/storage/sdcard0/alarms/abc.txt");
        _outputEditText.setText(output);
        initListeneres();
    }

    private void initListeneres() {
        _queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String output = doQuery(_inputEditText.getText().toString());
                _outputEditText.setText(output);
            }
        });
    }

    private String doQuery(String input) {

        return null;
    }
}
