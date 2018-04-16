package com.example.saveyourride.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.saveyourride.R;
import com.example.saveyourride.services.LocationService;
import com.example.saveyourride.utils.PermissionUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class PassiveMode extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap myGoogleMap;

    // BroadcastReceiver for messages from LocationService
    private BroadcastReceiver locationServiceReceiver;

    // IntentFilter filters messages received by BroadcastReceiver
    private IntentFilter filter;

    private LatLng currentLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passive_mode);

        // START Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapInPassiveMode);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                myOnMapReady(googleMap);
            }
        });
        // END Map

        // START Location Service
        Intent intentLocationService = new Intent(getApplicationContext(), LocationService.class);
        filter = new IntentFilter();

        // add actions into IntentFilter
        initFilter();

        // set up BroadcastReceiver
        initReceiver();

        startService(intentLocationService);
        System.out.println("START OF LOCATION_SERVICE");
        // END Location Service
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    private void myOnMapReady(GoogleMap googleMap) {
        myGoogleMap = googleMap;
        myGoogleMap.getUiSettings().setAllGesturesEnabled(false);
        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (myGoogleMap != null) {
            // Access to the location has been granted to the app.
            myGoogleMap.setMyLocationEnabled(true);
        }
    }

    /**
     * Add all needed actions into IntentFilter
     */
    private void initFilter() {
        filter.addAction("android.intent.action.LOCATION");
    }

    /**
     * Initialize the BroadcastReceiver.
     * This BroadcastReceiver receives the broadcasts from the LocationService
     * It can receive following broadcasts:
     * - Location with Latitude and Longitude as (double) extras.
     */
    private void initReceiver() {
        locationServiceReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                switch (intent.getAction()) {
                    case "android.intent.action.LOCATION": {
                        // DEBUG
                        System.out.println("Fragment-Receiver received 'location'-broadcast");
                        //
                        break;
                    }
                    default:
                        Toast.makeText(getApplicationContext(), "Unknown Broadcast received", Toast.LENGTH_LONG).show();
                }
            }
        };
    }
}
