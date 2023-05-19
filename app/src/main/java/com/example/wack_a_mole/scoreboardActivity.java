package com.example.wack_a_mole;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class scoreboardActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private TextView scoreboard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //hides top-menu

        setContentView(R.layout.activity_scoreboard);
        prefs = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        scoreboard = findViewById(R.id.scoreBoard);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("a", "100");
        editor.putString("b", "200");
        editor.putString("c", "300");
        editor.putString("d", "400");
        editor.putString("e", "500");
        editor.putString("f", "600");
        editor.putString("g", "700");
        editor.putString("h", "800");
        editor.putString("i", "900");
        editor.putString("j", "1000");
        editor.apply();
        readScores();
    }
    private void readScores() {
        Map<String,?> allEntries =  prefs.getAll();
        List<Map.Entry<String, ?>> entryList = new ArrayList<>(allEntries.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, ?>>() {
            @Override
            public int compare(Map.Entry<String, ?> entry1, Map.Entry<String, ?> entry2) {
                Integer value1 = Integer.valueOf((String) entry1.getValue());
                Integer value2 = Integer.valueOf((String) entry2.getValue());
                return value2.compareTo(value1);
            }
        });
        int i = 0;
        String complete = "";

        while(i < 10) {
            for (Map.Entry<String, ?> entry : entryList) {
                i++;
                String key = entry.getKey();
                key = key.toUpperCase();
                if(key.length() > 3) {
                    key = key.substring(0,3);
                }
                Object value = entry.getValue();
                complete = complete + key + " " + (String)value + "\n";
            }
            i++;
        }
        scoreboard.setText(complete);
    }
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}