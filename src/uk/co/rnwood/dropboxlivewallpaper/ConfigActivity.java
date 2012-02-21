package uk.co.rnwood.dropboxlivewallpaper;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class ConfigActivity extends PreferenceActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Preferences.PREFS_NAME);
        addPreferencesFromResource(R.xml.preferences);
    }
}