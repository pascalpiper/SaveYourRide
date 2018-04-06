package com.example.saveyourride.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;

public class IntervallTimer extends IntentService {

    int intervallCounter = 0;
    int intervallTime = 10000;
    int maxIntervalls = 6;

    Handler customHandler = new Handler();


    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public IntervallTimer() {
        super("IntervallTimer");
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // Normally we would do some work here, like download a file.
        // For our sample, we just sleep for 5 seconds.
        try {
//            Thread.sleep(5000);

            customHandler.post(intervallThread);


        } catch (Exception e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
    }

    private Runnable intervallThread = new Runnable() {

        public void run() {

            if (intervallCounter < maxIntervalls) {
                //intervallBenachrichtigung(intervallCounter);
                System.out.println(intervallCounter);
                intervallCounter++;
                customHandler.postDelayed(this, intervallTime);


            } else {
                System.out.println("Notruf");
               // button.setText("Notruf");
            }

        }
    };

}
