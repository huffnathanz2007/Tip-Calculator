package com.example.tipcalimproved;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

       //getFragmentManager().beginTransaction()
         //       .replace(android.R.id.content,new SettingsFragment()).commit();
        setContentView(R.layout.activity_settings);
    }
}
