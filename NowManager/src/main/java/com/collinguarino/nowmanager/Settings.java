package com.collinguarino.nowmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.ImageButton;

public class Settings extends PreferenceActivity  {

    // implements SharedPreferences.OnSharedPreferenceChangeListener

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.layout.settings);

        setContentView(R.layout.custom_preferences); // uses the listview in custom_preferences as the preferenceresource

        //getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this); // required for onSharedPreferenceChanged() to be called

        ImageButton share = (ImageButton) findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Now Manager App: https://play.google.com/store/apps/details?id=com.collinguarino.nowmanager";
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share To:"));

            }
        });

        ImageButton rate = (ImageButton) findViewById(R.id.rate);
        rate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.collinguarino.nowmanager")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.collinguarino.nowmanager")));
                }

            }
        });

        ImageButton apps = (ImageButton) findViewById(R.id.apps);
        apps.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Collin+Guarino")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Collin+Guarino")));
                }

            }
        });

    }

    /*private void updatePreference(Preference preference) {
        ListPreference audioSensitivity = (ListPreference) findPreference("audio_sensitivity");

        // defined new sharedpreference because super didn't work
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        if (preferences.getBoolean("audioResponse", false) == false) {
            audioSensitivity.setSummary("Must have the above setting enabled.");
        } else {
            audioSensitivity.setSummary("Sensitivity varies between devices.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                    updatePreference(preferenceGroup.getPreference(j));
                }
            } else {
                updatePreference(preference);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key));
    }*/
}
