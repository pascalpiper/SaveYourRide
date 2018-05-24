package com.saveyourride.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.saveyourride.R;

import java.util.Set;

public class SettingsPreviewMessage extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private final String TAG = "PreviewMessage";

    // Informations

    private String latitude;
    private String longitude;
    private String accidentTime;
    private String firstContact;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_preview_message);

        gotInformation();

        informationList = new String[6];

        TextView textViewPreviewMessage = findViewById(R.id.preview_message);
        textViewPreviewMessage.setText(createMessage());


    }

    private String createMessage() {

        String messageContent;
        if (customMessageEnabled) {
            messageContent = sharedPreferences.getString("pref_custom_message", "default");
        } else {
            messageContent = defaultMessage;
        }

        String message = "Hallo " + firstContact + ",\n" + messageContent +
                "\n -------- \nImportant information:";


        Set<String> included_information = sharedPreferences.getStringSet("pref_included_information", null);

        for (String each : included_information) {
            Log.d(TAG, each);
            saveInInformationList(each);
        }

        message = appendInformationToMessage(message);

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
            informationList[informationID] = "\n" + typeOfInformation + ": " + newInformation;
        }
    }

    private String appendInformationToMessage(String message) {
        int i;
        for (i = 0; i < informationList.length; i++) {
            if (informationList[i] != null) {
                message = message + informationList[i];
            }
        }
        return message;
    }

    public void gotInformation() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        latitude = "5456.666"; // TODO Location
        longitude = "4156.24";

        accidentTime = "15:45pm"; // TODO Got Time

        firstContact = "Taras Zaika"; // TODO Erster Konakt

        name = sharedPreferences.getString("pref_name", "default_name");
        diseases = sharedPreferences.getString("pref_diseases", "default_name");
        allergies = sharedPreferences.getString("pref_allergies", "default_name");
        drugs = sharedPreferences.getString("pref_drugs", "default_name");

        informedContacts = "Pascal Piper, Kerstin Piper, Patrick Piper"; // TODO Liste von Kontakten

        customMessageEnabled = sharedPreferences.getBoolean("pref_enable_custom_message", false);

        defaultMessage = "die App SaveYourRide hat bemerkt, dass " + name + " wahrscheinlich einen Unfall hatte! ";
    }

}
