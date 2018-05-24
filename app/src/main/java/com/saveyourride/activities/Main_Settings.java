package com.saveyourride.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.saveyourride.R;
import com.saveyourride.utils.Contact;
import com.saveyourride.utils.ContactsListAdapter;

import java.util.Objects;


public class Main_Settings extends AppCompatActivity {

    // Array of strings...
    ListView contactListView;
    Contact[] contactsList = {
            new Contact("Pascal", "Piper", "+491752847846"),
            new Contact("Taras", "Zaika", "+4915738196717"),
            new Contact("Patrick", "Piper", "+4915000000000"),
            new Contact("Philipp", "Hoedt", "+4915228791403"),
            new Contact("Paulo", "Pinto", "+491633629003")
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_settings);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        contactListView = (ListView) findViewById(R.id.contactList_contactListView);

        ContactsListAdapter listAdapter = new ContactsListAdapter(this, contactsList);
        contactListView.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contacts_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //click listener quasi.
        switch (item.getItemId()) {
            case R.id.addContactBtn:
                Intent activeIntent = new Intent(getApplicationContext(), MainScreen.class);
                startActivityIfNeeded(activeIntent, 0);
                startActivity(activeIntent);
                break;
        }
        return super.onOptionsItemSelected(item); //To change body of generated methods, choose Tools | Templates.
    }
}


