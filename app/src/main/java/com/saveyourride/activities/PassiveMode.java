package com.saveyourride.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.saveyourride.R;
import com.saveyourride.services.NotificationManager;
import com.saveyourride.services.PassiveModeManager;
import com.saveyourride.utils.PermissionUtils;

public class PassiveMode extends AppCompatActivity {

    // DEBUG
    private final String TAG = "PassiveMode";
    //

    //Dialog IDs
    private final int BACK_PRESSED = 0;
    private final int NO_MOVEMENT_DETECTED = 1;
    private final int ACCIDENT_GUARANTEE_PROCEDURE = 2;

    //Dialog
    AlertDialog currentDialog;

    // BroadcastReceiver
    private BroadcastReceiver receiver;

    private Intent pmmService, notificationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_passive_mode);

        // Map
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

        // Views
        Button buttonStopPassiveMode = (Button) findViewById(R.id.buttonStopPassiveMode);

        // Keeps Activity ON also on lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize BroadcastReceiver
        initReceiver();

        // Create intents for Services
        pmmService = new Intent(this.getApplicationContext(), PassiveModeManager.class);
        notificationService = new Intent(this.getApplicationContext(), NotificationManager.class);

        // Start Services
        startService(pmmService);
        startService(notificationService);

        // Button listeners
        buttonStopPassiveMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        /// ONLY FOR TESTS
        Button buttonRead = (Button) findViewById(R.id.buttonReadFromFile);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent("android.intent.action.PASSIVE_MODE_ACTIVITY"));
            }
        });

//        // Ask permissions
//        requestPermissions();
    }

//    private void requestPermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission to access the location is missing.
//            PermissionUtils.requestPermission(this, REQUEST_CODE_SEND_SMS,
//                    Manifest.permission.SEND_SMS, true);
//        } else {
//            Log.d(TAG, "Permission for sendSMS is given");
//        }
//    }

    /**
     * Creates new {@code BroadcastReceiver} and {@code IntentFilter} and then registers them.
     * {@code receiver} receives the broadcasts from the {@code PassiveModeManager} and {@code NotificationManager}.
     */
    private void initReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                switch (intent.getAction()) {
                    case "android.intent.action.NMD_SHOW_DIALOG": {
                        showAlertDialog(NO_MOVEMENT_DETECTED);
                        break;
                    }
                    case "android.intent.action.AGP_SHOW_DIALOG": {
                        showAlertDialog(ACCIDENT_GUARANTEE_PROCEDURE);
                        break;
                    }
                    case "android.intent.action.DISMISS_DIALOG": {
                        currentDialog.dismiss();
                        break;
                    }
                    default:
                        Log.d(TAG, "Unknown Broadcast received");
                        break;
                }
            }
        };

        // IntentFilter filters messages received by BroadcastReceiver
        IntentFilter filter = new IntentFilter();

        filter.addAction("android.intent.action.NMD_SHOW_DIALOG");
        filter.addAction("android.intent.action.AGP_SHOW_DIALOG");
        filter.addAction("android.intent.action.DISMISS_DIALOG");

        // register our receiver
        registerReceiver(receiver, filter);
    }

    /**
     * Show a dialog with the information for a specific notification.
     *
     * @param dialogID determines the information to be shown.
     */
    private void showAlertDialog(int dialogID) {
        switch (dialogID) {
            case BACK_PRESSED: {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.title_dialog_stop_passive_mode);

                // Set up the buttons
                alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PassiveMode.super.onBackPressed();
                    }
                });
                alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                currentDialog = alert.create();
                currentDialog.show();
                break;
            }

            case NO_MOVEMENT_DETECTED: {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.title_dialog_no_movement_detected);
                alert.setCancelable(false);

                // Set up the buttons
                alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendBroadcast(new Intent("android.intent.action.STOP_NOTIFICATION"));
                        dialog.cancel();
                    }
                });
                alert.setNegativeButton(R.string.sos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // DEBUG
                        Log.d(TAG, "AGP-DIALOG: SOS-Button was clicked!");
                        //
                        // TODO: Call SOS-MODE
                    }
                });

                if (currentDialog != null) {
                    if (currentDialog.isShowing()) {
                        currentDialog.cancel();
                    }
                }
                currentDialog = alert.create();
                currentDialog.show();
                break;
            }
            case ACCIDENT_GUARANTEE_PROCEDURE: {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.title_dialog_accident_guarantee_procedure_no_movement);
                alert.setCancelable(false);

                // Set up the buttons
                alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendBroadcast(new Intent("android.intent.action.STOP_NOTIFICATION"));
                        dialog.cancel();
                    }
                });
                alert.setNegativeButton(R.string.sos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // DEBUG
                        Log.d(TAG, "AGP-DIALOG: SOS-Button was clicked!");
                        //
                        // TODO: Call SOS-MODE
                    }
                });

                if (currentDialog.isShowing()) {
                    currentDialog.cancel();
                }
                currentDialog = alert.create();
                currentDialog.show();
                break;
            }
            default: {
                Log.d(TAG, "Unknown dialog!");
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        showAlertDialog(BACK_PRESSED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);

        stopService(notificationService);
        stopService(pmmService);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_CODE_SEND_SMS: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    Toast.makeText(getApplicationContext(), "SMS-sending is allowed",
//                            Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            "SMS-sendig is not allowed", Toast.LENGTH_LONG).show();
//                    return;
//                }
//            }
//        }
//
//    }
}
