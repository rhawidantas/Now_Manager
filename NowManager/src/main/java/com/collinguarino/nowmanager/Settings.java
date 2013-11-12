package com.collinguarino.nowmanager;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*addPreferencesFromResource(R.layout.settings);

        Preference emailDeveloper = (Preference) findPreference("EMAIL_DEVELOPER");
        assert emailDeveloper != null;
        emailDeveloper.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "cgtechapps@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Now Manager Feedback");
                startActivity(Intent.createChooser(emailIntent, "Email Developer:"));

                return true;
            }
        });*/
    }
}
