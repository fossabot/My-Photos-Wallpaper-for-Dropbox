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


public class SetupWizardActivity3 extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setupwizardactivity3);

        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SetupWizardActivity3.this, SetupWizardActivity2.class));
                finish();
            }
        });


        Button nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(SetupWizardActivity3.this, SetupWizardActivity4.class));
                finish();
            }
        });


        Button selectfoldersButton = (Button) findViewById(R.id.selectFoldersButton);
        selectfoldersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent("uk.co.rnwood.dropboxlivewallpaper.SELECTDROPBOXFOLDERS"));

            }
        });
    }


}