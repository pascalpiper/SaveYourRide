package com.saveyourride.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.saveyourride.R;

public class Location extends Service {

    // DEBUG
    private static final String TAG = "Location";
    //

    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 1f;
    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    // SharedPreferences for last known location
    private SharedPreferences lastKnownLocation;
    // LocationManager
    private LocationManager mLocationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // DEBUG
        Log.d(TAG, "onCreate");
        //

        // Set SharedPreferences
        lastKnownLocation = getSharedPreferences(getString(R.string.sp_key_last_known_location), Context.MODE_PRIVATE);

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "gps provider does not exist " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "network provider does not exist, " + ex.getMessage());
        }
    }

    private void initializeLocationManager() {
        // DEBUG
        Log.d(TAG, "initializeLocationManager");
        //
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    /**
     * Save new or changed {@code location} to shared preferences.
     *
     * @param location new or changed {@code location}
     */
    private void saveLocation(android.location.Location location) {
        SharedPreferences.Editor editor = lastKnownLocation.edit();
        editor.putString(getString(R.string.sp_key_latitude), Double.toString(location.getLatitude()));
        editor.putString(getString(R.string.sp_key_longitude), Double.toString(location.getLongitude()));
        editor.apply();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (LocationListener each : mLocationListeners) {
                try {
                    mLocationManager.removeUpdates(each);
                } catch (Exception ex) {
                    Log.e(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Listener for location updates and provider changes.
     */
    private class LocationListener implements android.location.LocationListener {
        android.location.Location mLastLocation;

        private LocationListener(String provider) {
            // DEBUG
            Log.d(TAG, "LocationListener constructor: " + provider);
            //
            mLastLocation = new android.location.Location(provider);
        }

        @Override
        public void onLocationChanged(android.location.Location location) {
            // DEBUG
            Log.d(TAG, "onLocationChanged: " + location);
            //
            mLastLocation.set(location);

            // Save new location to SharedPreferences
            saveLocation(location);

            //Send location update broadcast
            Intent locationUpdate = new Intent("android.intent.action.LOCATION_UPDATE")
                    .putExtra("latitude", location.getLatitude())
                    .putExtra("longitude", location.getLongitude())
                    .putExtra("locationProvider", location.getProvider());
            sendBroadcast(locationUpdate);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // DEBUG
            Log.d(TAG, "onProviderDisabled: " + provider);
            //
            //Send broadcast
            Intent providerDisabled = new Intent("android.intent.action.LOCATION_PROVIDER_DISABLED")
                    .putExtra("locationProvider", provider);
            sendBroadcast(providerDisabled);
        }

        @Override
        public void onProviderEnabled(String provider) {
            //
            Log.d(TAG, "onProviderEnabled: " + provider);
            //
            //Send broadcast
            Intent providerEnabled = new Intent("android.intent.action.LOCATION_PROVIDER_ENABLED")
                    .putExtra("locationProvider", provider);
            sendBroadcast(providerEnabled);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // DEBUG
            Log.d(TAG, "onStatusChanged: Provider:" + provider + " Status: " + status + " (2 means AVAILABLE)");
            //
            //Send status update broadcast
            Intent statusUpdate = new Intent("android.intent.action.LOCATION_PROVIDER_STATUS_UPDATE")
                    .putExtra("locationProvider", provider)
                    .putExtra("locationProviderStatus", status);
            sendBroadcast(statusUpdate);
        }
    }
}
