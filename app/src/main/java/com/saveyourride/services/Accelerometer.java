package com.saveyourride.services;

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

    private final float ACCIDENT_THRESHOLD = 130;
    private final float NO_MOVE_THRESHOLD = 2;

    private boolean noMovementBroadcastWasSent = false;
    private int noMovementCounter = 0;
    private final int MAX_NO_MOVEMENT_COUNTER = 20;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    @Override
    public void onCreate() {
        super.onCreate();
        senSensorManager = (SensorManager) this.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            float acceleration = Math.abs(x) + Math.abs(y) + Math.abs(z);

//            System.out.println("----  " + acceleration + "  ------ C: " + noMovementCounter);

            if (!noMovementBroadcastWasSent && acceleration < NO_MOVE_THRESHOLD) {


                if (noMovementCounter >= MAX_NO_MOVEMENT_COUNTER) {
                    Intent noMovement = new Intent("android.intent.action.ACCELEROMETER_NO_MOVEMENT");
                    sendBroadcast(noMovement);

                    noMovementBroadcastWasSent = true;

                } else {
                    noMovementCounter++;
                }
            }
            if (noMovementBroadcastWasSent && acceleration > NO_MOVE_THRESHOLD) {
                Intent movementAgain = new Intent("android.intent.action.ACCELEROMETER_MOVEMENT_AGAIN");
                sendBroadcast(movementAgain);

                noMovementBroadcastWasSent = false;
                noMovementCounter = 0;
            }

            if (!noMovementBroadcastWasSent && acceleration > ACCIDENT_THRESHOLD) {
                Intent shake = new Intent("android.intent.action.ACCELEROMETER_POSSIBLE_ACCIDENT").putExtra("acceleration", acceleration);
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
