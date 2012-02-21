/*
 * Copyright (c) 2012. Robert Wood <rob@rnwood.co.uk>
 * All rights reserved.
 */

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