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
    private ArrayList<String> smsSentSuccessfullyList;
    private int numberOfContactPersons;

    // Timer wich start the SOS procedure
    private CountDownTimer sosModeStartTimer;
    private final long WAIT_TIME_IN_SECONDS = 10;
    private final long SECOND_IN_MILLISECONDS = 1000;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: ");

        // initialize BroadcastReceiver
        initActivityReceiver();

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

                sendSmsToAllContacts(contactList, false); // Send the sos-signal to all contacts

                // Set Ring Stream Volume to max for incoming Calls from Sos-Contacts
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_VIBRATE);

                smsManager.sendTextMessage("+491752847846", null, "Test", null, null);
            }
        }.start();

        // list for sms
        smsSentSuccessfullyList = new ArrayList<String>();

    }

    /**
     * Contact all contacts via sms
     *
     * @param contactList the list of contacts which will be contacted
     * @param falseAlarm  true if it is a false alarm
     */
    private void sendSmsToAllContacts(ArrayList<Contact> contactList, boolean falseAlarm) {

        MessageBuilder messageBuilder = new MessageBuilder(this);

        smsSentSuccessfullyList = new ArrayList<String>();
        numberOfContactPersons = 0;

        if (falseAlarm) {
            for (Contact contact : contactList) {
                sendSmsToSingleContact(contact.getPhoneNumber(), splitMessageToSmsFormat(messageBuilder.buildFalseAlarmMessage(contact.getFirstName() + " " + contact.getLastName())), falseAlarm);
            }

        } else {
            for (Contact contact : contactList) {
                String[] message = messageBuilder.buildSosMessage(contact.getFirstName() + " " + contact.getLastName());
                sendSmsToSingleContact(contact.getPhoneNumber(), splitMessageToSmsFormat(message[0] + message[1]), falseAlarm);
            }
        }
    }

    /**
     * send a list of sms to a phoneNumber
     *
     * @param phoneNumber to this number the sms will be send
     * @param smsList
     * @param falseAlarm  if it is true, it is a false alarm
     */
    private void sendSmsToSingleContact(String phoneNumber, ArrayList<String> smsList, boolean falseAlarm) {

        Intent normalSmsPart, lastPart;

        if (!falseAlarm) {
            normalSmsPart = new Intent("SMS_SENT");
            lastPart = new Intent("LAST_SMS_SENT");
        } else {
            normalSmsPart = new Intent("SMS_FALSE_ALARM_SENT");
            lastPart = new Intent("LAST_SMS_FALSE_ALARM_SENT");
        }

        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, normalSmsPart, 0);
        for (int i = 0; i < smsList.size(); i++) {
            if (i == smsList.size() - 1) {
                sentIntent = PendingIntent.getBroadcast(this, 0, lastPart, 0);
            }
            Log.d(TAG, "" + phoneNumber);
            smsManager.sendTextMessage(phoneNumber, null, smsList.get(i), sentIntent, null);
        }
    }

    /**
     * A sms is limited to 160 characters
     *
     * @param message text to split in 160 big parts
     * @return list of the parts from the message
     */
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

        }
        smsList.add(messageText);

        return smsList;
    }

    /**
     * Read the resultCode from a sent sms. If the sending was successful,
     * it will be add a true-value to the smsSentSuccessfullyList. If not, it will be a false-value.
     *
     * @param resultCode
     */
    private void readSmsStatus(int resultCode) {

        boolean successful;
        switch (resultCode) {
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
            default: {
                successful = false;
                break;
            }
        }

        if (successful) {
            smsSentSuccessfullyList.add("true");
        } else {
            smsSentSuccessfullyList.add("false");
        }

    }

    /**
     * Check if all contacts are contact correctly. If one object in the list is a false-value,
     * not all contacts are contact correctly.
     *
     * @return
     */
    private boolean checkIfContactAllContactsSuccessfully() {

        //Debug
        Log.d(TAG, "Check SMS");
        Log.d(TAG, "checkIfContactAllContactsSuccessfully size : " + smsSentSuccessfullyList.size());

        for (String sentSms : smsSentSuccessfullyList) {
            Log.d(TAG, "checkIfContactAllContactsSuccessfully: " + sentSms);
            if (sentSms == "false") {
                Log.d(TAG, "checkIfContactAllContactsSuccessfully:  smsSent == false");
                return false;
            }
        }
        Log.d(TAG, "Sending was successful!");
        return true;
    }

    /**
     * Read contacts from SharedPreferences
     *
     * @return ArrayList of Contacts
     */
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
                        sendSmsToAllContacts(contactList, true);
                        break;
                    }
                    case "SMS_SENT": {
                        readSmsStatus(getResultCode());
                        break;
                    }
                    case "LAST_SMS_SENT": {

                        readSmsStatus(getResultCode());

                        Log.d(TAG, "onReceive: " + smsSentSuccessfullyList.size());

                        numberOfContactPersons++;


                        if (numberOfContactPersons >= contactList.size()) {
                            if (checkIfContactAllContactsSuccessfully()) {
                                sendBroadcast(new Intent("android.intent.action.SMS_SENT_STATUS").putExtra("status", true));
                            } else {
                                sendBroadcast(new Intent("android.intent.action.SMS_SENT_STATUS").putExtra("status", false));
                            }
                        }

                        break;
                    }

                    case "SMS_FALSE_ALARM_SENT": {
                        readSmsStatus(getResultCode());
                        break;
                    }
                    case "LAST_SMS_FALSE_ALARM_SENT": {

                        readSmsStatus(getResultCode());

                        Log.d(TAG, "onReceive: " + smsSentSuccessfullyList.size());

                        numberOfContactPersons++;

                        if (numberOfContactPersons >= contactList.size()) {
                            if (checkIfContactAllContactsSuccessfully()) {
                                sendBroadcast(new Intent("android.intent.action.SMS_FALSE_ALARM_SENT_STATUS").putExtra("status", true));
                            } else {
                                sendBroadcast(new Intent("android.intent.action.SMS_FALSE_ALARM_SENT_STATUS").putExtra("status", false));
                            }
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
        filter.addAction("LAST_SMS_SENT");
        filter.addAction("SMS_FALSE_ALARM_SENT");
        filter.addAction("LAST_SMS_FALSE_ALARM_SENT");
        filter.addAction("android.intent.action.SEND_FALSE_ALARM_SMS");

        registerReceiver(receiver, filter);
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
