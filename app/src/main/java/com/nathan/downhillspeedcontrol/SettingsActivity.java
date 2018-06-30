package com.nathan.downhillspeedcontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;

import static com.nathan.downhillspeedcontrol.DisplayActivity.defaultNightMode;

public class SettingsActivity extends AppCompatActivity {
    public static final String KEY_SPEED = "speed";
    public static final String KEY_TIME = "time";
    public static final String KEY_SOUND_VOLUME = "volume";
    public static final String KEY_NIGHT_MODE = "nightMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isNightMode = sharedPref.getBoolean(SettingsActivity.KEY_NIGHT_MODE, defaultNightMode);
        if (isNightMode) {
            setTheme(R.style.DarkTheme);
        }

        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
