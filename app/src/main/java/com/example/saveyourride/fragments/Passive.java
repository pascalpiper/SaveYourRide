package com.example.saveyourride.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.saveyourride.R;
import com.example.saveyourride.activities.PassiveMode;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Passive extends Fragment{

    private SupportMapFragment mapFragment;
    private Button buttonStartPassiveMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_passive, container, false);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    LatLng latLng = new LatLng(51.023226, 7.564927);
                    googleMap.addMarker(new MarkerOptions().position(latLng)
                            .title("Singapore"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                    googleMap.getUiSettings().setAllGesturesEnabled(false);
                }
            });
        }

        // R.id.map is a FrameLayout, not a Fragment
        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();

        buttonStartPassiveMode = (Button) rootView.findViewById(R.id.buttonStartPassiveMode);

        buttonStartPassiveMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("PASSIVE MODE ACTIVITY");
                Intent passiveModeIntent = new Intent(getActivity(), PassiveMode.class);
                startActivity(passiveModeIntent);
            }
        });

        return rootView;
    }
}
