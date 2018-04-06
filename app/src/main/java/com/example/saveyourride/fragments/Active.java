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

import com.example.saveyourride.R;
import com.example.saveyourride.activities.MainScreen;
import com.example.saveyourride.services.IntervallTimer;

public class Active extends Fragment {

    final int maxIntervalls = 6;
    final int intervallTime = 10000;
    int intervallCounter = 0;
    boolean timerStart = true;

    Button button;

    //BroadcastReceiver for ActiveFragment
    BroadcastReceiver mReceiver;
    IntentFilter intentFilter = new IntentFilter(
            "android.intent.action.FINISHED");

    private Handler customHandler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_active, container, false);

        //get Views
        button = (Button) fragmentView.findViewById(R.id.button);
        ///

        button.setText("Starten");
        button.setBackgroundColor(Color.GRAY);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (timerStart) {
//                    System.out.println("Custom Handler - Button");
//                    intervallCounter = 0;
//                    timerStart = false;
//                    customHandler.post(intervallThread);
                    getActivity().startService(new Intent(getActivity(), IntervallTimer.class));
                    time();

                } else resetTimer();
            }
        });


        //BroadcastReceiver for ActiveFraagment


        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                String msg_for_me = intent.getStringExtra("some_msg");
                System.out.println("Fragment-Receiver: "+ msg_for_me);

                button.setText(msg_for_me);


            }
        };

        //registering our receiver
        getActivity().registerReceiver(mReceiver, intentFilter);

        ///End - BroadcastReceiver for ActiveFragment

        return fragmentView;
    }

    private Runnable intervallThread = new Runnable() {

        public void run() {

            if (intervallCounter < maxIntervalls) {
                intervallBenachrichtigung(intervallCounter);
                intervallCounter++;
                customHandler.postDelayed(this, intervallTime);


            } else {
                System.out.println("Notruf");
                button.setText("Notruf");
            }

        }
    };

    private void intervallBenachrichtigung(int intervallCounter) {
        int übrigeIntervalle = maxIntervalls - intervallCounter;
        System.out.println("übrige Intervalle: " + übrigeIntervalle);
        if (intervallCounter < 2)
            button.setBackgroundColor(Color.GREEN);
        else if (übrigeIntervalle <= 2)
            button.setBackgroundColor(Color.RED);
        else
            button.setBackgroundColor(Color.YELLOW);

        button.setText(intervallCounter + " / " + maxIntervalls);
    }

    public void resetTimer() {
        customHandler.removeCallbacks(intervallThread);
        intervallCounter = 0;
        customHandler.post(intervallThread);

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

    public void time(){
        new CountDownTimer(300000, 1000) {

            public void onTick(long millisUntilFinished) {
                System.out.println("seconds remaining: " + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                System.out.println("done!");
            }

        }.start();
    }
}