package com.saveyourride.services;

import android.app.Activity;
import android.app.PendingIntent;
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
    private ArrayList<Boolean> smsSentSuccessfullyList;


    @Override
    public void onCreate() {
        super.onCreate();

        // initialize BroadcastReceiver
        initActivityReceiver();

        // list for sms
        smsSentSuccessfullyList = new ArrayList<>();

        // send directly the sos-sms
        // Test
        String phoneNo = "01752847846";
        phoneList.add(phoneNo);

        sendSms(phoneList, false);
    }


    private void sendSms(ArrayList<String> phoneList, boolean falseAlarm) {

        MessageBuilder messageBuilder = new MessageBuilder(this);

        if (falseAlarm) {
            smsSentSuccessfullyList = new ArrayList<>();
            for (String phoneNumber : phoneList) {
                sendSmsToContact(phoneNumber, messageBuilder.buildFalseAlarmMessage(phoneNumber));
                // TODO Name instead of Number
            }
            boolean successful = true;
            for (Boolean each : smsSentSuccessfullyList) {
                if (!each) {
                    // TODO sendBroadcast not successful
                    successful = false;
                    break;
                }
            }
            if (successful) {
                Log.d(TAG, "Alles hat geklappt");
            } else {
                Log.d(TAG, "Irgendwas ist schiefgelaufen");
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
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
//        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);

        if (message.length() <= 160) {
            smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, null);
        } else {
            String part1 = message.substring(0, MAX_SMS_LENGTH);
            String part2 = message.substring(MAX_SMS_LENGTH);

            int indexOfBlank = 0;


            char[] part1AsCharArray = part1.toCharArray();
            for (int i = part1.length() - 1; i >= 0; i--) {
                if (part1AsCharArray[i] == ' ') {
                    indexOfBlank = i;
                    break;
                }
            }

            if (indexOfBlank > 0) {
                part2 = part1.substring(indexOfBlank) + part2;
                part1 = part1.substring(0, indexOfBlank);
            }

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
                    case "SMS_SENT": {
                        boolean successful = false;
                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                successful = true;
                                break;
//                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//                                s = "Generic Failure Error";
//                                break;
//                            case SmsManager.RESULT_ERROR_NO_SERVICE:
//                                s = "Error : No Service Available";
//                                break;
//                            case SmsManager.RESULT_ERROR_NULL_PDU:
//                                s = "Error : Null PDU";
//                                break;
//                            case SmsManager.RESULT_ERROR_RADIO_OFF:
//                                s = "Error : Radio is off";
//                                break;
                            default:
                                successful = false;
                                break;
                        }
                        if (successful) {
                            smsSentSuccessfullyList.add(true);
                        } else {
                            smsSentSuccessfullyList.add(false);
                        }

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

        filter.addAction("SMS_SENT");
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
