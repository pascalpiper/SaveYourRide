package com.saveyourride.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.saveyourride.R;
import com.saveyourride.utils.ListAdapterContacts;

import java.util.Objects;


public class SettingsContacts extends AppCompatActivity {

    // Array of strings...
    ListView simpleList;
    String nameList[] = {"Pascal", "Taras", "Patrick", "Vater", "Mutter", "Ricarda"};
    String numberList[] = {"1234", "5678", "1234", "5678", "1234", "5678"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_contacts);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        simpleList = (ListView) findViewById(R.id.simpleListView);

        ListAdapterContacts customAdapter = new ListAdapterContacts(this, nameList, numberList);
        simpleList.setAdapter(customAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //menu activity bekannt
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contacts_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //click listener quasi.
        switch (item.getItemId()) {
            case R.id.addContactBtn:
                // settingsIntent.putExtra(Intent.EXTRA_TEXT, aktienInfo);
                Intent activeIntent = new Intent(getApplicationContext(), MainScreen.class);
                startActivityIfNeeded(activeIntent, 0);
                startActivity(activeIntent);
                break;
        }
        return super.onOptionsItemSelected(item); //To change body of generated methods, choose Tools | Templates.
    }
}


