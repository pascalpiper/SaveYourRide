package com.example.saveyourride.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.saveyourride.activities.MainScreen;

import java.util.ArrayList;

public class ControlService extends Service {

    private ArrayList<BroadcastReceiver> broadcastReceivers;
    private ArrayList<IntentFilter> intentFilters;
    private ArrayList<Intent> serviceIntents;

    private static final String TAG = "ControlService";

    @Override
    public void onCreate() {
        super.onCreate();

        broadcastReceivers = new ArrayList<BroadcastReceiver>();
        intentFilters = new ArrayList<IntentFilter>();
        serviceIntents = new ArrayList<Intent>();

        serviceIntents.add(new Intent(this.getApplicationContext(), Accelerometer.class));
        serviceIntents.add(new Intent(this.getApplicationContext(), LocationService.class));

        //Passive Fragment
        broadcastReceivers.add(new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {

            }
        });

        //Accelerometer
        broadcastReceivers.add(new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Broadcast from Accelerometer");
                System.out.println("Wir haben das Handy geschüttelt mit der Geschwindigkeit " + intent.getFloatExtra("acceleration", -1));
            }
        });

        //Location
        broadcastReceivers.add( new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Broadcast from LocationService");
                Log.d(TAG, "Location Speed: " + intent.getFloatExtra("location_speed", -1));
            }
        });

        intentFilters.add(new IntentFilter("android.intent.action.PASSIV_FRAGMENT"));
        intentFilters.add(new IntentFilter("android.intent.action.ACCELEROMETER_DETECTED_STRONG_SHAKE"));
        intentFilters.add(new IntentFilter("android.intent.action.LOCATION"));

        registerAllBroadcastReceivers();//
        startAllServices();
    }

    /**
     * Register all BroadcastReveicers from a ArrayList with its intentFilters in another ArrayList.
     */
    public void registerAllBroadcastReceivers(){
        for (int i = 0; i < broadcastReceivers.size(); i++ ){
            registerReceiver(broadcastReceivers.get(i), intentFilters.get(i));
            System.out.println(broadcastReceivers.get(i));
        }
    }

    /**
     * unregister all BroadcastReceivers from this Service
     */
    public void unregisterAllBroadcastReceivers(){
        for (int i = 0; i < broadcastReceivers.size(); i++ ){
            unregisterReceiver(broadcastReceivers.get(i));
        }
    }

    private void startAllServices(){
        for (Intent service : serviceIntents){
            startService(service);
        }
    }

    private void stopAllServices(){
        for (Intent service : serviceIntents){
            // DEBUG
            System.out.println("STOP SERVICE: " + service.toString());
            //
            stopService(service);
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterAllBroadcastReceivers();
        stopAllServices();

        Intent mainScreen = new Intent(this.getApplicationContext(), MainScreen.class);
        startActivity(mainScreen);

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
