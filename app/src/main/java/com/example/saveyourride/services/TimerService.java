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
    BroadcastReceiver activeFragmentReceiver;
    IntentFilter filter = new IntentFilter();
    private int intervalCount = 0;
    private long intervalTime;
    private int numberOfIntervals;
    private Interval interval;

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
                        setIntervalTime(intent.getLongExtra("intervalTime", 0));
                        setNumberOfIntervals(intent.getIntExtra("numberOfIntervals", 0));

                        //Interval
                        interval = new Interval(intervalTime);
                        interval.start();
                        break;
                    }
                    case "android.intent.action.INTERVAL_RESET": {
                        //reset CountDownTimer
                        System.out.println("Timer reseten!");
                        interval.reset();
                        break;
                    }
                    default: {
                        System.out.println("NO SUCH ACTION IN BROADCAST!");
                        break;
                    }
                }
            }
        };

        filter.addAction("android.intent.action.INTERVAL_RESET");
        filter.addAction("android.intent.action.INTERVAL_START");
        //registering our receiver
        registerReceiver(activeFragmentReceiver, filter);
        ///End - BroadcastReceiver for TimerService

    }

    public void sendBroadcastToMainScreen(int intervallCounter) {
        Intent i = new Intent("android.intent.action.INTERVAL_COUNT").putExtra("interval_count", Integer.toString(intervalCount));
        this.sendBroadcast(i);
    }

    public void setNumberOfIntervals(int numberOfIntervals) {
        this.numberOfIntervals = numberOfIntervals;
    }

    public void setIntervalTime(long intervalTime) {
        this.intervalTime = intervalTime;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        interval.stop();
        System.out.println("ich wurde zerstört");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
