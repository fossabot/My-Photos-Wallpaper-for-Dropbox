/*
 * Copyright (c) 2012. Robert Wood <rob@rnwood.co.uk>
 * All rights reserved.
 */

package uk.co.rnwood.dropboxlivewallpaper.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SetupWizardFinishActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Preferences(SetupWizardFinishActivity.this).setDoneFirstTimeSetup(true);
        setContentView(R.layout.setupwizardfinishactivity);

        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SetupWizardFinishActivity.this, SetupWizardActivity3.class));
                finish();
            }
        });

        Button advancedSettingsButton = (Button) findViewById(R.id.advancedSettingsButton);
        advancedSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SetupWizardFinishActivity.this, ConfigActivity.class));
            }
        });


        Button finishButton = (Button) findViewById(uk.co.rnwood.dropboxlivewallpaper.core.R.id.finishButton);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}