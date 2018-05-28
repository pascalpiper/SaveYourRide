package com.saveyourride.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.saveyourride.R;

import java.util.Set;

public class MessageBuilder {
    private Context context;
    private SharedPreferences sharedPreferences;

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
    private String defaultMessage;

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
            messageContent = sharedPreferences.getString("pref_custom_message", "default");
        } else {
            messageContent = context.getString(R.string.sos_message);
        }

        message = context.getString(R.string.greeting) + " "
                + contact + ", \n"
                + messageContent;

        return new String[]{message, getImportantInformations()};

    }

    private String getImportantInformations() {
        String message = "Important information: ";

        Set<String> included_information = sharedPreferences.getStringSet("pref_included_information", null);
        informationList = new String[6];

        for (String each : included_information) {
            saveInInformationList(each);
        }

        for (int i = 0; i < informationList.length; i++) {
            if (informationList[i] != null) {
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
        if (newInformation != null && informationID >= 0) {
//            informationList[informationID] = typeOfInformation + "- " + newInformation;
            informationList[informationID] = "\n->" + typeOfInformation + ":" + newInformation + " ";
        }
    }


    public void readPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);


        latitude = "5456.666"; // TODO Location
        longitude = "4156.24";

        accidentTime = "15:45pm"; // TODO Got Time

        name = sharedPreferences.getString("pref_name", "default_name");
        diseases = sharedPreferences.getString("pref_diseases", "default_name");
        allergies = sharedPreferences.getString("pref_allergies", "default_name");
        drugs = sharedPreferences.getString("pref_drugs", "default_name");

        informedContacts = "Pascal Piper, Kerstin Piper, Patrick Piper"; // TODO Liste von Kontakten

        customMessageEnabled = sharedPreferences.getBoolean("pref_enable_custom_message", false);

        defaultMessage = "die App SaveYourRide hat bemerkt, dass " + name + " wahrscheinlich einen Unfall hatte! ";
    }

}
