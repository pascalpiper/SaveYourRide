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

        sosModeStartTimer = new CountDownTimer(WAIT_TIME_IN_SECONDS * SECOND_IN_MILLISECONDS, SECOND_IN_MILLISECONDS) {
            int restTime = (int) WAIT_TIME_IN_SECONDS;
            ;

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

        if (falseAlarm) {
            smsSentSuccessfullyList = new ArrayList<>();
            for (Contact contact : contactList) {
                sendSmsToContact(contact.getPhoneNumber(), messageBuilder.buildFalseAlarmMessage(contact.getFirstName() + " " + contact.getLastName()));
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
            for (Contact contact : contactList) {
                String[] message = messageBuilder.buildSosMessage(contact.getFirstName() + " " + contact.getLastName());
                sendSmsToContact(contact.getPhoneNumber(), message[0]);
                sendSmsToContact(contact.getPhoneNumber(), message[1]);
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

                        sendSms(contactList, true);
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
