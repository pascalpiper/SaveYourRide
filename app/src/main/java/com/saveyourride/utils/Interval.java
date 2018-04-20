package com.saveyourride.utils;

import android.os.CountDownTimer;

import com.saveyourride.services.TimerService;

/*
 * Created by taraszaika on 12.04.18.
 * new Interval
 */
public class Interval {

    private final long MILLISECONDS_IN_SECOND = 1000;
    private final int SECONDS_IN_MINUTE = 60;
    private final TimerService timerService;
    private int seconds, minutes;
    private long intervalTime;
    private CountDownTimer timer;
    private int intervalCount;

    public Interval(long intervalTime, TimerService timerService, int intervalCount) {
        this.timerService = timerService;
        this.intervalTime = intervalTime;
        this.seconds = 0;
        this.minutes = 0;
        this.intervalCount = intervalCount;
    }

    public void start() {
        timer = new CountDownTimer(intervalTime, MILLISECONDS_IN_SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                // TEST
                System.out.println("millisUntilFinished = " + millisUntilFinished + "(in Seconds: " + ((int) millisUntilFinished / 1000) + ")");
                //
                timerService.setValues(minutes, seconds);
                if (seconds < SECONDS_IN_MINUTE) {
                    seconds = seconds + 1;
                } else {
                    minutes = minutes + 1;
                    seconds = 0;
                }
            }

            @Override
            public void onFinish() {
                System.out.println("ON FINISH!");
                intervalCount++;
                timerService.runInterval(intervalCount);

            }
        };
        timer.start();
    }

    public void stop() {
        timer.cancel();

    }

}
