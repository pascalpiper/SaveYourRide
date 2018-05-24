package com.saveyourride.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.saveyourride.R;
import com.saveyourride.activities.SettingsContacts;
import com.saveyourride.activities.SettingsPreviewMessage;

import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String KEY_PREF_CUSTOM_MESSAGE_SWITCH = "pref_enable_custom_message";
    SharedPreferences sharedPref;
    // Debug
    private final String TAG = "SettingsFragment";


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState,
                                    String rootKey) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        setPreferencesFromResource(R.xml.preferences, rootKey);
        getPreferenceManager().getPreferenceScreen().findPreference("pref_enable_custom_message").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Boolean switchPref = sharedPref.getBoolean
                        (KEY_PREF_CUSTOM_MESSAGE_SWITCH, false);
                if (!switchPref) {
                    getPreferenceManager().getPreferenceScreen().findPreference("pref_custom_message").setEnabled(false);
                } else {
                    getPreferenceManager().getPreferenceScreen().findPreference("pref_custom_message").setEnabled(true);

                }

                Set<String> test = sharedPref.getStringSet("pref_included_information", null);

                for (String each : test) {
                    Log.d(TAG, each);
                }
//
//                Toast.makeText(getActivity(), test2[0], Toast.LENGTH_LONG).show();

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

}
