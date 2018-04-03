package com.example.saveyourride;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class Activity_Active extends AppCompatActivity {

    final int maxIntervalls = 6;
    final int intervallTime = 10000;
    int intervallCounter = 0;
    boolean timerStart = true;

    Button button;
    ProgressBar progressBar;

    private Handler customHandler = new Handler();

    int aufruf = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__active);

        //get Views
        button = (Button) findViewById(R.id.button);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        ///
        button.setText("Starten");
        button.setBackgroundColor(Color.GRAY);

        progressBar.setMax(maxIntervalls);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(timerStart) {
                    System.out.println("Custom Handler - Button");
                    intervallCounter = 0;
                    timerStart = false;
                    customHandler.post(intervallThread);
                }
                else resetTimer();
            }
        });
    }

    private Runnable intervallThread = new Runnable() {

        public void run() {

            if(intervallCounter < maxIntervalls){
                intervallBenachrichtigung(intervallCounter);
                intervallCounter++;
                customHandler.postDelayed(this, intervallTime);


            }
            else{
                System.out.println("Notruf");
                button.setText("Notruf");
            }

        }
    };

    private void intervallBenachrichtigung(int intervallCounter) {
        int übrigeIntervalle = maxIntervalls - intervallCounter;
        System.out.println("übrige Intervalle: " + übrigeIntervalle);
        if(intervallCounter < 2)
            button.setBackgroundColor(Color.GREEN);
        else if(übrigeIntervalle <=2 )
            button.setBackgroundColor(Color.RED);
        else
            button.setBackgroundColor(Color.YELLOW);

       button.setText(intervallCounter + " / " + maxIntervalls);
       progressBar.setProgress(intervallCounter);
    }

    public void resetTimer(){
        customHandler.removeCallbacks(intervallThread);
        intervallCounter = 0;
        customHandler.post(intervallThread);
    }

    

}
