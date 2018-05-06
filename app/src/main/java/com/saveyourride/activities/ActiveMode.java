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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.saveyourride.R;
import com.saveyourride.services.ActiveModeManager;
import com.saveyourride.services.NotificationSound;

public class ActiveMode extends AppCompatActivity {

    // TODO: Pick Up values for number of intervals and interval time
    private final int numberOfIntervals = 6;
    private final long intervalTime = 10000L;

    //Dialog IDs
    private final int BACK_PRESSED = 0;
    private final int INTERVAL_EXPIRED = 1;

    //Dialog
    AlertDialog currentDialog;
    private long notificationTime = 6000L;
    private CountDownTimer notificationBeShownTimer;

    Intent stopNotificationIntent = new Intent("android.intent.action.STOP_NOTIFICATION");


    // BroadcastReceiver for messages from ActiveModeManager
    private BroadcastReceiver timerServiceReceiver;

    // IntentFilter filters messages received by BroadcastReceiver
    private IntentFilter filter;

    private TextView textViewIntervalCount;
    private TextView textViewTime;

    private Intent notificationService, intentTimerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_mode);
        intentTimerService = new Intent(this.getApplicationContext(), ActiveModeManager.class);
        notificationService = new Intent(this.getApplicationContext(), NotificationSound.class);


        filter = new IntentFilter();

        filter.addAction("android.intent.action.TIMER_SERVICE_READY");
        filter.addAction("android.intent.action.INTERVAL_COUNT");
        filter.addAction("android.intent.action.REST_INTERVAL_TIME");
        filter.addAction("android.intent.action.INTERVAL_EXPIRED");

        Button buttonStartTimer = (Button) findViewById(R.id.buttonResetTimer);
        Button buttonStopTimer = (Button) findViewById(R.id.buttonStopTimer);
        textViewTime = (TextView) findViewById(R.id.textViewTimer);
        textViewIntervalCount = (TextView) findViewById(R.id.textViewIntervalCount);

        buttonStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        buttonStopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActiveMode.this.finish();
            }
        });

        /// BroadcastReceiver for ActiveFragment
        /*
         * This BroadcastReceiver receives the broadcasts from the ActiveModeManager
         * It can receive following broadcasts:
         * - intervalCount
         * - restIntervalTime
         * - finish-message from the Timer
         * - status, that the Service is ready
         */
        timerServiceReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                switch (intent.getAction()) {
                    case "android.intent.action.TIMER_SERVICE_READY": {
                        // DEBUG
                        System.out.println("Fragment-Receiver received 'service-ready'-broadcast");
                        //
                        sendBroadcastToTimerService("startTimer");
                        break;
                    }
                    case "android.intent.action.INTERVAL_COUNT": {
                        int intervalCount = intent.getIntExtra("interval_count", -1);
                        System.out.println("Fragment-Receiver received interval count: " + intervalCount);
                        textViewIntervalCount.setText(Integer.toString(intervalCount + 1) + " / " + numberOfIntervals);
                        break;
                    }
                    case "android.intent.action.INTERVAL_EXPIRED": {
                        // Dialog
                        showAlertDialog(INTERVAL_EXPIRED);

                        long time = 2800L;
                        Intent startNotificationIntent = new Intent("android.intent.action.START_NOTIFICATION").putExtra("notificationSoundTime", time);
                        sendBroadcast(startNotificationIntent);

                        break;
                    }
                    case "android.intent.action.REST_INTERVAL_TIME": {
                        int intervalTimeMin = intent.getIntExtra("rest_interval_time_min", -1);
                        int intervalTimeSec = intent.getIntExtra("rest_interval_time_sec", -1);
                        System.out.println("Fragment-Receiver received interval time : " + intervalTimeMin + " Min : " + intervalTimeSec + " Sec ");
                        setTextViewTime(intervalTimeMin, intervalTimeSec);
                        break;
                    }
                    default:
                        Toast.makeText(getApplicationContext(), "Unknown Broadcast received", Toast.LENGTH_LONG).show();
                }
            }
        };

        // register our receiver
        registerReceiver(timerServiceReceiver, filter);
        ///End - BroadcastReceiver for ActiveFragment

        startService(intentTimerService);
        startService(notificationService);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    private void setTextViewTime(int intervalTimeMin, int intervalTimeSec) {
        String time = String.format("%02d", intervalTimeMin) + ":" + String.format("%02d", intervalTimeSec);
        textViewTime.setText(time);
    }

    private void resetTimer() {
        sendBroadcastToTimerService("resetTimer");
    }

    /**
     * This method will send a broadcast to the service ActiveModeManager.
     * case start => TierService becomes the numberOfIntervals and the intervalTime and start the timer.
     * case reset => ActiveModeManager will reset the timer and start the new one.
     * case stop => ActiveModeManager will stop the timer
     */
    private void sendBroadcastToTimerService(String broadcast) {
        switch (broadcast) {
            case "startTimer": {
                Intent startTimerIntent = new Intent("android.intent.action.START_TIMER").putExtra("numberOfIntervals", numberOfIntervals).putExtra("intervalTime", intervalTime);
                sendBroadcast(startTimerIntent);
                break;
            }
            case "resetTimer": {
                Intent resetTimerIntent = new Intent("android.intent.action.RESET_TIMER");
                sendBroadcast(resetTimerIntent);
                break;
            }
            case "stopTimer": {
                Intent stopTimerIntent = new Intent("android.intent.action.STOP_TIMER");
                sendBroadcast(stopTimerIntent);
                break;
            }
            default: {
                Toast.makeText(this, "Unknown broadcast!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                System.out.println("Home");
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        showAlertDialog(BACK_PRESSED);
//        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(timerServiceReceiver);
        stopService(notificationService);
        sendBroadcastToTimerService("stopTimer");
        stopService(intentTimerService);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(timerServiceReceiver, filter);
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
                        // ja button
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

            case INTERVAL_EXPIRED: {
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.dialog_active_mode_notification, null);

                Button dialogResetButton = dialogLayout.findViewById(R.id.dialogButtonResetTimer);

                dialogResetButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendBroadcastToTimerService("resetTimer");
                        //TODO: Hier muss Broadcast an NotificationService gesendet werden, um NotificationSound zu stoppen
                        ///
                        sendBroadcast(stopNotificationIntent);

                        currentDialog.dismiss();
                        notificationBeShownTimer.cancel();

                    }
                });

                AlertDialog.Builder alert = new AlertDialog.Builder(this);
//                    alert.setTitle(R.string.join_event);
                // this is set the view from XML inside AlertDialog
                alert.setView(dialogLayout);

                currentDialog = alert.create();
                currentDialog.show();

                startNotificationBeShownTimer(notificationTime);
            }

            break;
        }
    }

    public void startNotificationBeShownTimer(long notificationTime) {
        notificationBeShownTimer = new CountDownTimer(notificationTime, notificationTime) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Nothing happend here
            }

            @Override
            public void onFinish() {
                if (currentDialog.isShowing()) {
                    currentDialog.dismiss();
                }
                sendBroadcast(stopNotificationIntent);
            }
        }.start();
    }


}




