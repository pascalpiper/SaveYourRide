package com.saveyourride.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import com.saveyourride.utils.MessageBuilder;

import java.util.ArrayList;

public class SosModeManager extends Service {

    //TODO Location for SMS

    // DEBUG
    private final String TAG = "SosModeManager";
    //

    // BroadcastReceiver for messages from ActiveMode Activity
    private BroadcastReceiver receiver;


    // TestPhoneNumber
    ArrayList<String> phoneList = new ArrayList<String>();

    // SMS-Manager
    private SmsManager smsManager = SmsManager.getDefault();
    private final int MAX_SMS_LENGTH = 160;


    @Override
    public void onCreate() {
        super.onCreate();

        // initialize BroadcastReceiver
        initActivityReceiver();

        // send directly the sos-sms
        // Test
        String phoneNo = "01752847846";
        phoneList.add(phoneNo);

        sendSms(phoneList, false);
    }


    private void sendSms(ArrayList<String> phoneList, boolean falseAlarm) {

        MessageBuilder messageBuilder = new MessageBuilder(this);

        if (falseAlarm) {
            for (String phoneNumber : phoneList) {
                sendSmsToContact(phoneNumber, messageBuilder.buildFalseAlarmMessage(phoneNumber));
                // TODO Name instead of Number
            }
        } else {
            for (String phoneNumber : phoneList) {
                String[] message = messageBuilder.buildSosMessage(phoneNumber);
                sendSmsToContact(phoneNumber, message[0]);
                sendSmsToContact(phoneNumber, message[1]);
                // TODO Name instead of Number
            }
        }
    }

    private void sendSmsToContact(String phoneNumber, String message) {
        if (message.length() <= 160) {
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } else {
            String part1 = message.substring(0, MAX_SMS_LENGTH);
            String part2 = message.substring(MAX_SMS_LENGTH);

            int indexOfBlank = 0;


            char[] part1AsCharArray = part1.toCharArray();
            for (int i = part1.length() - 1; i >= 0; i--) {
                Log.d(TAG, " " + part1AsCharArray[i] + i);
                if (part1AsCharArray[i] == ' ') {
                    indexOfBlank = i;
                    break;
                }
            }
            Log.d(TAG, "IndexOfBlank" + indexOfBlank);
            if (indexOfBlank > 0) {
                part2 = part1.substring(indexOfBlank) + part2;
                part1 = part1.substring(0, indexOfBlank);
            }

//            // Test
//            String part1split = part1.substring(MAX_SMS_LENGTH - 20);
//
//            if (part1split.contains(" ")){
//                String [] part1splitAsArray = part1split.split(" ");
//
//                part1 = part1.substring(0, MAX_SMS_LENGTH - part1splitAsArray[part1splitAsArray.length-1].length());
//                part2 = part1splitAsArray[part1splitAsArray.length -1] + part2;
//
//            }
//            //

            smsManager.sendTextMessage(phoneNumber, null, part1, null, null);
            sendSmsToContact(phoneNumber, part2);
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

                        sendSms(phoneList, true);
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
