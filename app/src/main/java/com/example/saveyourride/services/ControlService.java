package com.example.saveyourride.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.saveyourride.activities.MainScreen;

import java.util.ArrayList;

public class ControlService extends Service {

    private ArrayList<BroadcastReceiver> broadcastReceivers;
    private ArrayList<IntentFilter> intentFilters;
    private ArrayList<Intent> services;

    private BroadcastReceiver passiveFragmentReceiver;
    private IntentFilter passiveFragmentFilter;

    private BroadcastReceiver accelerometerReceiver;
    private IntentFilter accerometerFilter;

    private BroadcastReceiver locationReceiver;
    private IntentFilter locationReceiverFilter;

    private Intent accelerometer;

    @Override
    public void onCreate() {
        super.onCreate();

        broadcastReceivers = new ArrayList<BroadcastReceiver>();
        intentFilters = new ArrayList<IntentFilter>();
        services = new ArrayList<Intent>();


        accelerometer = new Intent(this.getApplicationContext(), Accelerometer.class);
        //location = new Intent(this.getApplicationContext(), location.class);

        services.add(accelerometer);
        //services.add(location);


        passiveFragmentReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };

        accelerometerReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Broadcast from Accelerometer");
                System.out.println("Wir haben das Handy geschüttelt mit der Geschwindigkeit " + intent.getFloatExtra("speed", -1));
            }
        };

        locationReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };

        broadcastReceivers.add(passiveFragmentReceiver);
        broadcastReceivers.add(accelerometerReceiver);
        broadcastReceivers.add(locationReceiver);

        passiveFragmentFilter = new IntentFilter("android.intent.action.PASSIV_FRAGMENT");
        accerometerFilter = new IntentFilter("android.intent.action.ACCELEROMETER_DETECTED_SHAKE");
        locationReceiverFilter = new IntentFilter("android.intent.action.LOCATION");

        intentFilters.add(passiveFragmentFilter);
        intentFilters.add(accerometerFilter);
        intentFilters.add(locationReceiverFilter);



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
        for (Intent service :services){
            startService(service);
        }
    }

    private void stopAllServices(){
        for (Intent service :services){
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
