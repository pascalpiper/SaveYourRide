package com.example.saveyourride.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

/*
 * Created by taraszaika on 03.04.18.
 * new Accelerometer
 */
public class Accelerometer extends Service implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 2000;

    @Override
    public void onCreate() {
        super.onCreate();
        senSensorManager = (SensorManager) this.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            /// TEST
            System.out.println("X:   " + x + "    Y:   " + y + "    Z:   " + z);
            ///

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    /// TEST
                    Intent i = new Intent("android.intent.action.ACCELEROMETER_DETECTED_SHAKE").putExtra("speed", speed);
                    sendBroadcast(i);
                    //System.out.println("Wir haben das Handy geschüttelt mit der Geschwindigkeit " + speed);
                    ///
                }

                last_x = x;
                last_y = y;
                last_z = z;

                /// TEST
                //System.out.println("EINE SEKUNDE");
                ///
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        senSensorManager.unregisterListener(this, senAccelerometer);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
