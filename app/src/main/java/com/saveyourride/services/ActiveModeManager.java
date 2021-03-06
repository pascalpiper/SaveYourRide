package com.saveyourride.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class ActiveModeManager extends Service {

    // DEBUG
    private final String TAG = "ActiveModeManager";
    //

    // BroadcastReceiver for messages from ActiveMode Activity
    private BroadcastReceiver receiver;

    // Current interval number
    private int intervalNumber;

    // Values received from Activity
    private int numberOfIntervals;
    private long timeOfInterval;

    // CountDownTimer to run interval
    private CountDownTimer currentTimer;

    @Override
    public void onCreate() {
        super.onCreate();

        // Init-value of intervalNumber is 1 | first interval
        intervalNumber = 1;

        // initialize BroadcastReceiver
        initActivityReceiver();

        // send broadcast that AMM-Service is ready
        sendBroadcast(new Intent("android.intent.action.AMM_SERVICE_READY"));
    }

    /**
     * Creates new {@code BroadcastReceiver} and {@code IntentFilter} for messages from {@code ActiveMode} and registers them.
     * {@code receiver} receives the broadcasts from the {@code ActiveMode} activity.
     */
    private void initActivityReceiver() {
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "android.intent.action.START_TIMER": {
                        numberOfIntervals = intent.getIntExtra("numberOfIntervals", 0);
                        timeOfInterval = intent.getLongExtra("timeOfInterval", 0);

                        //run first interval | intervalNumber must be 1 at this moment
                        runInterval();
                        break;
                    }
                    case "android.intent.action.RESET_TIMER": {
                        resetIntervals();
                        break;
                    }
                    case "android.intent.action.STOP_TIMER": {
                        // stop current interval
                        currentTimer.cancel();
                        break;
                    }
                    default: {
                        Log.d(TAG, "NO SUCH ACTION IN BROADCAST!");
                        break;
                    }
                }
            }
        };

        // IntentFilter filters broadcasts received by BroadcastReceiver
        IntentFilter filter = new IntentFilter();

        filter.addAction("android.intent.action.RESET_TIMER");
        filter.addAction("android.intent.action.START_TIMER");
        filter.addAction("android.intent.action.STOP_TIMER");

        registerReceiver(receiver, filter);
    }

    /**
     * Run interval as CountDownTimer.
     */
    private void runInterval() {

        // One Second contains 1000 millis | onTick() will be called every second
        final long MILLISECONDS_IN_SECOND = 1000L;

        currentTimer = new CountDownTimer(timeOfInterval, MILLISECONDS_IN_SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                // DEBUG
                Log.d(TAG, "millisUntilFinished = " + millisUntilFinished + "(in Seconds: " + ((int) millisUntilFinished / 1000) + ")");
                //
                sendBroadcast(new Intent("android.intent.action.REST_INTERVAL_TIME").putExtra("rest_interval_millis", millisUntilFinished));
            }

            @Override
            public void onFinish() {
                // If it was last interval (intervalNumber == numberOfIntervals) send ACCIDENT_GUARANTEE_PROCEDURE broadcast.
                if (intervalNumber < numberOfIntervals) {
                    sendBroadcast(new Intent("android.intent.action.INTERVAL_TIME_EXPIRED"));

                    // run the next interval | intervalNumber must be incremented
                    intervalNumber++;
                    runInterval();
                } else {
                    sendBroadcast(new Intent("android.intent.action.ACCIDENT_GUARANTEE_PROCEDURE"));
                }
            }
        }.start();
        sendBroadcast(new Intent("android.intent.action.INTERVAL_NUMBER").putExtra("interval_number", intervalNumber).putExtra("number_of_intervals", numberOfIntervals));
    }

    /**
     * Reset all Intervals.
     * Stop current timer.
     * Set {@code intervalNumber} to 1 (first interval)
     * Run first interval
     */
    private void resetIntervals() {
        currentTimer.cancel();
        intervalNumber = 1;
        runInterval();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}