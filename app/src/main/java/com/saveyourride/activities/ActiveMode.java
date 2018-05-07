package com.saveyourride.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.saveyourride.R;
import com.saveyourride.services.ActiveModeManager;
import com.saveyourride.services.NotificationManager;

import java.util.concurrent.TimeUnit;

public class ActiveMode extends AppCompatActivity {

    // DEBUG
    private final String TAG = "ActiveMode";
    //

    // TODO: Pick Up values for number of intervals and interval time
    private final int numberOfIntervals = 6;
    private final long intervalTime = 60000L;

    //Dialog IDs
    private final int BACK_PRESSED = 0;
    private final int INTERVAL_TIME_EXPIRED = 1;

    //Dialog
    AlertDialog currentDialog;
    private long notificationTime = 6000L;
    private CountDownTimer dialogTimer;

    // BroadcastReceiver for messages from ActiveModeManager (AMM)
    private BroadcastReceiver ammReceiver;

    // TextViews
    private TextView textViewIntervalNumber;
    private TextView textViewTime;

    // Intents for Services | ActiveModeManager (AMM)
    private Intent notificationService, ammService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_active_mode);

        // Views
        Button buttonResetTimer = (Button) findViewById(R.id.buttonResetTimer);
        Button buttonStopTimer = (Button) findViewById(R.id.buttonStopTimer);
        textViewTime = (TextView) findViewById(R.id.textViewTimer);
        textViewIntervalNumber = (TextView) findViewById(R.id.textViewIntervalCount);

        // Keeps Activity ON also on lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize BroadcastReceiver
        initAmmReceiver();

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
     * Creates new {@code BroadcastReceiver} and {@code IntentFilter} for messages from {@code ActiveModeManager} and registers them.
     * {@code ammReceiver} receives the broadcasts from the ActiveModeManager
     * It can receive following broadcasts:
     * - status, that the AMM-Service is ready
     * - intervalNumber
     * - restIntervalTime
     * - the time of the interval has expired
     */
    private void initAmmReceiver() {
        ammReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                switch (intent.getAction()) {
                    case "android.intent.action.AMM_SERVICE_READY": {
                        sendBroadcast(new Intent("android.intent.action.START_TIMER").putExtra("numberOfIntervals", numberOfIntervals).putExtra("intervalTime", intervalTime));
                        break;
                    }
                    case "android.intent.action.INTERVAL_NUMBER": {
                        int intervalNumber = intent.getIntExtra("interval_number", -1);
                        textViewIntervalNumber.setText(Integer.toString(intervalNumber) + " / " + numberOfIntervals);
                        break;
                    }
                    case "android.intent.action.INTERVAL_TIME_EXPIRED": {
                        // Dialog
                        showAlertDialog(INTERVAL_TIME_EXPIRED);

                        // TODO make time as final in NotificationManager
                        long time = 2800L;
                        Intent startNotificationIntent = new Intent("android.intent.action.START_NOTIFICATION").putExtra("notificationSoundTime", time);
                        sendBroadcast(startNotificationIntent);

                        break;
                    }
                    case "android.intent.action.REST_INTERVAL_TIME": {
                        long restIntervalMillis = intent.getLongExtra("rest_interval_millis", -1);
                        // DEBUG
                        Log.d(TAG, "BroadcastReceiver received rest interval millis : " + restIntervalMillis);
                        //
                        setTextViewTime(restIntervalMillis);
                        break;
                    }
                    default:
                        Toast.makeText(getApplicationContext(), "Unknown Broadcast received", Toast.LENGTH_LONG).show();
                }
            }
        };

        // IntentFilter filters messages received by BroadcastReceiver
        IntentFilter filter = new IntentFilter();

        filter.addAction("android.intent.action.AMM_SERVICE_READY");
        filter.addAction("android.intent.action.INTERVAL_NUMBER");
        filter.addAction("android.intent.action.REST_INTERVAL_TIME");
        filter.addAction("android.intent.action.INTERVAL_TIME_EXPIRED");

        // register our receiver
        registerReceiver(ammReceiver, filter);
    }

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

                Button dialogResetButton = dialogLayout.findViewById(R.id.dialogButtonResetTimer);

                dialogResetButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendBroadcast(new Intent("android.intent.action.RESET_TIMER"));
                        //TODO: Hier muss Broadcast an NotificationService gesendet werden, um NotificationSound zu stoppen
                        ///
                        sendBroadcast(new Intent("android.intent.action.STOP_NOTIFICATION"));

                        currentDialog.dismiss();
                        dialogTimer.cancel();

                    }
                });

                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                // set the view from XML inside AlertDialog
                alert.setView(dialogLayout);

                currentDialog = alert.create();
                currentDialog.show();

                startNotificationBeShownTimer(notificationTime);
            }

            break;
        }
    }

    public void startNotificationBeShownTimer(long notificationTime) {
        dialogTimer = new CountDownTimer(notificationTime, notificationTime) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Nothing happend here
            }

            @Override
            public void onFinish() {
                if (currentDialog.isShowing()) {
                    currentDialog.dismiss();
                }
                //sendBroadcast(new Intent("android.intent.action.STOP_NOTIFICATION"));
            }
        }.start();
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

        unregisterReceiver(ammReceiver);

        stopService(notificationService);
        stopService(ammService);

        startActivity(new Intent(this.getApplicationContext(), MainScreen.class));
    }
}




