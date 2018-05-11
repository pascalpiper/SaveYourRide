package com.saveyourride.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.saveyourride.R;
import com.saveyourride.activities.ActiveMode;

public class Active extends Fragment {

    // DEBUG
    private final String TAG = "ActiveFragment";
    //

    // Permission request code
    private final int SEND_SMS_PERMISSIONS_REQUEST_CODE = 1;

    // DialogIDs
    private final int SEND_SMS_PERMISSION_EXPLANATION_DIALOG = 0;
    private final int SEND_SMS_PERMISSION_DENIED_DIALOG = 1;

    // Buttons
    private Button buttonStartActiveMode;

    // Because of Fragment we need an activity object.
    private FragmentActivity myActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_active, container, false);

        myActivity = getActivity();

        buttonStartActiveMode = (Button) fragmentView.findViewById(R.id.buttonStartActiveMode);

        buttonStartActiveMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSmsPermission()) {
                    startActivity(new Intent(getActivity(), ActiveMode.class));
                }
            }
        });

        return fragmentView;
    }

    /**
     * Checks if SEND_SMS permission is granted.
     *
     * @return boolean value. If SEND_SMS permission is granted: true else false.
     */
    private boolean checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(myActivity, Manifest.permission.SEND_SMS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showAlertDialog(SEND_SMS_PERMISSION_EXPLANATION_DIALOG);
            } else {
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSIONS_REQUEST_CODE);

            }
            return false;
        } else {
            // Permission has already been granted
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SEND_SMS_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buttonStartActiveMode.callOnClick();
                } else {
                    showAlertDialog(SEND_SMS_PERMISSION_DENIED_DIALOG);
                }
                break;
            }
            default: {
                Log.d(TAG, "NO SUCH REQUEST CODE!");
                break;
            }
        }
    }

    /**
     * Show a dialog with the information for a specific notification.
     *
     * @param dialogID determines the information to be shown.
     */
    private void showAlertDialog(int dialogID) {
        switch (dialogID) {
            case SEND_SMS_PERMISSION_EXPLANATION_DIALOG: {
                AlertDialog.Builder alert = new AlertDialog.Builder(myActivity);
                // Set dialog title
                alert.setTitle(R.string.title_dialog_send_sms_permission);
                // Set dialog message
                alert.setMessage(R.string.dialog_send_sms_permission_explanation);
                // Set up the button
                alert.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSIONS_REQUEST_CODE);
                    }
                });
                AlertDialog currentDialog = alert.create();
                currentDialog.show();
                break;
            }
            case SEND_SMS_PERMISSION_DENIED_DIALOG: {
                AlertDialog.Builder alert = new AlertDialog.Builder(myActivity);
                // Set dialog title
                alert.setTitle(R.string.title_dialog_send_sms_permission);
                // Set dialog message
                alert.setMessage(R.string.dialog_send_sms_permission_required);
                // Set up the buttons
                alert.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Go to app settings
                        Intent appSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", myActivity.getPackageName(), null);
                        appSettings.setData(uri);
                        startActivity(appSettings);
                    }
                });
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog currentDialog = alert.create();
                currentDialog.show();
                break;
            }
            default: {
                Log.d(TAG, "NO SUCH DIALOG!");
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}