package com.example.saveyourride.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.saveyourride.R;
import com.example.saveyourride.services.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PassiveMode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passive_mode);

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

        // START LOCATION SERVICE
        Intent intentLocationService = new Intent(getApplicationContext(), LocationService.class);
        startService(intentLocationService);
        System.out.println("START OF LOCATION_SERVICE");
        //
    }

}
