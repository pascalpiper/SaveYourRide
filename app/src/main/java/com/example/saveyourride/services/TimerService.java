package com.example.saveyourride.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.saveyourride.activities.MainScreen;
import com.example.saveyourride.utils.Interval;

public class TimerService extends Service {

    // BroadcastReceiver for messages from ActiveFragment
    private BroadcastReceiver activeFragmentReceiver;

    // IntentFilter filters messages received by BroadcastReceiver
    private IntentFilter filter;

    private Intent intentMainScreen;


    private int minutes, seconds, intervalCount;

    private int numberOfIntervals;
    private long intervalTime;

    private Interval currentInterval;


    @Override
    public void onCreate() {
        super.onCreate();

        intervalCount = 0;
        intentMainScreen = new Intent(this.getApplicationContext(), MainScreen.class);

        // DEBUG
        System.out.println("Service gestartet");
        //


        /// BroadcastReceiver for TimerService
        /*
         * This BroadcastReceiver receives the broadcast from the Active Fragment.
         * It will start or reset the CountDownTimer
         */
        activeFragmentReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "android.intent.action.START_TIMER": {
                        // DEBUG
                        System.out.println("Service-Receiver received 'start'-broadcast");
                        //
                        numberOfIntervals = intent.getIntExtra("numberOfIntervals", 0);
                        intervalTime = intent.getLongExtra("intervalTime", 0);

                        //run the intervals
                        runInterval(intervalCount);

                        break;
                    }
                    case "android.intent.action.RESET_TIMER": {
                        // DEBUG
                        System.out.println("Service-Receiver received 'reset'-broadcast");
                        //
                        // reset|restart the intervals
                        resetIntervals();

                        break;
                    }
                    case "android.intent.action.STOP_TIMER": {
                        // DEBUG
                        System.out.println("Service-Receiver received 'stop'-broadcast");
                        //
                        stopIntervals();
                    }
                    default: {
                        System.out.println("NO SUCH ACTION IN BROADCAST!");
                        break;
                    }
                }
            }
        };

        filter = new IntentFilter();
        filter.addAction("android.intent.action.RESET_TIMER");
        filter.addAction("android.intent.action.START_TIMER");
        filter.addAction("android.intent.action.STOP_TIMER");

        // register our receiver
        registerReceiver(activeFragmentReceiver, filter);
        ///End - BroadcastReceiver for TimerService

        // TODO Check if it is not async...
        sendBroadcastToActiveFragment("serviceReady");

    }

    public void runInterval(int intervalCount) {

        // DEBUG RUN ONE INTERVAL FIRST
//        firstInterval = new Interval(intervalTime, this, sperre);
//        intervalCount = 0;
//        firstInterval.start();
//        sendBroadcastToActiveFragment("intervalCount");
        //

            if(intervalCount < numberOfIntervals) {
                currentInterval = new Interval(intervalTime, this, intervalCount);
                currentInterval.start();
                this.intervalCount = intervalCount;
                sendBroadcastToActiveFragment("intervalCount");
            }
            else {
                sendBroadcastToActiveFragment("timerFinish");
        }
    }

    private void stopIntervals() {
        // DEBUG STOP ONLY ONE INTERVAL
        currentInterval.stop();
        //
    }

    private void resetIntervals() {
        stopIntervals();
        intervalCount = 0;
        runInterval(intervalCount);
    }

    public void setValues(int minutes, int seconds) {
        this.minutes = minutes;
        this.seconds = seconds;

        // DEBUG
        System.out.println("SetValues: Min: " + minutes + ", Sec: " + seconds);
        //

        sendBroadcastToActiveFragment("intervalTime");
    }

    public void sendBroadcastToActiveFragment(String broadcast) {
        switch (broadcast){
            case "serviceReady" : {
                Intent serviceReadyIntent = new Intent("android.intent.action.TIMER_SERVICE_READY");
                this.sendBroadcast(serviceReadyIntent);
                break;
            }
            case "intervalCount" : {
                Intent intervalCountIntent = new Intent("android.intent.action.INTERVAL_COUNT").putExtra("interval_count", intervalCount);
                this.sendBroadcast(intervalCountIntent);
                break;
            }
            case "intervalTime": {
                Intent intervalTimeIntent = new Intent("android.intent.action.REST_INTERVAL_TIME").putExtra("rest_interval_time_min", minutes).putExtra("rest_interval_time_sec", seconds);
                this.sendBroadcast(intervalTimeIntent);
                break;
            }
            default:
                System.out.println("TimerService: No Such Broadcast");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(activeFragmentReceiver);
        startActivity(intentMainScreen);
        System.out.println("SERVICE ON DESTROY");
        // TODO: INTERVAL TIMER STOPEN
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
