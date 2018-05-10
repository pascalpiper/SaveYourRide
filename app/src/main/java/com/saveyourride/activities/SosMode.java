package com.saveyourride.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.saveyourride.R;
import com.saveyourride.services.SosModeManager;

public class SosMode extends AppCompatActivity {

    private final String TAG = "SosMode";
    private static final int REQUEST_CODE_SEND_SMS = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_mode);

        Button buttonSos = (Button)findViewById(R.id.buttonSosInSosMode);
        Button buttonFalseAlarm = (Button)findViewById(R.id.buttonFalseAlarm);

        startService(new Intent(this,  SosModeManager.class));

        buttonSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Sos");
            }
        });

        buttonFalseAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                stopService(new Intent(getApplicationContext(), SosModeManager.class));
//                startActivity(new Intent(getApplicationContext(), MainScreen.class));
//                finish();
                sendSMSMessage();
            }
        });



    }



    protected void sendSMSMessage() {


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        REQUEST_CODE_SEND_SMS);
            }
        }
        else {
            sendBroadcast(new Intent("android.intent.action.SEND_SMS"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        Log.d(TAG, " "+ requestCode);
        switch (requestCode) {
            case REQUEST_CODE_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    sendBroadcast(new Intent("android.intent.action.SEND_SMS"));

                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }
}

