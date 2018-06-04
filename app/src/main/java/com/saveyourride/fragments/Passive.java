package com.saveyourride.fragments;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.saveyourride.R;
import com.saveyourride.activities.PassiveMode;
import com.saveyourride.services.Location;

public class Passive extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    // DEBUG
    private final String TAG = "PassiveFragment";
    //

    // Permission request codes
    private final int SEND_SMS_PERMISSIONS_REQUEST_CODE = 1;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 2;

    // DialogIDs
    private final int LOCATION_PERMISSION_EXPLANATION_DIALOG = 0;
    private final int SEND_SMS_PERMISSION_EXPLANATION_DIALOG = 2;
    private final int SEND_SMS_PERMISSION_DENIED_DIALOG = 3;
    private final int LOCATION_SERVICES_DISABLED = 4;

    // Buttons
    private Button buttonStartPassiveMode;

    // BroadcastReceiver
    private BroadcastReceiver receiver;

    // IntentFilter
    private IntentFilter filter;

    // Intents for Services
    private Intent locationService;

    // Location providers state (enabled per default = true)
    private boolean gpsState = true, networkState = true;

    // SharedPreferences
    private SharedPreferences lastKnownLocation;

    // GoogleMap
    private GoogleMap myGoogleMap;

    // ImageViews for myLocation
    private ImageView myLocationCircleView, myLocationAnimatedView;

    // Because of Fragment we need an activity object.
    private FragmentActivity myActivity;
    private SupportMapFragment mapFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_passive, container, false);

        myActivity = getActivity();

        // Set SharedPreferences
        lastKnownLocation = myActivity.getSharedPreferences(getString(R.string.sp_key_last_known_location), Context.MODE_PRIVATE);

        // Set Map
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    myOnMapReady(googleMap);
                }
            });
        }
        getChildFragmentManager().beginTransaction().replace(R.id.passiveFragment_map, mapFragment).commit();

        // My Location View
        myLocationAnimatedView = (ImageView) rootView.findViewById(R.id.passiveFragment_myLocationAnimated);
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                myLocationAnimatedView,
                PropertyValuesHolder.ofFloat("scaleX", 6f),
                PropertyValuesHolder.ofFloat("scaleY", 6f));
        scaleDown.setDuration(1000);
        scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);
        scaleDown.start();
        // TODO: scaleDown.cancel() and not only set transparent
        myLocationCircleView = (ImageView) rootView.findViewById(R.id.passiveFragment_myLocationCircle);

        // Set Button
        buttonStartPassiveMode = (Button) rootView.findViewById(R.id.passiveFragment_buttonStart);

        // Initialize BroadcastReceiver
        initReceiver();

        // Button Listener
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
        myGoogleMap.getUiSettings().setAllGesturesEnabled(false);

        if (checkLocationPermission()) {
            initWithPermission();
        } else {
            initWithoutPermission();
            Toast.makeText(myActivity, getString(R.string.location_permission_denied), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Checks if {@link Manifest.permission#ACCESS_FINE_LOCATION} permission is granted.
     *
     * @return boolean value. If {@link Manifest.permission#ACCESS_FINE_LOCATION} permission is granted: true else false.
     */
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(myActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showAlertDialog(LOCATION_PERMISSION_EXPLANATION_DIALOG);
            } else {
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
            return false;
        } else {
            // Permission has already been granted
            return true;
        }
    }

    /**
     * Checks if {@link Manifest.permission#SEND_SMS} permission is granted.
     *
     * @return boolean value. If {@link Manifest.permission#SEND_SMS} permission is granted: true else false.
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

    /**
     * Initialize map-fragment with permission. First steps. But only after permission check.
     */
    private void initWithPermission() {
        String latString = lastKnownLocation.getString(getString(R.string.sp_key_latitude), getString(R.string.default_germany_latitude));
        String lngString = lastKnownLocation.getString(getString(R.string.sp_key_longitude), getString(R.string.default_germany_longitude));
        double latitude = Double.parseDouble(latString);
        double longitude = Double.parseDouble(lngString);
        float zoom = Float.parseFloat(getString(R.string.default_user_location_zoom));
        LatLng lastKnownLatLng = new LatLng(latitude, longitude);
        moveMapCamera(lastKnownLatLng, zoom, false);

        // Start location service only if permission was granted
        locationService = new Intent(myActivity.getApplicationContext(), Location.class);
        myActivity.startService(locationService);
    }

    /**
     * Initialize map-fragment without permission. First steps. But only after permission check.
     */
    private void initWithoutPermission() {
        // Disable drawables
        myLocationCircleView.setBackgroundResource(R.color.colorTransparent);
        myLocationAnimatedView.setBackgroundResource(R.color.colorTransparent);

        // location services are disabled
        double latitude = Double.parseDouble(getString(R.string.default_germany_latitude));
        double longitude = Double.parseDouble(getString(R.string.default_germany_longitude));
        float zoom = Float.parseFloat(getString(R.string.default_germany_zoom));
        LatLng lastKnownLatLng = new LatLng(latitude, longitude);
        moveMapCamera(lastKnownLatLng, zoom, false);
    }

    /**
     * Creates new {@link BroadcastReceiver} and {@link IntentFilter} and then registers them.
     * {@code receiver} receives the broadcasts from the {@code Location} service.
     */
    private void initReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                switch (intent.getAction()) {
                    case "android.intent.action.LOCATION_UPDATE": {
                        if (intent.getStringExtra("locationProvider").equals(LocationManager.NETWORK_PROVIDER)) {
                            myLocationCircleView.setBackgroundResource(R.drawable.my_location_red_circle);
                            myLocationAnimatedView.setBackgroundResource(R.drawable.my_location_animated_circle);
                            Log.d(TAG, "Network provider was detected!");
                        } else if (intent.getStringExtra("locationProvider").equals(LocationManager.GPS_PROVIDER)) {
                            myLocationCircleView.setBackgroundResource(R.drawable.my_location_green_circle);
                            myLocationAnimatedView.setBackgroundResource(R.drawable.my_location_animated_circle);
                            Log.d(TAG, "GPS provider was detected!");
                        }
                        double defaultLatitude = Double.parseDouble(getString(R.string.default_germany_latitude));
                        double defaultLongitude = Double.parseDouble(getString(R.string.default_germany_longitude));
                        float zoom = Float.parseFloat(getString(R.string.default_user_location_zoom));
                        double newLatitude = intent.getDoubleExtra("latitude", defaultLatitude);
                        double newLongitude = intent.getDoubleExtra("longitude", defaultLongitude);
                        LatLng newLatLng = new LatLng(newLatitude, newLongitude);
                        moveMapCamera(newLatLng, zoom, true);
                        break;
                    }
                    case "android.intent.action.LOCATION_PROVIDER_STATUS_UPDATE": {
                        String provider = intent.getStringExtra("locationProvider");
                        int status = intent.getIntExtra("locationProviderStatus", LocationProvider.OUT_OF_SERVICE);
                        if (provider.equals(LocationManager.GPS_PROVIDER)) {
                            if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                                myLocationCircleView.setBackgroundResource(R.drawable.my_location_yellow_circle);
                                Log.d(TAG, "Gps is temporarily unavailable!");
                            } else if (status == LocationProvider.AVAILABLE) {
                                myLocationCircleView.setBackgroundResource(R.drawable.my_location_green_circle);
                                Log.d(TAG, "Gps is available!");
                            }
                        }
                        break;
                    }
                    case "android.intent.action.LOCATION_PROVIDER_DISABLED": {
                        if (intent.getStringExtra("locationProvider").equals(LocationManager.GPS_PROVIDER)) {
                            gpsState = false;
                        } else if (intent.getStringExtra("locationProvider").equals(LocationManager.NETWORK_PROVIDER)) {
                            networkState = false;
                        }
                        if (!gpsState && !networkState) {
                            showAlertDialog(LOCATION_SERVICES_DISABLED);
                            initWithoutPermission();
                        }
                        break;
                    }
                    case "android.intent.action.LOCATION_PROVIDER_ENABLED": {
                        if (intent.getStringExtra("locationProvider").equals(LocationManager.GPS_PROVIDER)) {
                            gpsState = true;
                        } else if (intent.getStringExtra("locationProvider").equals(LocationManager.NETWORK_PROVIDER)) {
                            networkState = true;
                        }
                        break;
                    }
                    default:
                        Log.d(TAG, "Unknown Broadcast received");
                        break;
                }
            }
        };

        // IntentFilter filters messages received by BroadcastReceiver
        filter = new IntentFilter();

        filter.addAction("android.intent.action.LOCATION_UPDATE");
        filter.addAction("android.intent.action.LOCATION_PROVIDER_STATUS_UPDATE");
        filter.addAction("android.intent.action.LOCATION_PROVIDER_DISABLED");
        filter.addAction("android.intent.action.LOCATION_PROVIDER_ENABLED");

        // register our receiver
        myActivity.registerReceiver(receiver, filter);
    }

    /**
     * Move Google-Map camera to new or changed position ({@link LatLng}).
     *
     * @param latLng   new or changed position ({@link LatLng}).
     * @param zoom     zoom of camera.
     * @param animated move camera with animation (true) or not (false).
     */
    private void moveMapCamera(LatLng latLng, float zoom, boolean animated) {
        if (animated) {
            myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        } else {
            myGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initWithPermission();
                } else {
                    initWithoutPermission();
                    Toast.makeText(myActivity, getString(R.string.location_permission_denied), Toast.LENGTH_LONG).show();
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
                AlertDialog.Builder alert = new AlertDialog.Builder(myActivity);
                // Set dialog title
                alert.setTitle(R.string.title_dialog_location_permission);
                // Set dialog message
                alert.setMessage(R.string.dialog_location_permission_explanation);
                // Set up the button
                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog currentDialog = alert.create();
                currentDialog.show();
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
                alert.setMessage(R.string.dialog_send_sms_permission_denied);
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
            case LOCATION_SERVICES_DISABLED: {
                AlertDialog.Builder alert = new AlertDialog.Builder(myActivity);
                // Set dialog title
                alert.setTitle(R.string.title_dialog_location_services);
                // Set dialog message
                alert.setMessage(R.string.dialog_location_services_are_disabled);
                // Set up the buttons
                alert.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Go to location settings
                        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
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

    @Override
    public void onPause() {
        super.onPause();
        // DEBUG
        Log.d(TAG, "onPause!");
        //
        if (locationService != null)
            myActivity.stopService(locationService);
        myActivity.unregisterReceiver(receiver);
    }

    public void onResume() {
        super.onResume();
        // DEBUG
        Log.d(TAG, "onResume!");
        //
        if (locationService != null) {
            locationService = new Intent(myActivity.getApplicationContext(), Location.class);
            myActivity.startService(locationService);
        }
        myActivity.registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // DEBUG
        Log.d(TAG, "onDestroy!");
        //
        if (locationService != null)
            myActivity.stopService(locationService);
    }
}
