package com.example.saveyourride.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.saveyourride.R;
import com.example.saveyourride.services.IntervallTimer;

public class Active extends Fragment {

    public int counter;
    public long totalTime, interval;
    private Button buttonStartTimer;
    private Button buttonStopTimer;
    private TextView textViewTimerCount;
    private boolean isStarted = false;
    private final Intent intentStartIntervallTimer = new Intent(getActivity(), IntervallTimer.class);

    private int maxIntervals = 6;

    //BroadcastReceiver for ActiveFragment
    BroadcastReceiver mReceiver;
    IntentFilter intentFilter = new IntentFilter(
            "android.intent.action.INTERVAL_COUTNER");
    //

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

        //BroadcastReceiver for ActiveFraagment
        /**
         * This BroadcastReceiver receive the intervallCounter and a finish-message from the
         * service "IntervallTimer". They will be shown in this fragment.
         */
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                String stringIntervallCounter = intent.getStringExtra("intervall_counter");
                System.out.println("Fragment-Receiver: "+ stringIntervallCounter);
                buttonStartTimer.setText(stringIntervallCounter);
            }
        };
        //registering our receiver
        getActivity().registerReceiver(mReceiver, intentFilter);
        ///End - BroadcastReceiver for ActiveFragment

        return fragmentView;
    }

    /**
     * Method for showing the current Intervall
     * @param intervallCounter
     */
    private void intervallBenachrichtigung(int intervallCounter) {
        int übrigeIntervalle = maxIntervals - intervallCounter;
        System.out.println("übrige Intervalle: " + übrigeIntervalle);
        if (intervallCounter < 2)
            buttonStartTimer.setBackgroundColor(Color.GREEN);
        else if (übrigeIntervalle <= 2)
            buttonStartTimer.setBackgroundColor(Color.RED);
        else
            buttonStartTimer.setBackgroundColor(Color.YELLOW);

        buttonStartTimer.setText(intervallCounter + " / " + maxIntervals);
    }

    /**
     * This Method reset the Timer. The IntervallCounter will be set to 0
     */
    public void resetTimer() {

    }


    @Override
    public void onPause() {
        super.onPause();
        //unregister our receiver
        // getActivity().unregisterReceiver(this.mReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        //register our receiver
        getActivity().registerReceiver(mReceiver, intentFilter);
    }
}