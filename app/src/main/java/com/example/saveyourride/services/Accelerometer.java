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

    private static final int SHAKE_THRESHOLD = 50;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    @Override
    public void onCreate() {
        super.onCreate();
        senSensorManager = (SensorManager) this.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            float acceleration = Math.abs(x) + Math.abs(y) + Math.abs(z);

            if (acceleration > SHAKE_THRESHOLD) {
                Intent shake = new Intent("android.intent.action.ACCELEROMETER_DETECTED_STRONG_SHAKE").putExtra("acceleration", acceleration);
                sendBroadcast(shake);
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
