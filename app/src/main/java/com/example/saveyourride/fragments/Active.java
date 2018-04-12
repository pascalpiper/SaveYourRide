package com.example.saveyourride.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.saveyourride.R;
import com.example.saveyourride.activities.MainScreen;
import com.example.saveyourride.services.IntervallTimer;

public class Active extends Fragment {

    public int counter;
    public long totalTime, interval;
    private Button buttonStartTimer;
    private Button buttonStopTimer;
    private TextView textViewTimerCount;
    private boolean isStarted = false;
    private final Intent intentStartIntervallTimer = new Intent(getActivity(), IntervallTimer.class);;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_active, container, false);

        buttonStartTimer = (Button) fragmentView.findViewById(R.id.buttonStartTimer);
        buttonStopTimer = (Button) fragmentView.findViewById(R.id.buttonStopTimer);
        textViewTimerCount = (TextView) fragmentView.findViewById(R.id.textViewTimerCount);

        totalTime = 10000;
        interval = 1000;

        buttonStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isStarted){
                    // TODO: Reset Timer
                }
                else {
                    getActivity().startService(intentStartIntervallTimer);
                }
            }

        });
        buttonStopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().stopService(intentStartIntervallTimer);
            }
        });

        return fragmentView;
    }

}