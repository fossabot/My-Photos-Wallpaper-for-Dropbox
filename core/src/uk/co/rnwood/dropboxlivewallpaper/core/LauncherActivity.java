/*
 * Copyright (c) 2012. Robert Wood <rob@rnwood.co.uk>
 * All rights reserved.
 */

package uk.co.rnwood.dropboxlivewallpaper.core;

import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class LauncherActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

        WallpaperManager wallpaperManager = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);

        WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
        if (wallpaperInfo == null || !wallpaperInfo.getPackageName().contains("dropboxlivewallpaper")) {
            Toast.makeText(getApplicationContext(), "Please select " + getString(R.string.app_name) + " then choose 'Set Wallpaper'", Toast.LENGTH_LONG).show();
            startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
        } else {
            startActivity(new Intent(getApplicationContext(), ConfigActivity.class));
            finish();
        }
    }
}