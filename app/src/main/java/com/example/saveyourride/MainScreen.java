package com.example.saveyourride;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        try {
            getSupportActionBar().setTitle(R.string.app_name);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        //Listener für "click" auf Banner1

        Button deleteEventButton = (Button) findViewById(R.id.deleteEvent);

        deleteEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteEvent();
            }
        });

    }

    private void deleteEvent() {

        ParseServer ps = ParseServer.getInstance(this);
        ps.deleteEventData("nvUP4HrcGG");
        Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();

    }
}
