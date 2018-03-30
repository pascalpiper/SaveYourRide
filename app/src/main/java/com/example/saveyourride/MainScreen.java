package com.example.saveyourride;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.List;


public class MainScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        final SensorManager mSensorManager;
        final Sensor mAccelerometer;
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        print(mSensorManager);
    }

    private void print(SensorManager m) {
        int i = 0;
        while (i < 2) {
            List<Sensor> list = m.getSensorList(Sensor.TYPE_ALL);
            System.out.println("Hallo");
            for (Sensor each : list) {
                System.out.println(each.getName());
            }
            i++;
        }
    }

    // New Branch //
}
