/*
 * Copyright (c) 2012. Robert Wood <rob@rnwood.co.uk>
 * All rights reserved.
 */

package uk.co.rnwood.dropboxlivewallpaper;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;

import java.util.Arrays;
import java.util.Vector;

public class DropboxGalleryPreference extends DialogPreference {

    private boolean[] itemSelection;

    public DropboxGalleryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DropboxGalleryPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void showDialog(Bundle state) {

        super.showDialog(state);

        final AlertDialog alertDialog = (AlertDialog) getDialog();

        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemSelection.length > 0) {
                    boolean value = !itemSelection[0];

                    ListView list = alertDialog.getListView();
                    for (int index = 0; index < list.getCount(); index++) {
                        itemSelection[index] = value;
                        list.setItemChecked(index, value);
                    }
                }
            }
        });
    }

    private CharSequence[] entries;

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {

        try {

            Preferences preferences = new Preferences(getContext());


            AndroidAuthSession session = new AndroidAuthSession(new AppKeyPair(Wallpaper.DROPBOX_APPKEY, Wallpaper.DROPBOX_APPSECRET), Session.AccessType.DROPBOX);
            session.setAccessTokenPair(new AccessTokenPair(preferences.GetAuthKey(), preferences.GetAuthSecret()));
            DropboxAPI<AndroidAuthSession> api = new DropboxAPI<AndroidAuthSession>(session);


            Vector<CharSequence> entriesList = new Vector<CharSequence>();
            DropboxAPI.Entry photosFolder = api.metadata("/Photos", 0, null, true, null);

            for (DropboxAPI.Entry entry : photosFolder.contents) {
                if (entry.isDir) {
                    entriesList.add(entry.fileName());
                }
            }

            entries = new CharSequence[entriesList.size()];
            entriesList.toArray(entries);
            itemSelection = new boolean[entries.length];

            Vector<String> storedValues = new Vector<String>(Arrays.asList(getPersistedString("*").split("\n")));
            boolean allSelected = storedValues.size() == 1 && storedValues.get(0).equals("*");

            for (int index = 0; index < entries.length; index++) {
                if (allSelected || storedValues.contains(entries[index])) {
                    itemSelection[index] = true;
                }
            }

            builder.setNeutralButton("Toggle all", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //overidden in ShowDialog()
                }
            });


            builder.setMultiChoiceItems(entries, itemSelection,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        public void onClick(DialogInterface dialog, int which, boolean val) {
                            itemSelection[which] = val;
                        }
                    });
        } catch (Exception e) {
            builder.setPositiveButton(null, null);
            builder.setMessage(R.string.erroraccessingdropbox);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {

        if (positiveResult) {
            boolean allSelected = true;
            StringBuilder value = new StringBuilder();
            for (int index = 0; index < entries.length; index++) {
                allSelected &= itemSelection[index];

                if (itemSelection[index]) {
                    if (value.length() > 0) {
                        value.append("\n");
                    }

                    value.append(entries[index]);
                }
            }

            String stringValue = value.toString();
            if (allSelected) {
                stringValue = "*";
            }

            persistString(stringValue);
        }
    }
}
