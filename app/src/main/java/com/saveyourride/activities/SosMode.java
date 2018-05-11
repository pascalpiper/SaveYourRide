package com.saveyourride.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.saveyourride.R;
import com.saveyourride.services.SosModeManager;

public class SosMode extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    //
    // DEBUG
    private final String TAG = "ActiveMode";
    // Intents for SosModeManager-Service (SMM)
    private Intent smmService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_sos_mode);

        // Views
        Button buttonFalseAlarm = (Button) findViewById(R.id.sosMode_buttonFalseAlarm);
        Button buttonExit = (Button) findViewById(R.id.sosMode_buttonExit);

        // Keeps Activity ON also on lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Create intent for Service
        smmService = new Intent(this, SosModeManager.class);

        // Start Services
        startService(smmService);

        // Button listeners
        buttonFalseAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                stopService(new Intent(getApplicationContext(), SosModeManager.class));
//                startActivity(new Intent(getApplicationContext(), MainScreen.class));
//                finish();
                sendSMSMessage();
            }
        });
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "EXIT");
            }
        });
    }


    protected void sendSMSMessage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
        else {
            sendBroadcast(new Intent("android.intent.action.SEND_SMS"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        Log.d(TAG, " "+ requestCode);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    sendBroadcast(new Intent("android.intent.action.SEND_SMS"));

                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(smmService);
    }
}

