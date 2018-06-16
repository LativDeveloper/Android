package com.google.android.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Stack;

public class FileManagerActivity extends AppCompatActivity {
    private ListView _filesListView;
    private Stack<String> _dirStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        _dirStack = new Stack<String>();

        String[] names = new String[3000];
        for (int i = 0; i < names.length; i++)
            names[i] = i + " file";

        // находим список
        _filesListView = (ListView) findViewById(R.id.filesListView);

        // создаем адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);

        // присваиваем адаптер списку
        _filesListView.setAdapter(adapter);

        initListeners();
    }

    private void initListeners() {
        _filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dir = ((TextView) view).getText().toString();
                _dirStack.push(dir);
            }
        });
    }
}
