package com.saveyourride.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;

public class SosModeManager extends Service {

    //TODO Location for SMS

    // DEBUG
    private final String TAG = "SosModeManager";
    //

    // BroadcastReceiver for messages from ActiveMode Activity
    private BroadcastReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize BroadcastReceiver
        initActivityReceiver();

        // send directly the sos-sms
        sendSosSms();
    }

    private void sendSosSms() {
        String messageSos = "Hallo Pascal, hier ist die SaveYourRide App von Pascals Handy";

        String phoneNo = "01752847846";

        ArrayList<String> phoneList = new ArrayList<String>();
        phoneList.add(phoneNo);

        for (String phoneNumber:phoneList) {
            sendSms(phoneNumber, messageSos);
        }
    }

    /**
     * Creates new {@code BroadcastReceiver} and {@code IntentFilter} and then registers them.
     * {@code receiver} receives the broadcasts from the {@code SosMode} activity.
     */
    private void initActivityReceiver() {
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "android.intent.action.SEND_FALSE_ALARM_SMS": {

                        // Test

                        String messageNoSos = "Hallo Pascal, hier ist die SaveYourRide App. Die letzte Meldung war ein Fehlalarm";

                        String phoneNo = "01752847846";

                        ArrayList<String> phoneList = new ArrayList<String>();
                        phoneList.add(phoneNo);

                        for (String phoneNumber:phoneList) {
                            sendSms(phoneNumber, messageNoSos);
                        }

                        ///

                        break;
                    }
                    default: {
                        Log.d(TAG, "NO SUCH ACTION IN BROADCAST!");
                        break;
                    }
                }
            }
        };

        // IntentFilter filters broadcasts received by BroadcastReceiver
        IntentFilter filter = new IntentFilter();

        filter.addAction("android.intent.action.SEND_SOS_SMS");
        filter.addAction("android.intent.action.SEND_FALSE_ALARM_SMS");

        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    /**
     * Send a sms with the {@code message} to a member with the {@code phoneNo}
     * @param phoneNo the number of the contact how gets the sms
     * @param message the content of the sms
     */
    private void sendSms( String phoneNo, String message){

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNo, null, message, null, null);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
