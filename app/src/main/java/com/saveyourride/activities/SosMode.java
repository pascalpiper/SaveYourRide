package com.saveyourride.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.saveyourride.R;
import com.saveyourride.services.SosModeManager;

public class SosMode extends AppCompatActivity {

    private final String TAG = "SosMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_mode);

        Button buttonSos = (Button)findViewById(R.id.buttonSosInSosMode);
        Button buttonFalseAlarm = (Button)findViewById(R.id.buttonFalseAlarm);

        startService(new Intent(this,  SosModeManager.class));

        buttonSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Sos");
            }
        });

        buttonFalseAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(getApplicationContext(), SosModeManager.class));
                startActivity(new Intent(getApplicationContext(), MainScreen.class));
                finish();
            }
        });

    }
}
