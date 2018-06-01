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


    // TestPhoneNumber
    ArrayList<String> phoneList = new ArrayList<String>();

    // SMS-Manager
    private SmsManager smsManager = SmsManager.getDefault();
    private final int MAX_SMS_LENGTH = 160;
    private ArrayList<Boolean> smsSentSuccessfullyList;

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
        smsSentSuccessfullyList = new ArrayList<>();
    }

    private void sendSms(ArrayList<Contact> contactList, boolean falseAlarm) {

        MessageBuilder messageBuilder = new MessageBuilder(this);

        smsSentSuccessfullyList = new ArrayList<>();

        if (falseAlarm) {
            for (Contact contact : contactList) {
                sendSmsToContact(contact.getPhoneNumber(), messageBuilder.buildFalseAlarmMessage(contact.getFirstName() + " " + contact.getLastName()), falseAlarm);
            }

        } else {
            for (Contact contact : contactList) {
                String[] message = messageBuilder.buildSosMessage(contact.getFirstName() + " " + contact.getLastName());
                sendSmsToContact(contact.getPhoneNumber(), message[0] + message[1], falseAlarm);
//                sendSmsToContact(contact.getPhoneNumber(), message[1], falseAlarm);
            }
        }
    }

    private void sendSmsToContact(String phoneNumber, String message, boolean falseAlarm) {

        Intent sosIntent = new Intent("SMS_SENT_SOS");
        Intent falseAlarmIntent = new Intent("SMS_SENT_FALSE_ALARM");

        PendingIntent sentIntent;

        if (falseAlarm) {
            sentIntent = PendingIntent.getBroadcast(this, 0, falseAlarmIntent, 0);
        } else {
            sentIntent = PendingIntent.getBroadcast(this, 0, sosIntent, 0);
        }


        ArrayList<String> smsList = splitMessageToSmsFormat(message);
        for (String sms : smsList) {
            Log.d(TAG, "sendSmsToContact: " + sms);
            smsManager.sendTextMessage(phoneNumber, null, sms, sentIntent, null);
        }
    }

    public ArrayList<String> splitMessageToSmsFormat(String message) {
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
                    case "SMS_SENT_FALSE_ALARM": {
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
//                        Log.d(TAG, "" + intent.getBooleanExtra("lastPart", false));
//                        Log.d(TAG, "" + intent.getBooleanExtra("falseAlarm", true));
                        Log.d(TAG, "FalseAlarm");

                        break;
                    }
                    case "SMS_SENT_SOS": {
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
                    case "LAST_PART": {
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
                        checkIfSendSmsSuccessful();
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

        filter.addAction("SMS_SENT_SOS");
        filter.addAction("SMS_SENT_FALSE_ALARM");
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
        for (boolean sentSms : smsSentSuccessfullyList) {
            if (!sentSms) {
                sendBroadcast(new Intent("android.intent.action.SMS_SENT_STATUS").putExtra("status", false));
                successful = false;
                break;
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
