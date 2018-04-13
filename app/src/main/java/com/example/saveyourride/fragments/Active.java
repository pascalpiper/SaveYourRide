package com.example.saveyourride.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.saveyourride.R;
import com.example.saveyourride.services.TimerService;

public class Active extends Fragment {

    // TODO: Pick Up values for number of intervals and interval time
    private final int numberOfIntervals = 6;
    private final long intervalTime = 10000L;

    private Intent intentTimerService;

    // BroadcastReceiver for messages from TimerService
    private BroadcastReceiver timerServiceReceiver;

    // IntentFilter filters messages received by BroadcastReceiver
    private IntentFilter filter;

    private Button buttonStartTimer;
    private Button buttonStopTimer;
    private TextView textViewIntervalCount;
    private TextView textViewTime;

    private boolean isStarted = false; // TODO: check when it must be set.

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_active, container, false);

        intentTimerService = new Intent(getActivity(), TimerService.class);
        filter = new IntentFilter();

        filter.addAction("android.intent.action.TIMER_SERVICE_READY");
        filter.addAction("android.intent.action.INTERVAL_COUNT");
        filter.addAction("android.intent.action.REST_INTERVAL_TIME");

        buttonStartTimer = (Button) fragmentView.findViewById(R.id.buttonStartTimer);
        buttonStopTimer = (Button) fragmentView.findViewById(R.id.buttonStopTimer);
        textViewTime = (TextView) fragmentView.findViewById(R.id.textViewTimer);
        textViewIntervalCount = (TextView) fragmentView.findViewById(R.id.textViewIntervalCount);

        buttonStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStarted) {
                    // DEBUG
                    System.out.println("isStarted is true");
                    //
                    resetTimer(); // TODO: RESET BUTTON
                } else {
                    getActivity().startService(intentTimerService);
                    isStarted = true;
                }
            }
        });

        buttonStopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcastToTimerService("stopTimer");
                //getActivity().stopService(intentTimerService);
            }
        });

        /// BroadcastReceiver for ActiveFragment
        /*
         * This BroadcastReceiver receives the broadcasts from the TimerService
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
                        textViewIntervalCount.setText(Integer.toString(intervalCount));
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
                        Toast.makeText(getActivity(), "Unknown Broadcast received", Toast.LENGTH_LONG).show();
                }
            }
        };

        // register our receiver
        getActivity().registerReceiver(timerServiceReceiver, filter);
        ///End - BroadcastReceiver for ActiveFragment

        return fragmentView;
    }

    private void setTextViewTime(int intervalTimeMin, int intervalTimeSec) {
        String time = String.format("%02d", intervalTimeMin) + ":" + String.format("%02d", intervalTimeSec);
        textViewTime.setText(time);
    }

    private void resetTimer() {
        sendBroadcastToTimerService("resetTimer");
    }

    /**
     * This method will send a broadcast to the service TimerService.
     * case start => TierService becomes the numberOfIntervals and the intervalTime and start the timer.
     * case reset => TimerService will reset the timer and start the new one.
     */
    private void sendBroadcastToTimerService(String broadcast) {
        switch (broadcast) {
            case "startTimer": {
                Intent startTimerIntent = new Intent("android.intent.action.START_TIMER").putExtra("numberOfIntervals", numberOfIntervals).putExtra("intervalTime", intervalTime);
                getActivity().sendBroadcast(startTimerIntent);
                break;
            }
            case "resetTimer": {
                Intent resetTimerIntent = new Intent("android.intent.action.RESET_TIMER");
                getActivity().sendBroadcast(resetTimerIntent);
                break;
            }
            case "stopTimer": {
                Intent stopTimerIntent = new Intent("android.intent.action.STOP_TIMER");
                getActivity().sendBroadcast(stopTimerIntent);
                break;
            }
            default: {
                Toast.makeText(getActivity(), "Unknown broadcast!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(timerServiceReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(timerServiceReceiver, filter);
    }
}