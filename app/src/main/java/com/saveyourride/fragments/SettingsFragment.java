package com.saveyourride.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.saveyourride.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState,
                                    String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        getPreferenceManager().getPreferenceScreen().findPreference("pref_gps").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (getPreferenceManager().getPreferenceScreen().findPreference("pref_name").isEnabled()) {
                    getPreferenceManager().getPreferenceScreen().findPreference("pref_name").setEnabled(false);
                } else {
                    getPreferenceManager().getPreferenceScreen().findPreference("pref_name").setEnabled(true);

                }
                return false;
            }
        });
    }

}
