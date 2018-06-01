package com.saveyourride.services;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saveyourride.R;
import com.saveyourride.utils.Contact;
import com.saveyourride.utils.MessageBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SosModeManager extends Service {

    //TODO Location for SMS

    // DEBUG
    private final String TAG = "SosModeManager";
    //

    // BroadcastReceiver for messages from ActiveMode Activity
    private BroadcastReceiver receiver;

    // Shared Preferences
    SharedPreferences savedContacts;

    ArrayList<Contact> contactList;

    // SMS-Manager
    private SmsManager smsManager = SmsManager.getDefault();
    private final int MAX_SMS_LENGTH = 160;
    private ArrayList<Boolean> smsSentSuccessfullyList;
    private int numberOfContactPersons;

    // Timer witch start the SOS procedure
    private CountDownTimer sosModeStartTimer;
    private final long WAIT_TIME_IN_SECONDS = 10;
    private final long SECOND_IN_MILLISECONDS = 1000;


    @Override
    public void onCreate() {
        super.onCreate();

        // initialize BroadcastReceiver
        initActivityReceiver();

        Log.d(TAG, "onCreate: ");

        sosModeStartTimer = new CountDownTimer(WAIT_TIME_IN_SECONDS * SECOND_IN_MILLISECONDS, SECOND_IN_MILLISECONDS) {
            int restTime = (int) WAIT_TIME_IN_SECONDS;

            @Override
            public void onTick(long millisUntilFinished) {
                sendBroadcast(new Intent("android.intent.action.SECOND_IS_OVER").putExtra("restTime", restTime));
                restTime--;
            }

            @Override
            public void onFinish() {

                sendBroadcast(new Intent("android.intent.action.SOS_PROCEDURE_IS_RUNNING"));
                contactList = readContacts();

                sendSms(contactList, false);

                // Set Ring Stream Volume to max for incoming Calls from Sos-Contacts
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_VIBRATE);
            }
        }.start();

        // list for sms
        smsSentSuccessfullyList = new ArrayList<Boolean>();
    }

    private void sendSms(ArrayList<Contact> contactList, boolean falseAlarm) {

        MessageBuilder messageBuilder = new MessageBuilder(this);

        smsSentSuccessfullyList = new ArrayList<>();
        numberOfContactPersons = 0;

        if (falseAlarm) {
            for (Contact contact : contactList) {
                sendSmsToContact(contact.getPhoneNumber(), splitMessageToSmsFormat(messageBuilder.buildFalseAlarmMessage(contact.getFirstName() + " " + contact.getLastName())), falseAlarm);
            }

        } else {
            for (Contact contact : contactList) {
                String[] message = messageBuilder.buildSosMessage(contact.getFirstName() + " " + contact.getLastName());
                sendSmsToContact(contact.getPhoneNumber(), splitMessageToSmsFormat(message[0] + message[1]), falseAlarm);
            }
        }
        checkIfSendSmsSuccessful();
    }

    private void sendSmsToContact(String phoneNumber, ArrayList<String> smsList, boolean falseAlarm) {

        Intent lastPart = new Intent("LAST_PART");
        Intent normalSmsPart = new Intent("SMS_SENT");

        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, normalSmsPart, 0);


        for (int i = 0; i < smsList.size(); i++) {
            if (i == smsList.size() - 1) {
                sentIntent = PendingIntent.getBroadcast(this, 0, lastPart, 0);
            } else
                smsManager.sendTextMessage(phoneNumber, null, smsList.get(i), sentIntent, null);
        }
    }

    private ArrayList<String> splitMessageToSmsFormat(String message) {
        ArrayList<String> smsList = new ArrayList<String>();

        String messageText = message;

        while (messageText.length() > MAX_SMS_LENGTH) {

            String part1 = messageText.substring(0, MAX_SMS_LENGTH);
            String part2 = messageText.substring(MAX_SMS_LENGTH);

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

            smsList.add(part1);
            messageText = part2;
//
//           Log.d(TAG, "splitMessageToSmsFormat: " + part1);
//           Log.d(TAG, "splitMessageToSmsFormat: " + part2);
        }
        smsList.add(messageText);

        return smsList;
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

                        sendSms(contactList, true);
                        break;
                    }
                    case "SMS_SENT": {
                        boolean successful;
                        switch (getResultCode()) {
                            case Activity.RESULT_OK: {
                                successful = true;
                                break;
                            }
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE: {
                                successful = false;
                                break;
                            }
                            case SmsManager.RESULT_ERROR_NO_SERVICE: {
                                successful = false;
                                break;
                            }
                            case SmsManager.RESULT_ERROR_NULL_PDU: {
                                successful = false;
                                break;
                            }
                            case SmsManager.RESULT_ERROR_RADIO_OFF: {
                                successful = false;
                                break;
                            }
                            default:
                                successful = true;
                                break;

                        }
                        smsSentSuccessfullyList.add(false);
                        if (successful) {
                            smsSentSuccessfullyList.add(true);
                        } else {
                            smsSentSuccessfullyList.add(false);
                        }
                        break;
                    }
                    case "LAST_PART": {
                        boolean successful;
                        switch (getResultCode()) {
                            case Activity.RESULT_OK: {
                                successful = true;
                                break;
                            }
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE: {
                                successful = false;
                                break;
                            }
                            case SmsManager.RESULT_ERROR_NO_SERVICE: {
                                successful = false;
                                break;
                            }
                            case SmsManager.RESULT_ERROR_NULL_PDU: {
                                successful = false;
                                break;
                            }
                            case SmsManager.RESULT_ERROR_RADIO_OFF: {
                                successful = false;
                                break;
                            }
                            default:
                                successful = true;
                                break;
                        }
                        if (successful) {
                            smsSentSuccessfullyList.add(true);
                        } else {
                            smsSentSuccessfullyList.add(false);
                        }
                        Log.d(TAG, "onReceive: " + smsSentSuccessfullyList.size());

                        numberOfContactPersons++;

                        if (numberOfContactPersons >= contactList.size()) {
                            checkIfSendSmsSuccessful();
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
        filter.addAction("LAST_PART");
        filter.addAction("android.intent.action.SEND_FALSE_ALARM_SMS");

        registerReceiver(receiver, filter);
    }

    private ArrayList<Contact> readContacts() {
        // Set SharedPreferences
        savedContacts = getSharedPreferences(getString(R.string.sp_key_saved_contacts), Context.MODE_PRIVATE);

        // Set contactList
        String contactsJSON = savedContacts.getString(getString(R.string.sp_key_contacts_json), getString(R.string.default_contacts_json));
        Type type = new TypeToken<ArrayList<Contact>>() {
        }.getType();
        contactList = new Gson().fromJson(contactsJSON, type);
        return contactList;
    }

    private void checkIfSendSmsSuccessful() {
        Log.d(TAG, "Check SMS");
        boolean successful = true;
        Log.d(TAG, "checkIfSendSmsSuccessful size : " + smsSentSuccessfullyList.size());
        for (boolean sentSms : smsSentSuccessfullyList) {
            Log.d(TAG, "checkIfSendSmsSuccessful: " + sentSms);
            if (!sentSms) {
                Log.d(TAG, "checkIfSendSmsSuccessful:  smsSent == false");
                sendBroadcast(new Intent("android.intent.action.SMS_SENT_STATUS").putExtra("status", false));
                successful = false;
            }
        }
        if (successful) {
            Log.d(TAG, "Sending was successful!");
            sendBroadcast(new Intent("android.intent.action.SMS_SENT_STATUS").putExtra("status", true));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);

        if (sosModeStartTimer != null) {
            sosModeStartTimer.cancel();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
