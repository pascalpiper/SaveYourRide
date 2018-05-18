package com.saveyourride.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.saveyourride.R;
import com.saveyourride.activities.PassiveMode;
import com.saveyourride.utils.PermissionUtils;

import static android.content.Context.LOCATION_SERVICE;

public class Passive extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    // DEBUG
    private final String TAG = "PassiveFragment";
    //

    // Permission request codes
    private final int SEND_SMS_PERMISSIONS_REQUEST_CODE = 1;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 2;

    // DialogIDs
    private final int LOCATION_PERMISSION_EXPLANATION_DIALOG = 0;
    private final int LOCATION_PERMISSION_DENIED_DIALOG = 1;
    private final int SEND_SMS_PERMISSION_EXPLANATION_DIALOG = 2;
    private final int SEND_SMS_PERMISSION_DENIED_DIALOG = 3;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    // Buttons
    private Button buttonStartPassiveMode;

    // TODO change workflow of location permission request (alles neu machen)

    // Because of Fragment we need an activity object.
    private FragmentActivity myActivity;
    private SupportMapFragment mapFragment;
    private GoogleMap myGoogleMap;
    private LocationManager myLocationManager;
    private String bestLocationProvider;
    private LocationProvider myLocationProvider;
    private Location currentLocation;
    private SharedPreferences sharedPreferencesLastLocation;
    private LocationListener gpsLocationListener;
    private LocationListener networkLocationListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_passive, container, false);

        myActivity = getActivity();
        sharedPreferencesLastLocation = myActivity.getSharedPreferences(getString(R.string.sp_key_location), Context.MODE_PRIVATE);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    myOnMapReady(googleMap);
                }
            });
        }

        // R.id.map is a FrameLayout, not a Fragment
        getChildFragmentManager().beginTransaction().replace(R.id.passiveFragment_map, mapFragment).commit();

        buttonStartPassiveMode = (Button) rootView.findViewById(R.id.passiveFragment_buttonStart);

        buttonStartPassiveMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSmsPermission()) {
                    startActivity(new Intent(getActivity(), PassiveMode.class));
                }
            }
        });
        return rootView;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    private void myOnMapReady(GoogleMap googleMap) {

        myGoogleMap = googleMap;
        googleMap.getUiSettings().setAllGesturesEnabled(false);

        if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(myActivity, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            enableMyLocation();
            initWithPermission();
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(myActivity, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (myGoogleMap != null) {
            // Access to the location has been granted to the app.
            myGoogleMap.setMyLocationEnabled(true);
        }
    }

    /**
     * Init with permission. First steps. But only after permission check.
     */
    private void initWithPermission() {
        if (!getMyLocation()) {
            if (!locationServicesCheck(myLocationManager)) {
                // location services are enabled
                LatLng germanyLatLng = new LatLng(50.980602, 10.314458);
                myGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(germanyLatLng, 6));
                Toast.makeText(myActivity, "Your Location-Services are enabled\n" +
                        "You can use some 'My Location' button.", Toast.LENGTH_LONG).show();

            } else {
                // location services are disabled
                LatLng germanyLatLng = new LatLng(50.980602, 10.314458);
                myGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(germanyLatLng, 6));
                Toast.makeText(myActivity, "Your Location-Services are probably disabled. You can not use some features.", Toast.LENGTH_LONG).show();
            }
        } else {
            locationServicesCheck(myLocationManager);
        }

    }

    /**
     * Init without permission. First steps. But only after permission check.
     */
    private void initWithoutPermission() {
        // location services are disabled
        LatLng germanyLatLng = new LatLng(50.980602, 10.314458);
        myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(germanyLatLng, 6));
        Toast.makeText(myActivity, "Your Location-Services are probably disabled\n" +
                "You can not use some features.", Toast.LENGTH_LONG).show();
    }

    /**
     * Tries to get {@link Location} of Device. First of all it checkes last known {@link Location}. If no one is available it requests for one.
     *
     * @return true if location found, flase - if not.
     */
    private boolean getMyLocation() {

        // Permission Check
        if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(myActivity, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }

        // Move to current Position
        if (myLocationManager == null || myLocationProvider == null) {
            myLocationManager = (LocationManager) myActivity.getSystemService(LOCATION_SERVICE);

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(true);
            criteria.setSpeedRequired(false);

            bestLocationProvider = myLocationManager.getBestProvider(criteria, true);
            if (bestLocationProvider == null) {
                return false;
            } else {
                myLocationProvider = myLocationManager.getProvider(bestLocationProvider);
            }
        }

        // Find last location
        Location lastLocation = myLocationManager.getLastKnownLocation(bestLocationProvider);
        lastLocation = lastLocation == null ? myLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) : lastLocation;
        lastLocation = lastLocation == null ? myLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) : lastLocation;
        lastLocation = lastLocation == null ? currentLocation : lastLocation;

        currentLocation = lastLocation;

        // TODO remove not important check if null. Use default value.
        double sharedLatitude = sharedPreferencesLastLocation.getString(getString(R.string.sp_key_latitude), null) != null ?
                Double.parseDouble(sharedPreferencesLastLocation.getString(getString(R.string.sp_key_latitude), null)) : 0;
        double sharedLongitude = sharedPreferencesLastLocation.getString(getString(R.string.sp_key_longitude), null) != null ?
                Double.parseDouble(sharedPreferencesLastLocation.getString(getString(R.string.sp_key_longitude), null)) : 0;

        if (currentLocation == null) {
            if (sharedLatitude == 0 && sharedLongitude == 0) {
                requestLocation();
                return false;
            } else {
                myLocationManager.removeUpdates(getGpsLocationListener());
                myLocationManager.removeUpdates(getNetworkLocationListener());
                LatLng currentLatLng = new LatLng(sharedLatitude, sharedLongitude);
                myGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                return true;
            }
        } else {
            // Move camera to my Location
            myLocationManager.removeUpdates(getGpsLocationListener());
            myLocationManager.removeUpdates(getNetworkLocationListener());
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            LatLng currentLatLng = new LatLng(latitude, longitude);
            myGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
            return true;
        }

    }

    /**
     * Check if Location Services are enabled. If they are not, start dialog window and settings.
     *
     * @param lm LocationManager provide information about Location-Services.
     */
    private boolean locationServicesCheck(LocationManager lm) {

        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.d("Location", "Probable GPS_PROVIDER is null");
            ex.printStackTrace();
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.d("Location", "Probable NETWORK_PROVIDER is null");
            ex.printStackTrace();
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle(R.string.please_activate_location_services);
            dialog.setMessage("Click ok to goto settings else exit.");
            dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    paramDialogInterface.cancel();
                }
            });
            dialog.show();
        } else {
            return true;
        }

        // Check if the user has enabled the location data.
        // !!!NEVER USED BECOUSE of MULTI-THREAD
        // TODO Implement in right way "Check if the user has enabled the location data."
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.d("Location", "Probable GPS_PROVIDER is null");
            ex.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.d("Location", "Probable NETWORK_PROVIDER is null");
            ex.printStackTrace();
        }
        return !gps_enabled && !network_enabled;
    }

    /**
     * Request for Location with GPS_PROVIDER and NETWORK_PROVIDER
     */
    private void requestLocation() {

        // Permission Check
        if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(myActivity, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }

        gpsLocationListener = getGpsLocationListener();
        myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);
        networkLocationListener = getNetworkLocationListener();
        myLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);
    }

    /**
     * Get GPS_PROVIDER {@link LocationListener}
     *
     * @return LocationListener
     */
    private LocationListener getGpsLocationListener() {
        return gpsLocationListener != null ? gpsLocationListener : new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                SharedPreferences.Editor editor = sharedPreferencesLastLocation.edit();
                editor.putString(getString(R.string.sp_key_latitude), Double.toString(location.getLatitude()));
                editor.putString(getString(R.string.sp_key_longitude), Double.toString(location.getLongitude()));
                editor.apply();
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                myGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
    }

    /**
     * Get NETWORK_PROVIDER {@link LocationListener}
     *
     * @return LocationListener
     */
    private LocationListener getNetworkLocationListener() {
        return networkLocationListener != null ? networkLocationListener : new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                SharedPreferences.Editor editor = sharedPreferencesLastLocation.edit();
                editor.putString(getString(R.string.sp_key_latitude), Double.toString(location.getLatitude()));
                editor.putString(getString(R.string.sp_key_longitude), Double.toString(location.getLongitude()));
                editor.apply();
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                myGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            initWithoutPermission();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(myActivity.getSupportFragmentManager(), "dialog");
    }

    /**
     * Checks if SEND_SMS permission is granted.
     *
     * @return boolean value. If SEND_SMS permission is granted: true else false.
     */
    private boolean checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(myActivity, Manifest.permission.SEND_SMS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showAlertDialog(SEND_SMS_PERMISSION_EXPLANATION_DIALOG);
            } else {
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSIONS_REQUEST_CODE);

            }
            return false;
        } else {
            // Permission has already been granted
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SEND_SMS_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buttonStartPassiveMode.callOnClick();
                } else {
                    showAlertDialog(SEND_SMS_PERMISSION_DENIED_DIALOG);
                }
                break;
            }
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Enable the my location layer if the permission has been granted.
                    enableMyLocation();
                    initWithPermission();
                } else {
                    // Display the missing permission error dialog when the fragments resume.
                    initWithoutPermission();
                    mPermissionDenied = true;
                }
                break;
            }
            default: {
                Log.d(TAG, "NO SUCH REQUEST CODE!");
                break;
            }
        }
    }

    /**
     * Show a dialog with the information for a specific notification.
     *
     * @param dialogID determines the information to be shown.
     */
    private void showAlertDialog(int dialogID) {
        switch (dialogID) {
            case LOCATION_PERMISSION_EXPLANATION_DIALOG: {
                // TODO implement LOCATION_PERMISSION_EXPLANATION_DIALOG
                break;
            }
            case LOCATION_PERMISSION_DENIED_DIALOG: {
                // TODO implement LOCATION_PERMISSION_DENIED_DIALOG
                break;
            }
            case SEND_SMS_PERMISSION_EXPLANATION_DIALOG: {
                AlertDialog.Builder alert = new AlertDialog.Builder(myActivity);
                // Set dialog title
                alert.setTitle(R.string.title_dialog_send_sms_permission);
                // Set dialog message
                alert.setMessage(R.string.dialog_send_sms_permission_explanation);
                // Set up the button
                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSIONS_REQUEST_CODE);
                    }
                });
                AlertDialog currentDialog = alert.create();
                currentDialog.show();
                break;
            }
            case SEND_SMS_PERMISSION_DENIED_DIALOG: {
                AlertDialog.Builder alert = new AlertDialog.Builder(myActivity);
                // Set dialog title
                alert.setTitle(R.string.title_dialog_send_sms_permission);
                // Set dialog message
                alert.setMessage(R.string.dialog_send_sms_permission_required);
                // Set up the buttons
                alert.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Go to app settings
                        Intent appSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", myActivity.getPackageName(), null);
                        appSettings.setData(uri);
                        startActivity(appSettings);
                    }
                });
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog currentDialog = alert.create();
                currentDialog.show();
                break;
            }
            default: {
                Log.d(TAG, "NO SUCH DIALOG!");
                break;
            }
        }
    }
}
