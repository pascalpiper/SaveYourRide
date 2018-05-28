package com.saveyourride.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saveyourride.R;
import com.saveyourride.utils.Contact;
import com.saveyourride.utils.MessageBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SettingsPreviewMessage extends AppCompatActivity {

    private final String TAG = "PreviewMessage";

    //MessageBuilder
    MessageBuilder messageBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_preview_message);
        messageBuilder = new MessageBuilder(this);

        String firstContact;
        if (!readContacts().isEmpty()) {
            firstContact = readContacts().get(0).getFirstName() + " " + readContacts().get(0).getLastName();
        } else {
            firstContact = getString(R.string.test_contact);
        }


        String[] message = messageBuilder.buildSosMessage(firstContact);

        TextView textViewPreviewMessage_part1 = findViewById(R.id.preview_message);
        textViewPreviewMessage_part1.setText(message[0] + message[1]);
    }

    private ArrayList<Contact> readContacts() {
        // Set SharedPreferences
        SharedPreferences savedContacts = getSharedPreferences(getString(R.string.sp_key_saved_contacts), Context.MODE_PRIVATE);

        // Set contactList
        String contactsJSON = savedContacts.getString(getString(R.string.sp_key_contacts_json), getString(R.string.default_contacts_json));
        Type type = new TypeToken<ArrayList<Contact>>() {
        }.getType();
        ArrayList contactList = new Gson().fromJson(contactsJSON, type);
        return contactList;
    }
}
