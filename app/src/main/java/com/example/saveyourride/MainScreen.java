package com.example.saveyourride;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

    // Edit Pascal
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //menu activity bekannt
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //click listener quasi.
        switch (item.getItemId()) {
            case R.id.settingsButton:
                System.out.println("SETTINGS");
                // Intent erzeugen und Starten der AktiendetailActivity mit explizitem Intent
                Intent settingsIntent = new Intent(this, MainScreen.class);
                // settingsIntent.putExtra(Intent.EXTRA_TEXT, aktienInfo);
                startActivity(settingsIntent);
                break;
        }
        return super.onOptionsItemSelected(item); //To change body of generated methods, choose Tools | Templates.
    }
}
