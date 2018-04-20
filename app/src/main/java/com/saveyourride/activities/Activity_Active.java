package com.saveyourride.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.saveyourride.R;

public class Activity_Active extends AppCompatActivity {

    public int counter;
    public long totalTime, interval;
    private Button buttonStartTimer;
    private Button buttonStopTimer;
    private TextView textViewTimerCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__active);
        buttonStartTimer = (Button) findViewById(R.id.buttonStartTimer1);
        buttonStopTimer = (Button) findViewById(R.id.buttonStopTimer1);
        textViewTimerCount = (TextView) findViewById(R.id.textViewTimerCount1);

        totalTime = 10000;
        interval = 1000;

        buttonStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CountDownTimer(totalTime, interval) {
                    public void onTick(long millisUntilFinished) {
                        textViewTimerCount.setText(String.valueOf(counter));
                        counter++;
                    }

                    public void onFinish() {
                        textViewTimerCount.setText("FINISH!!");
                    }
                }.start();
            }
        });
        buttonStopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /// NICHTS PASSIERT
            }
        });
    }
}
