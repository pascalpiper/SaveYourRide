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

import com.saveyourride.activities.MainScreen;

public class ActiveModeManager extends Service {

    // DEBUG
    private final String TAG = "ActiveModeManager";
    //

    // BroadcastReceiver for messages from ActiveMode Activity
    private BroadcastReceiver activityReceiver;

    // Value for TextView
    private int intervalCount;

    // Values received from Activity
    private int numberOfIntervals;
    private long intervalTime;

    // CountDownTimer to run interval
    private CountDownTimer currentTimer;

    @Override
    public void onCreate() {
        super.onCreate();

        intervalCount = 0;

        initActivityReceiver();

        sendBroadcast(new Intent("android.intent.action.AMM_SERVICE_READY"));
    }

    /**
     * Creates new {@code BroadcastReceiver} and {@code IntentFilter} for messages from {@code ActiveMode} and registers them.
     * {@code activityReceiver} receives the broadcasts from the {@code ActiveMode} activity.
     */
    private void initActivityReceiver() {
        activityReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "android.intent.action.START_TIMER": {
                        // DEBUG
                        Log.d(TAG, "'START-TIMER' - broadcast received");
                        //
                        numberOfIntervals = intent.getIntExtra("numberOfIntervals", 0);
                        intervalTime = intent.getLongExtra("intervalTime", 0);

                        //run first interval
                        runInterval(intervalCount);

                        break;
                    }
                    case "android.intent.action.RESET_TIMER": {
                        // DEBUG
                        Log.d(TAG, "'RESET-TIMER' - broadcast received");
                        //
                        resetIntervals(); // reset|restart the intervals
                        break;
                    }
                    case "android.intent.action.STOP_TIMER": {
                        // DEBUG
                        Log.d(TAG, "'STOP-TIMER' - broadcast received");
                        //
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

        // IntentFilter filters messages received by BroadcastReceiver
        IntentFilter filter = new IntentFilter();

        filter.addAction("android.intent.action.RESET_TIMER");
        filter.addAction("android.intent.action.START_TIMER");
        filter.addAction("android.intent.action.STOP_TIMER");

        // register our receiver
        registerReceiver(activityReceiver, filter);
    }

    /**
     * Run interval as CountDownTimer.
     *
     * @param intervalCount number of current interval. If it equal to {@code numberOfIntervals} call // TODO name of "SicherStellungsverfahren"
     */
    private void runInterval(int intervalCount) {
        if (intervalCount < numberOfIntervals) {

            // One Second contains 1000 millis | onTick() will be called every second
            final long MILLISECONDS_IN_SECOND = 1000L;

            currentTimer = new CountDownTimer(intervalTime, MILLISECONDS_IN_SECOND) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // DEBUG
                    Log.d(TAG, "millisUntilFinished = " + millisUntilFinished + "(in Seconds: " + ((int) millisUntilFinished / 1000) + ")");
                    //
                    sendBroadcast(new Intent("android.intent.action.REST_INTERVAL_TIME").putExtra("rest_interval_millis", millisUntilFinished));
                }

                @Override
                public void onFinish() {
                    // DEBUG
                    Log.d(TAG, "OnFinish() von Interval: " + ActiveModeManager.this.intervalCount);
                    //

                    runInterval(++ActiveModeManager.this.intervalCount);

                    if (ActiveModeManager.this.intervalCount < numberOfIntervals) {
                        sendBroadcast(new Intent("android.intent.action.INTERVAL_EXPIRED"));
                    } else {
                        // DEBUG
                        Log.d(TAG, "intervalCount is : " + ActiveModeManager.this.intervalCount + ". Probable it was the last one.");
                        //
                    }

                }
            }.start();

            sendBroadcast(new Intent("android.intent.action.INTERVAL_COUNT").putExtra("interval_count", intervalCount));

        } else {
            // TODO Call "Sicherstellungsverfahren"
            Log.d(TAG, "Call 'Sicherstellungsverfahren'");
        }
    }

    private void resetIntervals() {
        currentTimer.cancel();
        intervalCount = 0;
        runInterval(intervalCount);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // DEBUG
        Log.d(TAG, "AMM-SERVICE ON DESTROY");
        //
        // unregister BroadcastReceiver
        unregisterReceiver(activityReceiver);

        startActivity(new Intent(this.getApplicationContext(), MainScreen.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}