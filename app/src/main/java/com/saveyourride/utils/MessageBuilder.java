package com.saveyourride.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saveyourride.R;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

public class MessageBuilder {
    private Context context;
    private SharedPreferences sharedPreferencesSettings;

    // Informations

    private String latitude;
    private String longitude;
    private String accidentTime;
    private String name;
    private String diseases;
    private String allergies;
    private String drugs;
    private String informedContacts;
    private Boolean customMessageEnabled;

    private String[] informationList;

    private final int LOCATION = 0;
    private final int ACCIDENT_TIME = 1;
    private final int DISEASES = 2;
    private final int ALLERGIES = 3;
    private final int DRUGS = 4;
    private final int INFORMED_CONTACTS = 5;

    public MessageBuilder(Context context) {
        this.context = context;
        readPreferences();
    }


    public String buildFalseAlarmMessage(String contact) {
        String message = context.getString(R.string.greeting) + " "
                + contact + ", \n"
                + context.getString(R.string.false_alarm_message);
        return message;
    }

    public String[] buildSosMessage(String contact) {

        String message;
        String messageContent;

        if (customMessageEnabled) {
            messageContent = sharedPreferencesSettings.getString("pref_custom_message", "default");
        } else {
            messageContent = String.format(context.getString(R.string.sos_message), name);
        }

        message = String.format(context.getString(R.string.greeting), contact)
                + "\n"
                + messageContent
                + "\n "
        ;

        return new String[]{message, getImportantInformations()};

    }

    private String getImportantInformations() {
        String message = "Important information: ";

        Set<String> included_information = sharedPreferencesSettings.getStringSet("pref_included_information", null);
        informationList = new String[6];

        for (String each : included_information) {
            saveInInformationList(each);
        }

        for (int i = 0; i < informationList.length; i++) {
            if (informationList[i] != null && !informationList[i].isEmpty()) {
                message = message + informationList[i];
            }
        }
        return message;
    }

    public void saveInInformationList(String typeOfInformation) {
        String newInformation = null;
        int informationID = -1;

        switch (typeOfInformation) {
            case "GPS-Location": {
                informationID = LOCATION;
                newInformation = longitude + " " + latitude;
                break;
            }
            case "Time of Accident": {
                informationID = ACCIDENT_TIME;
                newInformation = accidentTime;
                break;
            }
            case "Diseases": {
                informationID = DISEASES;
                newInformation = diseases;
                break;
            }
            case "Allergies": {
                informationID = ALLERGIES;
                newInformation = allergies;
                break;
            }
            case "Drugs": {
                informationID = DRUGS;
                newInformation = drugs;
                break;
            }
            case "Informed contacts": {
                informationID = INFORMED_CONTACTS;
                newInformation = informedContacts;
                break;
            }

            default:
                newInformation = null;
        }
        if (newInformation != null && !newInformation.isEmpty() && informationID >= 0) {
//            informationList[informationID] = typeOfInformation + "- " + newInformation;
            informationList[informationID] = "\n->" + typeOfInformation + ":" + newInformation + " ";
        }
    }


    public void readPreferences() {
        sharedPreferencesSettings = PreferenceManager.getDefaultSharedPreferences(context);


        latitude = "5456.666"; // TODO Location
        longitude = "4156.24";

        SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm");
        accidentTime = mdformat.format(Calendar.getInstance().getTime());

        name = sharedPreferencesSettings.getString("pref_name", "");
        diseases = sharedPreferencesSettings.getString("pref_diseases", "");
        allergies = sharedPreferencesSettings.getString("pref_allergies", "");
        drugs = sharedPreferencesSettings.getString("pref_drugs", "");

        for (Contact contact : readContacts()) {

            String currentContact = contact.getFirstName() + " " +
                    contact.getLastName() + " " +
                    contact.getPhoneNumber() + "; ";

            if (informedContacts == null) {
                informedContacts = currentContact;
            } else {
                informedContacts = informedContacts + currentContact;
            }
        }

        customMessageEnabled = sharedPreferencesSettings.getBoolean("pref_enable_custom_message", false);

    }

    private ArrayList<Contact> readContacts() {
        // Set SharedPreferences
        SharedPreferences savedContacts = context.getSharedPreferences(context.getString(R.string.sp_key_saved_contacts), Context.MODE_PRIVATE);

        // Set contactList
        String contactsJSON = savedContacts.getString(context.getString(R.string.sp_key_contacts_json), context.getString(R.string.default_contacts_json));
        Type type = new TypeToken<ArrayList<Contact>>() {
        }.getType();
        ArrayList<Contact> contactList = new Gson().fromJson(contactsJSON, type);
        return contactList;
    }

}
