/*
 * Copyright (c) 2012. Robert Wood <rob@rnwood.co.uk>
 * All rights reserved.
 */

package uk.co.rnwood.dropboxlivewallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class Preferences {

    public static final String KEY_FOLDERS = "folders";
    public static final String KEY_ONLYDOWNLOADONWIFI = "onlydownloadonwifi";
    public static final String KEY_SCALEMODE = "scalemode";

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
    public static final String KEY_EFFECT_TRANSPARENT = "effect_transparent";
    public static final String KEY_BACKGROUNDCOLOUR = "backgroundcolour";
    public static final String KEY_LOWQUALITYIMAGES = "lowqualityimages";
    public static final String KEY_DONEFIRSTTIMESETUP = "donefirsttimesetup";

    private SharedPreferences sharedPreferences;

    public String getAuthKey() {
        return sharedPreferences.getString(KEY_DROPBOXAUTHKEY, null);
    }

    public void setAuthKeyAndSecret(String key, String secret) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_DROPBOXAUTHKEY, key);
        editor.putString(KEY_DROPBOXAUTHSECRET, secret);
        editor.commit();
    }

    public String getAuthSecret() {
        return sharedPreferences.getString(KEY_DROPBOXAUTHSECRET, null);
    }

    public String[] getFolders() {
        return sharedPreferences.getString(KEY_FOLDERS, "*").split("\n");
    }


    public long getLastImageChange() {
        return sharedPreferences.getLong(KEY_LASTIMAGECHANGE, 0);
    }

    public void setLastImageChange(long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LASTIMAGECHANGE, value);
        editor.commit();
    }

    public int getFrequency() {
        String freqString = sharedPreferences.getString(KEY_FREQUENCY, "300");
        return Integer.parseInt(freqString);
    }

    public boolean getLowQualityImages() {
        return sharedPreferences.getBoolean(KEY_LOWQUALITYIMAGES, false);
    }

    public boolean getEffectGrayscale() {
        return sharedPreferences.getBoolean(KEY_EFFECT_GRAYSALE, false);
    }

    public boolean getEffectTransparent() {
        return sharedPreferences.getBoolean(KEY_EFFECT_TRANSPARENT, false);
    }

    public boolean getEffectSepia() {
        return sharedPreferences.getBoolean(KEY_EFFECT_SEPIA, false);
    }

    public boolean getEffectBlurEdges() {
        return sharedPreferences.getBoolean(KEY_EFFECT_BLUREDGES, false);
    }

    public boolean getDoneFirstTimeSetup() {
        return sharedPreferences.getBoolean(KEY_DONEFIRSTTIMESETUP, false);
    }

    public void setDoneFirstTimeSetup(Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_DONEFIRSTTIMESETUP, value);
        editor.commit();
    }

    public boolean getEffectInstantPhoto() {
        return sharedPreferences.getBoolean(KEY_EFFECT_INSTANTPHOTO, false);
    }

    public int getBackgroundColour() {

        return Color.parseColor(sharedPreferences.getString(KEY_BACKGROUNDCOLOUR, "BLACK"));
    }

    public boolean getOnlyDownloadOnWifi() {

        return sharedPreferences.getBoolean(KEY_ONLYDOWNLOADONWIFI, false);
    }

    public ScaleMode getScaleMode() {
        return ScaleMode.valueOf(sharedPreferences.getString(KEY_SCALEMODE, ScaleMode.Scale.toString()));
    }

    public void setScaleMode(ScaleMode value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SCALEMODE, value.toString());
        editor.commit();
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
