package com.example.saveyourride.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.saveyourride.R;
import com.example.saveyourride.services.ControlService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PassiveMode extends AppCompatActivity {

    private Intent controlService;
    private Button buttonStopPassiveMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passive_mode);

        buttonStopPassiveMode = (Button) findViewById(R.id.buttonStopPassiveMode);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapInPassiveMode);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                LatLng latLng = new LatLng(51.023226, 7.564927);
                googleMap.addMarker(new MarkerOptions().position(latLng)
                        .title("Singapore"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                googleMap.getUiSettings().setAllGesturesEnabled(false);
            }
        });

        controlService = new Intent(this.getApplicationContext(), ControlService.class);
        startService(controlService);

        buttonStopPassiveMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(controlService);
            }
        });

        /// ONLY FOR TESTS

        Button buttonRead = (Button) findViewById(R.id.buttonReadFromFile);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent readIntent = new Intent("android.intent.action.PASSIV_FRAGMENT");
                sendBroadcast(readIntent);
            }
        });
    }
}
