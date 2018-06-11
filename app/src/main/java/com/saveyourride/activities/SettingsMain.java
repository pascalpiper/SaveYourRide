package com.saveyourride.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.saveyourride.fragments.SettingsFragment;

public class SettingsMain extends AppCompatActivity {

    // Debug
    private final String TAG = "SettingsMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
