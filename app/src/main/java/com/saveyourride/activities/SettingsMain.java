package com.saveyourride.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.saveyourride.R;
import com.saveyourride.utils.ListAdapterContacts;
import com.saveyourride.utils.ListAdapterMain;

public class SettingsMain extends AppCompatActivity {

    // Debug
    private final String TAG = "SettingsMain";

    // Array of strings...
    ListView settingsList;

    private final int SETTINGS_LIST_SIZE = 4;

    private final int SETTINGS_NAME = 0;
    private final int SETTINGS_MESSAGE= 1;
    private final int SETTINGS_SOS_CONTACTS = 2;
    private final int SETTINGS_ABOUT= 3;

    String settingsNameList []= new String[SETTINGS_LIST_SIZE];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_main);

        settingsNameList[SETTINGS_NAME] = "Name";
        settingsNameList[SETTINGS_MESSAGE] = "Message";
        settingsNameList[SETTINGS_SOS_CONTACTS] = "SOS-Contacts";
        settingsNameList[SETTINGS_ABOUT] = "About";

        settingsList = (ListView) findViewById(R.id.simpleListView);

        ListAdapterMain customAdapter = new ListAdapterMain(this, settingsNameList);
        settingsList.setAdapter(customAdapter);

        settingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case SETTINGS_NAME: {
                        Log.d(TAG, "SettingsName is clicked");
                        break;
                    }
                    case SETTINGS_MESSAGE: {
                        break;
                    }
                    case SETTINGS_SOS_CONTACTS: {
                        startActivity(new Intent(getApplicationContext(), SettingsContacts.class));
                        break;
                    }
                    case SETTINGS_ABOUT: {
                        Log.d(TAG, "SettingsAbout is clicked");
                        break;
                    }
                }
            }
        });
    }
}
