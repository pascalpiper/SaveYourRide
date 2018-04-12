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

    //BroadcastReceiver for ActiveFragment
    private BroadcastReceiver activeFragmentReceiver;
    private IntentFilter filter;

    private int minutes, seconds, intervalCount;

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Service gestartet");

        //BroadcastReceiver for TimerService
        /*
         * This BroadcastReceiver receive the broadcast from the Active Fragment.
         * This will starting the reset from the CountDownTimer
         */
        activeFragmentReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "android.intent.action.INTERVAL_START": {
                        // DEBUG
                        System.out.println("Service hat start bekommen");
                        //
                        int numberOfIntervals = intent.getIntExtra("numberOfIntervals", 0);
                        long intervalTime = intent.getLongExtra("intervalTime", 0);

                        //run the Intervals
                        runIntervals(numberOfIntervals, intervalTime);

                        break;
                    }
                    case "android.intent.action.INTERVAL_RESET": {
                        //reset CountDownTimer
                        System.out.println("Timer reseten!");
                        break;
                    }
                    default: {
                        System.out.println("NO SUCH ACTION IN BROADCAST!");
                        break;
                    }
                }
            }
        };

        filter = new IntentFilter();
        filter.addAction("android.intent.action.INTERVAL_RESET");
        filter.addAction("android.intent.action.INTERVAL_START");
        //registering our receiver
        registerReceiver(activeFragmentReceiver, filter);
        ///End - BroadcastReceiver for TimerService
        // TODO Check if it is not async...
        sendBroadcastToActiveFragment("serviceReady");

    }

    public void sendBroadcastToActiveFragment(String broadcast) {
        switch (broadcast){
            case "serviceReady" : {
                Intent i = new Intent("android.intent.action.TIMER_SERVICE_READY");
                this.sendBroadcast(i);
                break;
            }
            case "sendIntervallCount" : {
                Intent i = new Intent("android.intent.action.INTERVAL_COUNT").putExtra("interval_count", Integer.toString(intervalCount));
                this.sendBroadcast(i);
                break;
            }
            case "sendIntervallTime" : {
                Intent i = new Intent("android.intent.action.REST_INTERVAL_TIME").putExtra("rest_interval_time_sec", Integer.toString(seconds)).putExtra("rest_interval_time_min", Integer.toString(minutes));
                this.sendBroadcast(i);
                break;
            }
            default:
                System.out.println("TimerService: No Such Broadcast");
        }

    }

    private void runIntervals(int numberOfIntervals, long intervalTime) {
        Interval[] intervals = new Interval[numberOfIntervals];
        for (int i = 0; i < 1; i++) {
            intervals[i] = new Interval(intervalTime, this);

            intervalCount = i;
            if(i == 0) {
                intervals[i].start();
            }
            else {
                synchronized (this) {
                    try {
                        this.wait();
                        intervals[i].start();
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            sendBroadcastToActiveFragment("sendIntervallCount");

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("ich wurde zerstört");
        // TODO: INTERVAL TIMER STOPEN
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setValues(int minutes, int seconds) {
        this.minutes = minutes;
        this.seconds = seconds;
        sendBroadcastToActiveFragment("sendIntervallTime");
    }
}
