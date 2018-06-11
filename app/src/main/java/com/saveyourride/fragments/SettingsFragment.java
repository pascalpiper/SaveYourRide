package com.saveyourride.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.saveyourride.R;
import com.saveyourride.activities.SettingsContacts;
import com.saveyourride.activities.SettingsPreviewMessage;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String KEY_PREF_CUSTOM_MESSAGE_SWITCH = "pref_enable_custom_message";
    SharedPreferences sharedPref;
    // Debug
    private final String TAG = "SettingsFragment";

    private SharedPreferences sharedPreferences;

    // Information

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


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState,
                                    String rootKey) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setPreferencesFromResource(R.xml.preferences, rootKey);

        checkIfCustomMessageEnabled();

        getPreferenceManager().getPreferenceScreen().findPreference("pref_enable_custom_message").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                checkIfCustomMessageEnabled();

                return false;
            }
        });

        getPreferenceManager().getPreferenceScreen().findPreference("pref_contacts").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), SettingsContacts.class));
                return false;
            }
        });

        getPreferenceManager().getPreferenceScreen().findPreference("pref_preview_message").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), SettingsPreviewMessage.class));
                return false;
            }
        });

    }

    private void checkIfCustomMessageEnabled() {
        Boolean switchPref = sharedPref.getBoolean
                (KEY_PREF_CUSTOM_MESSAGE_SWITCH, false);
        if (!switchPref) {
            getPreferenceManager().getPreferenceScreen().findPreference("pref_custom_message").setEnabled(false);
        } else {
            getPreferenceManager().getPreferenceScreen().findPreference("pref_custom_message").setEnabled(true);

        }
    }






}
