package com.example.saveyourride.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.saveyourride.fragments.Activity_Active;
import com.example.saveyourride.R;
import com.example.saveyourride.utils.PagerAdapter;

import java.util.Objects;


public class MainScreen extends AppCompatActivity {

    private final int PASSIVE_TAB = 0;
    private final int ACTIVE_TAB = 1;

    /// TEST
    private Intent activeIntent;
    ///

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.passive)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.active)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.fragment_container);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // IMPLEMENT LATER
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // IMPLEMENT LATER
            }
        });

        // First selected Tab
        viewPager.setCurrentItem(PASSIVE_TAB);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //menu activity bekannt
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //click listener quasi.
        switch (item.getItemId()) {
            case R.id.settingsButton:
                System.out.println("SETTINGS");

                activeIntent = activeIntent != null? activeIntent : new Intent(this, Activity_Active.class);

                // settingsIntent.putExtra(Intent.EXTRA_TEXT, aktienInfo);
                startActivityIfNeeded(activeIntent, 0);
                //startActivity(activeIntent);
                break;
        }
        return super.onOptionsItemSelected(item); //To change body of generated methods, choose Tools | Templates.
    }
}
