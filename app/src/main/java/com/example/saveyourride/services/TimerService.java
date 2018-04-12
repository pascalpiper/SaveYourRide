package com.example.saveyourride.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.saveyourride.utils.Interval;

public class TimerService extends Service {

    // BroadcastReceiver for messages from ActiveFragment
    private BroadcastReceiver activeFragmentReceiver;

    // IntentFilter filters messages received by BroadcastReceiver
    private IntentFilter filter;

    private int minutes, seconds, intervalCount;

    private int numberOfIntervals;
    private long intervalTime;

    /// TEST
    private Interval firstInterval;
    ///

    @Override
    public void onCreate() {
        super.onCreate();

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
                        runIntervals();

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

    private void runIntervals() {

        // DEBUG RUN ONE INTERVAL FIRST
        firstInterval = new Interval(intervalTime, this);
        intervalCount = 0;
        firstInterval.start();
        sendBroadcastToActiveFragment("intervalCount");
        //

//        Interval[] intervals = new Interval[numberOfIntervals];
//        for (int i = 0; i < 1; i++) {
//            intervals[i] = new Interval(intervalTime, this);
//
//            intervalCount = i;
//            if(i == 0) {
//                intervals[i].start();
//            }
//            else {
//                synchronized (this) {
//                    try {
//                        this.wait();
//                        intervals[i].start();
//                    }
//                    catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            sendBroadcastToActiveFragment("intervalCount");
//
//        }
    }

    private void stopIntervals() {
        // DEBUG STOP ONLY ONE INTERVAL
        firstInterval.stop();
        //
    }

    private void resetIntervals() {
        stopIntervals();
        runIntervals();
    }

    public void setValues(int minutes, int seconds) {
        this.minutes = minutes;
        this.seconds = seconds;
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
                Intent intervalCountIntent = new Intent("android.intent.action.INTERVAL_COUNT").putExtra("interval_count", Integer.toString(intervalCount));
                this.sendBroadcast(intervalCountIntent);
                break;
            }
            case "itervalTime" : {
                Intent intervalTimeIntent = new Intent("android.intent.action.REST_INTERVAL_TIME").putExtra("rest_interval_time_sec", Integer.toString(seconds)).putExtra("rest_interval_time_min", Integer.toString(minutes));
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
        System.out.println("SERVICE ON DESTROY");
        // TODO: INTERVAL TIMER STOPEN
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
