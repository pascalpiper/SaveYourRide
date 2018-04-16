package com.example.saveyourride.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.saveyourride.R;
import com.example.saveyourride.services.TimerService;

public class ActiveMode extends AppCompatActivity {


    // TODO: Pick Up values for number of intervals and interval time
    private final int numberOfIntervals = 6;
    private final long intervalTime = 10000L;

    // BroadcastReceiver for messages from TimerService
    private BroadcastReceiver timerServiceReceiver;

    // IntentFilter filters messages received by BroadcastReceiver
    private IntentFilter filter;

    private TextView textViewIntervalCount;
    private TextView textViewTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_mode);

        final Intent intentTimerService = new Intent(this.getApplicationContext(), TimerService.class);
        filter = new IntentFilter();

        filter.addAction("android.intent.action.TIMER_SERVICE_READY");
        filter.addAction("android.intent.action.INTERVAL_COUNT");
        filter.addAction("android.intent.action.REST_INTERVAL_TIME");

        Button buttonStartTimer = (Button) findViewById(R.id.buttonResetTimer);
        Button buttonStopTimer = (Button) findViewById(R.id.buttonStopTimer);
        textViewTime = (TextView)findViewById(R.id.textViewTimer);
        textViewIntervalCount = (TextView)findViewById(R.id.textViewIntervalCount);

        buttonStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    resetTimer();
            }
        });

        buttonStopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcastToTimerService("stopTimer");
                stopService(intentTimerService);
            }
        });

        /// BroadcastReceiver for ActiveFragment
        /*
         * This BroadcastReceiver receives the broadcasts from the TimerService
         * It can receive following broadcasts:
         * - intervalCount
         * - restIntervalTime
         * - finish-message from the Timer
         * - status, that the Service is ready
         */
        timerServiceReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                switch (intent.getAction()) {
                    case "android.intent.action.TIMER_SERVICE_READY": {
                        // DEBUG
                        System.out.println("Fragment-Receiver received 'service-ready'-broadcast");
                        //
                        sendBroadcastToTimerService("startTimer");
                        break;
                    }
                    case "android.intent.action.INTERVAL_COUNT": {
                        int intervalCount = intent.getIntExtra("interval_count", -1);
                        System.out.println("Fragment-Receiver received interval count: " + intervalCount);
                        textViewIntervalCount.setText(Integer.toString(intervalCount+1) + " / " + numberOfIntervals);
                        break;
                    }
                    case "android.intent.action.REST_INTERVAL_TIME": {
                        int intervalTimeMin = intent.getIntExtra("rest_interval_time_min", -1);
                        int intervalTimeSec = intent.getIntExtra("rest_interval_time_sec", -1);
                        System.out.println("Fragment-Receiver received interval time : " + intervalTimeMin + " Min : " + intervalTimeSec + " Sec ");
                        setTextViewTime(intervalTimeMin, intervalTimeSec);
                        break;
                    }
                    default:
                        Toast.makeText(getApplicationContext(), "Unknown Broadcast received", Toast.LENGTH_LONG).show();
                }
            }
        };

        // register our receiver
        registerReceiver(timerServiceReceiver, filter);
        ///End - BroadcastReceiver for ActiveFragment

        startService(intentTimerService);
    }


    private void setTextViewTime(int intervalTimeMin, int intervalTimeSec) {
        String time = String.format("%02d", intervalTimeMin) + ":" + String.format("%02d", intervalTimeSec);
        textViewTime.setText(time);
    }

    private void resetTimer() {
        sendBroadcastToTimerService("resetTimer");
    }

    /**
     * This method will send a broadcast to the service TimerService.
     * case start => TierService becomes the numberOfIntervals and the intervalTime and start the timer.
     * case reset => TimerService will reset the timer and start the new one.
     * case stop => TimerService will stop the timer
     */
    private void sendBroadcastToTimerService(String broadcast) {
        switch (broadcast) {
            case "startTimer": {
                Intent startTimerIntent = new Intent("android.intent.action.START_TIMER").putExtra("numberOfIntervals", numberOfIntervals).putExtra("intervalTime", intervalTime);
                sendBroadcast(startTimerIntent);
                break;
            }
            case "resetTimer": {
                Intent resetTimerIntent = new Intent("android.intent.action.RESET_TIMER");
                sendBroadcast(resetTimerIntent);
                break;
            }
            case "stopTimer": {
                Intent stopTimerIntent = new Intent("android.intent.action.STOP_TIMER");
                sendBroadcast(stopTimerIntent);
                break;
            }
            default: {
                Toast.makeText(this, "Unknown broadcast!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(timerServiceReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(timerServiceReceiver, filter);
    }

}
