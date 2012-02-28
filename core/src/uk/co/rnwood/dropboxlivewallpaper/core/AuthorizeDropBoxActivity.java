/*
 * Copyright (c) 2012. Robert Wood <rob@rnwood.co.uk>
 * All rights reserved.
 */

package uk.co.rnwood.dropboxlivewallpaper.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;

public class AuthorizeDropBoxActivity extends Activity {

    DropboxAPI<AndroidAuthSession> api;
    boolean authInProgress = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.authorizedropboxactivity);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!authInProgress) {
            startAuth();
        } else {
            resumeAuth();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void resumeAuth() {
        boolean authError = false;
        authInProgress = false;

        try {
            api.getSession().finishAuthentication();
        } catch (IllegalStateException ex) {
            //Happens if auth cancelled
            authError = true;
        }

        if (!authError && api.getSession().authenticationSuccessful()) {

            AccessTokenPair accessTokenPair = api.getSession().getAccessTokenPair();

            Preferences preferences = new Preferences(this);
            preferences.setAuthKeyAndSecret(accessTokenPair.key, accessTokenPair.secret);


            if (getIntent().getAction().endsWith("NOTIFICATION")) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            }

            finish();

        } else {

            TextView label = (TextView) findViewById(R.id.statusText);
            label.setText(uk.co.rnwood.dropboxlivewallpaper.core.R.string.authorizationfailed);
        }


    }

    private void startAuth() {
        authInProgress = true;
        AndroidAuthSession session = new AndroidAuthSession(new AppKeyPair(Wallpaper.DROPBOX_APPKEY, Wallpaper.DROPBOX_APPSECRET), Session.AccessType.DROPBOX);
        api = new DropboxAPI<AndroidAuthSession>(session);
        api.getSession().startAuthentication(AuthorizeDropBoxActivity.this);
    }

}