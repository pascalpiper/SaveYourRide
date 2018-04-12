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

    private Intent intentTimerService;
    private final int numberOfIntervals = 6;
    private final long intervalTime = 100L;

    //BroadcastReceiver for ActiveFragment
    private BroadcastReceiver timerServiceReceiver;
    private IntentFilter filter;
    private Button buttonStartTimer;
    private Button buttonStopTimer;
    private TextView textViewTime;
    private boolean isStarted = false; /// TODO: check when it must be set.
    //

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_active, container, false);

        intentTimerService = new Intent(getActivity(), TimerService.class);
        filter = new IntentFilter();

        filter.addAction("android.intent.action.TIMER_SERVICE_READY");
        filter.addAction("android.intent.action.INTERVAL_COUNT");
        filter.addAction("android.intent.action.INTERVAL_TIME");


        buttonStartTimer = (Button) fragmentView.findViewById(R.id.buttonStartTimer);
        buttonStopTimer = (Button) fragmentView.findViewById(R.id.buttonStopTimer);
        textViewTime = (TextView) fragmentView.findViewById(R.id.textViewTimer);

        buttonStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStarted) {
                    // DEBUG
                    System.out.println("isStarted is true");
                    //
                    resetTimer(); /// TODO: RESET BUTTON
                } else {
                    getActivity().startService(intentTimerService);
                    isStarted = true;
                    //sendBroadcastToTimerService("start");
                }
            }
        });
        buttonStopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().stopService(intentTimerService);
            }
        });

        //BroadcastReceiver for ActiveFraagment
        /*
         * This BroadcastReceiver receive the intervallCounter and a finish-message from the
         * service "TimerService". They will be shown in this fragment.
         */
        timerServiceReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                switch (intent.getAction()){
                    case "android.intent.action.TIMER_SERVICE_READY" : {
                        sendBroadcastToTimerService("start");
                        break;
                    }
                    case "android.intent.action.INTERVAL_COUNT" : {
                        String intervalCount = intent.getStringExtra("interval_count");
                        System.out.println("Fragment-Receiver: " + intervalCount);
                        buttonStartTimer.setText(intervalCount);
                        break;
                    }
                    case "android.intent.action.INTERVAL_TIME" : {
                        String intervalTimeSec = intent.getStringExtra("interval_time_sec");
                        String intervalTimeMin = intent.getStringExtra("interval_time_min");
                        System.out.println("Fragment-Receiver: " + intervalTimeMin + " Min : "+ intervalTimeSec + " sec ");
                        setTime(intervalTimeMin, intervalTimeSec);
                        break;
                    }
                default:
                    System.out.println("No Broadcast Receive");
               }


            }
        };
        //registering our receiver
        getActivity().registerReceiver(timerServiceReceiver, filter);
        ///End - BroadcastReceiver for ActiveFragment

        return fragmentView;
    }

    private void setTime(String intervalTimeMin, String intervalTimeSec) {
        String time = String.format("%02d:%02d", intervalTimeMin, intervalTimeSec);
        textViewTime.setText(time);

    }

    public void resetTimer() {
        sendBroadcastToTimerService("reset");
    }

    /**
     * This method will send a broadcast to the service TimerService
     */
    public void sendBroadcastToTimerService(String broadcast) {

        switch (broadcast) {

            case "reset": {
                Intent i = new Intent("android.intent.action.INTERVAL_RESET");
                getActivity().sendBroadcast(i);
                break;
            }
            case "start": {
                Intent i = new Intent("android.intent.action.INTERVAL_START").putExtra("numberOfIntervals", numberOfIntervals).putExtra("intervalTime", intervalTime);
                getActivity().sendBroadcast(i);
                break;
            }
            default: {
                Toast.makeText(getActivity(),"No such broadcast!", Toast.LENGTH_LONG).show();
            }
        }
    }

}