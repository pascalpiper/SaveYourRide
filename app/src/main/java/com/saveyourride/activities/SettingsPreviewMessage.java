package com.saveyourride.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.saveyourride.R;
import com.saveyourride.utils.MessageBuilder;

public class SettingsPreviewMessage extends AppCompatActivity {

    private final String TAG = "PreviewMessage";

    //MessageBuilder
    MessageBuilder messageBuilder;
    String firstContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_preview_message);
        messageBuilder = new MessageBuilder(this);
        firstContact = getString(R.string.test_contact);

        String[] message = messageBuilder.buildSosMessage(firstContact);

        TextView textViewPreviewMessage_part1 = findViewById(R.id.preview_message);
        textViewPreviewMessage_part1.setText(message[0] + message[1]);
        // TODO 2. Textview
//        TextView textViewPreviewMessage_part2 = findViewById(R.id.preview_message_part2);
//        textViewPreviewMessage_part2.setText(message[1]);
    }
}
