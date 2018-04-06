package com.example.saveyourride.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
    ProgressBar progressBar;

    private Handler customHandler = new Handler();

    int aufruf = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_active, container, false);

        //get Views
        button = (Button) fragmentView.findViewById(R.id.button);
        progressBar = (ProgressBar) fragmentView.findViewById(R.id.progressBar);
        ///

        button.setText("Starten");
        button.setBackgroundColor(Color.GRAY);

        progressBar.setMax(maxIntervalls);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (timerStart) {
//                    System.out.println("Custom Handler - Button");
//                    intervallCounter = 0;
//                    timerStart = false;
//                    customHandler.post(intervallThread);
getActivity().startService(new Intent(getActivity(),IntervallTimer.class));
                } else resetTimer();
            }
        });

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
        progressBar.setProgress(intervallCounter);
    }

    public void resetTimer() {
        customHandler.removeCallbacks(intervallThread);
        intervallCounter = 0;
        customHandler.post(intervallThread);

    }
}