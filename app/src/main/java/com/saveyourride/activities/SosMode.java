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
import android.widget.TextView;

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
    private Button buttonFalseAlarm;
    private Button buttonExit;
    private Button buttonStopSos;
    private TextView textViewStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_sos_mode);

        //init Receiver
        initReceiver();


        // Views
        buttonFalseAlarm = (Button) findViewById(R.id.sosMode_buttonFalseAlarm);
        buttonExit = (Button) findViewById(R.id.sosMode_buttonExit);
        buttonStopSos = (Button) findViewById(R.id.sosMode_buttonStopSos);
        textViewStatus = (TextView) findViewById(R.id.sosMode_textView);
        // Set button invisible on start
        buttonFalseAlarm.setVisibility(View.INVISIBLE);
        buttonExit.setVisibility(View.INVISIBLE);

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

        buttonStopSos.setOnClickListener(new View.OnClickListener() {
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
                switch (intent.getAction()) {
                    case "android.intent.action.SECOND_IS_OVER": {
                        textViewStatus.setText(getString(R.string.restTime) + " " + intent.getIntExtra("restTime", -1));
                        break;
                    }
                    case "android.intent.action.SOS_PROCEDURE_IS_RUNNING": {
                        buttonStopSos.setVisibility(View.INVISIBLE);

                        buttonExit.setVisibility(View.VISIBLE);
                        buttonFalseAlarm.setVisibility(View.VISIBLE);

                        textViewStatus.setText(getString(R.string.sos_signal));
                        break;
                    }
                    case "android.intent.action.SMS_SENT_STATUS": {

                        if (!intent.getBooleanExtra("status", false)) {
                            textViewStatus.setText(getString(R.string.status_sent_sms_not_successful));
                        } else {
                            textViewStatus.setText(getString(R.string.status_sent_sms_successful));
                        }

                        break;
                    }
                    case "android.intent.action.SMS_FALSE_ALARM_SENT_STATUS": {

                        if (!intent.getBooleanExtra("status", false)) {
                            textViewStatus.setText(getString(R.string.status_sent_false_alarm_sms_not_successful));
                        } else {
                            textViewStatus.setText(getString(R.string.status_sent_false_alarm_sms_successful));
                            buttonFalseAlarm.setClickable(false);
                            // TODO ETWAS SCHÖNER
                            buttonFalseAlarm.setBackgroundColor(getResources().getColor(R.color.cardview_dark_background, null));
                        }

                        break;
                    }
                    default: {

                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SECOND_IS_OVER");
        filter.addAction("android.intent.action.SOS_PROCEDURE_IS_RUNNING");
        filter.addAction("android.intent.action.SMS_SENT_STATUS");
        filter.addAction("android.intent.action.SMS_FALSE_ALARM_SENT_STATUS");

        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(smmService);
        unregisterReceiver(receiver);
    }
}

