package com.example.saveyourride.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

public class IntervallTimer extends Service {

    private int intervalCounter = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("Service gestartet");
    }

    public void sendBroadcastToMainScreen(int intervallCounter){
        Intent i = new Intent("android.intent.action.INTERVAL_COUNTER").putExtra("intervall_counter", Integer.toString(intervalCounter));
        this.sendBroadcast(i);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("ich wurde zerstört");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
