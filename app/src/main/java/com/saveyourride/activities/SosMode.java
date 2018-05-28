package com.saveyourride.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.saveyourride.R;
import com.saveyourride.services.SosModeManager;

public class SosMode extends AppCompatActivity {

    // DEBUG
    private final String TAG = "SosMode";
    //

    // Intents for SosModeManager-Service (SMM)
    private Intent smmService;

    //
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_sos_mode);

        //init Receiver
        initReceiver();


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
                sendBroadcast(new Intent("android.intent.action.SEND_FALSE_ALARM_SMS"));
            }
        });
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };
        registerReceiver(receiver, new IntentFilter("android.intent.action.SEND_SMS_SUCCESSFUL"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(smmService);
        unregisterReceiver(receiver);
    }
}

