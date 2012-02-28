/*
 * Copyright (c) 2012. Robert Wood <rob@rnwood.co.uk>
 * All rights reserved.
 */

package uk.co.rnwood.dropboxlivewallpaper.core;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;


public class ConfigActivity extends PreferenceActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Preferences.PREFS_NAME);
        addPreferencesFromResource(uk.co.rnwood.dropboxlivewallpaper.core.R.xml.preferences);

        if ("uk.co.rnwood.dropboxlivewallpaper.SELECTDROPBOXFOLDERS".equals(getIntent().getAction())) {
            DropboxGalleryPreference p = (DropboxGalleryPreference) findPreference("folders");
            p.setOnCloseListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            p.show();

            return;
        } else if (!new Preferences(this).getDoneFirstTimeSetup()) {
            startActivity(new Intent(this, SetupWizardActivity.class));
            finish();
            return;
        }


        /*
     if (false)
     {
     findPreference(Preferences.KEY_LOWQUALITYIMAGES).setEnabled(false);
     findPreference(Preferences.KEY_EFFECT_GRAYSALE).setEnabled(false);
     findPreference(Preferences.KEY_EFFECT_SEPIA).setEnabled(false);
     findPreference(Preferences.KEY_EFFECT_BLUREDGES).setEnabled(false);
     findPreference(Preferences.KEY_EFFECT_INSTANTPHOTO).setEnabled(false);
     findPreference(Preferences.KEY_EFFECT_TRANSPARENT).setEnabled(false);
     findPreference(Preferences.KEY_LOWQUALITYIMAGES).setEnabled(false);
     findPreference("onlydownloadonwifi").setEnabled(false);
     } else {
         getPreferenceScreen().removePreference(findPreference("buyme"));
     }   */

    }


}