/*
 * Copyright (c) 2012. Robert Wood <rob@rnwood.co.uk>
 * All rights reserved.
 */

package uk.co.rnwood.dropboxlivewallpaper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;


public class SetupWizardActivity4 extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setupwizardactivity4);

        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SetupWizardActivity4.this, SetupWizardActivity3.class));
                finish();
            }
        });


        final RadioButton fillScreenRadioButton = (RadioButton) findViewById(R.id.fillScreenRadioButton);
        RadioButton showWholeImageRadioButton = (RadioButton) findViewById(R.id.showWholeImageRadioButton);

        switch (new Preferences(this).getScaleMode()) {
            case Scale:
                showWholeImageRadioButton.setChecked(true);
                break;
            case Crop:
                fillScreenRadioButton.setChecked(true);
                break;
        }

        Button nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Preferences(SetupWizardActivity4.this).setScaleMode(fillScreenRadioButton.isChecked() ? Preferences.ScaleMode.Crop : Preferences.ScaleMode.Scale);

                startActivity(new Intent(SetupWizardActivity4.this, SetupWizardFinishActivity.class));
                finish();
            }
        });

    }


}