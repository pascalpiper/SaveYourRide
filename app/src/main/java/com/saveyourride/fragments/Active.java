package com.saveyourride.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.NumberPicker;
import android.widget.TextView;

import com.saveyourride.R;
import com.saveyourride.activities.ActiveMode;
import com.saveyourride.utils.PickerEditor;

import java.util.concurrent.TimeUnit;

public class Active extends Fragment {

    // DEBUG
    private final String TAG = "ActiveFragment";
    //

    // Permission request code
    private final int SEND_SMS_PERMISSIONS_REQUEST_CODE = 1;

    // DialogIDs
    private final int SEND_SMS_PERMISSION_EXPLANATION_DIALOG = 0;
    private final int SEND_SMS_PERMISSION_DENIED_DIALOG = 1;
    private final int NUMBER_OF_INTERVALS_DIALOG = 2;
    private final int TIME_OF_INTERVAL_DIALOG = 3;

    // Buttons
    private Button buttonStartActiveMode;

    // TextViews
    private TextView textViewNumberOfIntervals, textViewTimeOfInterval;

    // Because of Fragment we need an activity object.
    private FragmentActivity myActivity;

    // SharedPreferences for timer values
    private SharedPreferences timerValues;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_active, container, false);

        myActivity = getActivity();

        // Set SharedPreferences
        timerValues = myActivity.getSharedPreferences(getString(R.string.sp_key_timer_values), Context.MODE_PRIVATE);

        // TextViews
        textViewNumberOfIntervals = (TextView) fragmentView.findViewById(R.id.activeFragment_textViewNumberOfIntervals);
        textViewTimeOfInterval = (TextView) fragmentView.findViewById(R.id.activeFragment_textViewTimeOfInterval);

        // Set TextViews
        setNumberOfIntervalsText();
        setTimeOfIntervalText();

        // Button
        buttonStartActiveMode = (Button) fragmentView.findViewById(R.id.activeFragment_buttonStart);

        // Layouts (as Views)
        View layoutNumberOfIntervals = (View) fragmentView.findViewById(R.id.activeFragment_layoutNumberOfIntervals);
        View layoutTimeOfInterval = (View) fragmentView.findViewById(R.id.activeFragment_layoutTimeOfInterval);

        // OnClickListeners
        buttonStartActiveMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSmsPermission()) {
                    startActivity(new Intent(getActivity(), ActiveMode.class));
                }
            }
        });
        layoutNumberOfIntervals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(NUMBER_OF_INTERVALS_DIALOG);
            }
        });
        layoutTimeOfInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(TIME_OF_INTERVAL_DIALOG);
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
                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
                alert.setMessage(R.string.dialog_send_sms_permission_denied);
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
            case NUMBER_OF_INTERVALS_DIALOG: {
                final NumberPicker numberOfIntervalsPicker = new NumberPicker(myActivity);
                PickerEditor.setNumberPickerTextColor(numberOfIntervalsPicker, myActivity.getColor(android.R.color.black));
                numberOfIntervalsPicker.setMinValue(getResources().getInteger(R.integer.min_number_of_intervals));
                numberOfIntervalsPicker.setMaxValue(getResources().getInteger(R.integer.max_number_of_intervals));
                numberOfIntervalsPicker.setValue(timerValues.getInt(getString(R.string.sp_key_number_of_interval), getResources().getInteger(R.integer.default_number_of_intervals)));

                //Dialog
                AlertDialog.Builder alert = new AlertDialog.Builder(myActivity);
                alert.setView(numberOfIntervalsPicker);
                alert.setTitle(R.string.number_of_intervals);

                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Save value to SharedPreferences
                        SharedPreferences.Editor editor = timerValues.edit();
                        editor.putInt(getString(R.string.sp_key_number_of_interval), numberOfIntervalsPicker.getValue());
                        editor.apply();
                        // Change textView
                        setNumberOfIntervalsText();
                    }
                });

                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = alert.create();
                dialog.show();
                break;
            }
            case TIME_OF_INTERVAL_DIALOG: {
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_time_of_interval_picker, null);

                final long timeOfIntervalMillis = timerValues.getLong(getString(R.string.sp_key_time_of_interval), getResources().getInteger(R.integer.default_time_of_interval));

                final NumberPicker minutesPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_minutesPicker);
                final NumberPicker secondsPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_secondsPicker);
                PickerEditor.setNumberPickerTextColor(minutesPicker, myActivity.getColor(android.R.color.black));
                PickerEditor.setDividerColor(minutesPicker, myActivity.getColor(android.R.color.black));
                PickerEditor.setNumberPickerTextColor(secondsPicker, myActivity.getColor(android.R.color.black));
                PickerEditor.setDividerColor(secondsPicker, myActivity.getColor(android.R.color.black));

                // Set values for minutesPicker (values from 01..10)
                final int minMinutes = getResources().getInteger(R.integer.min_interval_minutes);
                final int maxMinutes = getResources().getInteger(R.integer.max_interval_minutes);
                final String[] minutes = new String[maxMinutes - minMinutes + 1];
                for (int i = 0; i < minutes.length; i++) {
                    minutes[i] = String.format("%02d", (i + minMinutes));
                }
                minutesPicker.setDisplayedValues(minutes);
                minutesPicker.setMinValue(minMinutes);
                minutesPicker.setMaxValue(maxMinutes);
                minutesPicker.setValue((int) TimeUnit.MILLISECONDS.toMinutes(timeOfIntervalMillis));

                // Set values for secondsPicker (values from 00..59)
                final int minSeconds = getResources().getInteger(R.integer.min_interval_seconds);
                final int maxSeconds = getResources().getInteger(R.integer.max_interval_seconds);
                final String[] seconds = new String[maxSeconds - minSeconds + 1];
                for (int i = 0; i < seconds.length; i++) {
                    seconds[i] = String.format("%02d", (i + minSeconds));
                }
                secondsPicker.setDisplayedValues(seconds);
                secondsPicker.setMinValue(minSeconds);
                secondsPicker.setMaxValue(maxSeconds);

                /* To set seconds we need some calculation.
                 * For example: we have 80000 millis in {@code timeOfIntervalMillis}.
                 * It is equal to 80 seconds or 1 minute and 20 seconds.
                 * It means that the value must be set to 20 seconds.
                 * We have to subtract 60 seconds (1 minute) from 80 seconds.
                 */
                secondsPicker.setValue((int) (
                        TimeUnit.MILLISECONDS.toSeconds(timeOfIntervalMillis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeOfIntervalMillis))));

                AlertDialog.Builder alert = new AlertDialog.Builder(myActivity);
                // Set the view from XML inside AlertDialog
                alert.setView(dialogView);
                alert.setTitle(R.string.time_of_interval);

                // Set up the buttons
                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final long newTimeOfIntervalMillis = (TimeUnit.MINUTES.toMillis(minutesPicker.getValue()) + TimeUnit.SECONDS.toMillis(secondsPicker.getValue()));
                        // Save value to SharedPreferences
                        SharedPreferences.Editor editor = timerValues.edit();
                        editor.putLong(getString(R.string.sp_key_time_of_interval), newTimeOfIntervalMillis);
                        editor.apply();
                        // Change textView
                        setTimeOfIntervalText();
                    }
                });
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = alert.create();
                dialog.show();
                break;
            }
            default: {
                Log.d(TAG, "NO SUCH DIALOG!");
                break;
            }
        }
    }

    /**
     * Set text in the {@code textViewNumberOfIntervals}.
     * Text is the value from {@code SharedPreferences} {@code timerValues}.
     */
    private void setNumberOfIntervalsText() {
        textViewNumberOfIntervals.setText(Integer.toString(timerValues.getInt(getString(R.string.sp_key_number_of_interval), getResources().getInteger(R.integer.default_number_of_intervals))));
    }

    /**
     * Set text in the {@code textViewTimeOfInterval}.
     * Text is the value from {@code SharedPreferences} {@code timerValues}.
     */
    private void setTimeOfIntervalText() {
        long timeOfIntervalMillis = timerValues.getLong(getString(R.string.sp_key_time_of_interval), getResources().getInteger(R.integer.default_time_of_interval));
        String timeOfIntervalString = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeOfIntervalMillis),
                TimeUnit.MILLISECONDS.toSeconds(timeOfIntervalMillis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeOfIntervalMillis))
        );
        textViewTimeOfInterval.setText(timeOfIntervalString);
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