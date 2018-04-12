package com.example.saveyourride.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.saveyourride.utils.Interval;

public class IntervallTimer extends Service {

    private int intervalCounter = 0;

    private long intervalTime;
    private int maxIntervals;
    private Interval interval;

    //BroadcastReceiver for ActiveFragment
    BroadcastReceiver intervallTimerReceiver;
    IntentFilter intentFilter = new IntentFilter();

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Service gestartet");

        //BroadcastReceiver for IntervallTimer
        /**
         * This BroadcastReceiver receive the broadcast from the Active Fragment.
         * This will starting the reset from the CountDownTimer
         */
        intervallTimerReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("android.intent.action.INTERVAL_RESET")) {
                    //reset CountDownTimer
                    System.out.println("Timer reseten!");
                    interval.reset();
                } else if(intent.getAction().equals("android.intent.action.INTERVAL_START")) {
                    setIntervalTime(intent.getIntExtra("intervalTime", 0));
                    setMaxIntervals(intent.getIntExtra("maxIntervals", 0));

                    //Interval
                    interval = new Interval(intervalTime);
                    interval.start();
                }

            }
        };
        intentFilter.addAction("android.intent.action.INTERVAL_RESET");
        intentFilter.addAction("android.intent.action.INTERVAL_START");
        //registering our receiver
        registerReceiver(intervallTimerReceiver, intentFilter);
        ///End - BroadcastReceiver for IntervallTimer

    }

    public void sendBroadcastToMainScreen(int intervallCounter) {
        Intent i = new Intent("android.intent.action.INTERVAL_COUNTER").putExtra("intervall_counter", Integer.toString(intervalCounter));
        this.sendBroadcast(i);
    }

    public void setMaxIntervals(int maxIntervals){
        this.maxIntervals = maxIntervals;
    }

    public void setIntervalTime(long intervalTime){
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
