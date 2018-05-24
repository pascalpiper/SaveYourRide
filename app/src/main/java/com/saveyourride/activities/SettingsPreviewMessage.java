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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_preview_message);

        gotInformations();

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
            message = appendToMessage(each, message);
        }

        return message;
    }

    public String appendToMessage(String typeOfInformation, String message) {
        String newInformation = null;

        switch (typeOfInformation) {
            case "GPS-Location": {
                newInformation = longitude + " " + latitude;
                break;
            }
            case "Time of Accident": {
                newInformation = accidentTime;
                break;
            }
            case "Diseases": {
                newInformation = diseases;
                break;
            }
            case "Allergies": {
                newInformation = allergies;
                break;
            }
            case "Drugs": {
                newInformation = drugs;
                break;
            }
            case "Informed contacts": {
                newInformation = informedContacts;
                break;
            }

            default:
                newInformation = null;
        }
        if (newInformation == null) {
            return message;
        } else {
            return message + "\n" + typeOfInformation + ": " + newInformation;
        }
    }

    public void gotInformations() {
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
