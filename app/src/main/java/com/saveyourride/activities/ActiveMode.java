package com.saveyourride.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.saveyourride.R;
import com.saveyourride.services.ActiveModeManager;
import com.saveyourride.services.NotificationManager;

import java.util.concurrent.TimeUnit;

public class ActiveMode extends AppCompatActivity {

    // DEBUG
    private final String TAG = "ActiveMode";
    //

    // Dialog IDs
    private final int BACK_PRESSED = 0;
    private final int INTERVAL_TIME_EXPIRED = 1;
    private final int ACCIDENT_GUARANTEE_PROCEDURE = 2;

    // Dialog
    AlertDialog currentDialog;

    // BroadcastReceiver
    private BroadcastReceiver receiver;

    // Intents for Services | ActiveModeManager (AMM)
    private Intent notificationService, ammService;

    // TextViews
    private TextView textViewIntervalNumber;
    private TextView textViewTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_active_mode);

        // Views
        Button buttonResetTimer = (Button) findViewById(R.id.activeMode_buttonReset);
        Button buttonStopTimer = (Button) findViewById(R.id.activeMode_buttonStop);
        textViewTime = (TextView) findViewById(R.id.activeMode_textViewTime);
        textViewIntervalNumber = (TextView) findViewById(R.id.activeMode_textViewNumberOfIntervals);

        // Keeps Activity ON also on lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize BroadcastReceiver
        initReceiver();

        // Create intents for Services
        ammService = new Intent(this.getApplicationContext(), ActiveModeManager.class);
        notificationService = new Intent(this.getApplicationContext(), NotificationManager.class);

        // Start Services
        startService(ammService);
        startService(notificationService);

        // Button listeners
        buttonResetTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent("android.intent.action.RESET_TIMER"));
            }
        });
        buttonStopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /**
     * Creates new {@code BroadcastReceiver} and {@code IntentFilter} and then registers them.
     * {@code receiver} receives the broadcasts from the {@code ActiveModeManager} and {@code NotificationManager}.
     * It can receive following broadcasts:
     * - status, that the AMM-Service is ready
     * - intervalNumber
     * - restIntervalTime
     * - the time of the interval has expired
     */
    private void initReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                switch (intent.getAction()) {
                    case "android.intent.action.AMM_SERVICE_READY": {
                        SharedPreferences timerValues = getSharedPreferences(getString(R.string.sp_key_timer_values), Context.MODE_PRIVATE);
                        int numberOfIntervals = timerValues.getInt(                             // Value from shared preferences, getInt(key, defaultValue)
                                getString(R.string.sp_key_number_of_interval),                  // key (String)
                                getResources().getInteger(R.integer.default_number_of_intervals)// defaultValue (int)
                        );
                        long timeOfInterval = timerValues.getLong(                              // Value from shared preferences, getLong(key, defaultValue)
                                getString(R.string.sp_key_time_of_interval),                    // key (String)
                                getResources().getInteger(R.integer.default_time_of_interval)   // defaultValue (long or int)
                        );
                        sendBroadcast(new Intent("android.intent.action.START_TIMER").putExtra("numberOfIntervals", numberOfIntervals).putExtra("timeOfInterval", timeOfInterval));
                        break;
                    }
                    case "android.intent.action.REST_INTERVAL_TIME": {
                        long restIntervalMillis = intent.getLongExtra("rest_interval_millis", -1);
                        setTextViewTime(restIntervalMillis);
                        break;
                    }
                    case "android.intent.action.INTERVAL_NUMBER": {
                        int intervalNumber = intent.getIntExtra("interval_number", -1);
                        int numberOfIntervals = intent.getIntExtra("number_of_intervals", -1);
                        textViewIntervalNumber.setText(intervalNumber + " / " + numberOfIntervals);
                        break;
                    }
                    case "android.intent.action.ITE_SHOW_DIALOG": {
                        showAlertDialog(INTERVAL_TIME_EXPIRED);
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

        filter.addAction("android.intent.action.AMM_SERVICE_READY");
        filter.addAction("android.intent.action.REST_INTERVAL_TIME");
        filter.addAction("android.intent.action.INTERVAL_NUMBER");
        filter.addAction("android.intent.action.ITE_SHOW_DIALOG");
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
                alert.setTitle(R.string.title_dialog_stop_active_mode);

                // Set up the buttons
                alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActiveMode.super.onBackPressed();
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

            case INTERVAL_TIME_EXPIRED: {
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.dialog_active_mode_notification, null);

                Button dialogResetButton = dialogLayout.findViewById(R.id.activeMode_dialog_buttonReset);

                dialogResetButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendBroadcast(new Intent("android.intent.action.RESET_TIMER"));
                        sendBroadcast(new Intent("android.intent.action.STOP_NOTIFICATION"));
                        currentDialog.dismiss();
                    }
                });

                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                // set the view from XML inside AlertDialog
                alert.setView(dialogLayout);
                alert.setCancelable(false);

                currentDialog = alert.create();
                currentDialog.show();
                break;
            }
            case ACCIDENT_GUARANTEE_PROCEDURE: {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.title_dialog_accident_guarantee_procedure_interval);
                alert.setCancelable(false);

                // Set up the buttons
                alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendBroadcast(new Intent("android.intent.action.STOP_NOTIFICATION"));
                        sendBroadcast(new Intent("android.intent.action.RESET_TIMER"));
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

    /**
     * Set the text in the textView. The text contains the remaining interval time.
     *
     * @param restIntervalMillis remaining millis.
     */
    private void setTextViewTime(long restIntervalMillis) {
        String remainingTime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(restIntervalMillis),
                TimeUnit.MILLISECONDS.toSeconds(restIntervalMillis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(restIntervalMillis))
        );
        textViewTime.setText(remainingTime);
    }

    @Override
    public void onBackPressed() {
        showAlertDialog(BACK_PRESSED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sendBroadcast(new Intent("android.intent.action.STOP_TIMER"));

        unregisterReceiver(receiver);

        stopService(notificationService);
        stopService(ammService);
    }
}




