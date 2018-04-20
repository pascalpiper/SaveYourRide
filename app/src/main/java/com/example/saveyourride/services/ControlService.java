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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ControlService extends Service {

    private ArrayList<BroadcastReceiver> broadcastReceivers;
    private ArrayList<IntentFilter> intentFilters;
    private ArrayList<Intent> serviceIntents;

    private static final String TAG = "ControlService";

    /// ONLY FOR TESTS
    private File accelerometerDataFile;
    private String accelerometerData;
    ///

    @Override
    public void onCreate() {
        super.onCreate();

        /// ONLY FOR TESTS
        accelerometerDataFile = getFile();
        ///

        broadcastReceivers = new ArrayList<BroadcastReceiver>();
        intentFilters = new ArrayList<IntentFilter>();
        serviceIntents = new ArrayList<Intent>();

        serviceIntents.add(new Intent(this.getApplicationContext(), Accelerometer.class));
        serviceIntents.add(new Intent(this.getApplicationContext(), LocationService.class));

        //Passive Fragment
        broadcastReceivers.add(new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("ACCELEROMETER DATA FILE: \n" + readAccelerometerDataFromFile(accelerometerDataFile));
            }
        });

        //Accelerometer
        broadcastReceivers.add(new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Broadcast from Accelerometer");
                System.out.println("Wir haben das Handy geschüttelt mit der Geschwindigkeit " + intent.getFloatExtra("acceleration", -1));

                /// ONLY FOR TESTS
                accelerometerData = accelerometerData + "\n" + "Acceleration is: " + intent.getFloatExtra("acceleration", -1);
                //writeAccelerometerDataToFile(accelerometerDataFile, accelerometerData);
                ///
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

    /// ONLY FOR TESTS
    private File getFile() {
        File path = getApplicationContext().getFilesDir();
        return new File(path, "accelerometer_data.txt");
    }

    private void writeAccelerometerDataToFile(File file, String data) {
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(data.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readAccelerometerDataFromFile(File file) {
        int length = (int) file.length();
        byte[] bytes = new byte[length];

        try {
            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(bytes);
    }
    ///


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterAllBroadcastReceivers();
        stopAllServices();

        Intent mainScreen = new Intent(this.getApplicationContext(), MainScreen.class);
        startActivity(mainScreen);

        /// ONLY FOR TESTSWrite received broadcast into file
        writeAccelerometerDataToFile(accelerometerDataFile, accelerometerData);
        ///
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
