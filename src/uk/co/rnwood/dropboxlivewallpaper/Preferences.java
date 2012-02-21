/*
 * Copyright (c) 2012. Robert Wood <rob@rnwood.co.uk>
 * All rights reserved.
 */

package uk.co.rnwood.dropboxlivewallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class Preferences {

    public Preferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static final String PREFS_NAME = "prefs";
    public static final String KEY_DROPBOXAUTHKEY = "authkey";
    public static final String KEY_DROPBOXAUTHSECRET = "authsecret";
    public static final String KEY_LASTIMAGECHANGE = "lastimagechange";
    public static final String KEY_FREQUENCY = "frequency";
    public static final String KEY_EFFECT_GRAYSALE = "effect_grayscale";
    public static final String KEY_EFFECT_SEPIA = "effect_sepia";
    public static final String KEY_EFFECT_BLUREDGES = "effect_bluredges";
    public static final String KEY_EFFECT_INSTANTPHOTO = "effect_instantphoto";
    public static final String KEY_BACKGROUNDCOLOUR = "backgroundcolour";
    public static final String KEY_LOWQUALITYIMAGES = "lowqualityimages";

    private SharedPreferences sharedPreferences;

    public String GetAuthKey() {
        return sharedPreferences.getString(KEY_DROPBOXAUTHKEY, null);
    }

    public void SetAuthKeyAndSecret(String key, String secret) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_DROPBOXAUTHKEY, key);
        editor.putString(KEY_DROPBOXAUTHSECRET, secret);
        editor.commit();
    }

    public String GetAuthSecret() {
        return sharedPreferences.getString(KEY_DROPBOXAUTHSECRET, null);
    }

    public String[] GetFolders() {
        return sharedPreferences.getString("folders", "*").split("\n");
    }


    public long GetLastImageChange() {
        return sharedPreferences.getLong(KEY_LASTIMAGECHANGE, 0);
    }

    public void SetLastImageChange(long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LASTIMAGECHANGE, value);
        editor.commit();
    }

    public int GetFrequency() {
        String freqString = sharedPreferences.getString(KEY_FREQUENCY, "300");
        return Integer.parseInt(freqString);
    }

    public boolean GetLowQualityImages() {
        return sharedPreferences.getBoolean(KEY_LOWQUALITYIMAGES, false);
    }

    public boolean GetEffectGrayscale() {
        return sharedPreferences.getBoolean(KEY_EFFECT_GRAYSALE, false);
    }

    public boolean GetEffectSepia() {
        return sharedPreferences.getBoolean(KEY_EFFECT_SEPIA, false);
    }

    public boolean GetEffectBlurEdges() {
        return sharedPreferences.getBoolean(KEY_EFFECT_BLUREDGES, false);
    }

    public boolean GetEffectInstantPhoto() {
        return sharedPreferences.getBoolean(KEY_EFFECT_INSTANTPHOTO, false);
    }

    public int GetBackgroundColour() {

        return Color.parseColor(sharedPreferences.getString(KEY_BACKGROUNDCOLOUR, "BLACK"));
    }

    public boolean GetOnlyDownloadOnWifi() {

        return sharedPreferences.getBoolean("onlydownloadonwifi", false);
    }

    public ScaleMode GetScaleMode() {
        return ScaleMode.valueOf(sharedPreferences.getString("scalemode", "Scale"));
    }

    enum ScaleMode {
        Scale,
        Crop
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }
}
