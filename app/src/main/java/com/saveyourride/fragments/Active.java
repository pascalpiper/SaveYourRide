package com.saveyourride.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.saveyourride.R;
import com.saveyourride.activities.ActiveMode;

public class Active extends Fragment {

    private Button buttonStartTimer;
    private Intent activeMode;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_active, container, false);

        buttonStartTimer = (Button) fragmentView.findViewById(R.id.buttonStartActiveMode);

        activeMode = new Intent(getContext(), ActiveMode.class);
        buttonStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(activeMode);
            }
        });

        return fragmentView;
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