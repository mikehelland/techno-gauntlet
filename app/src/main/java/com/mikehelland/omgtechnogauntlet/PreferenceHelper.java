package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {

    public final static String POINT_COUNT = "POINT_COUNT";
    public final static String LAST_CHANNEL_CONFIGURATION = "LAST_CHANNEL_CONFIGURATION";


    public static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    public static String getLastPartConfiguration(Context c) {
        return getPrefs(c).getString(LAST_CHANNEL_CONFIGURATION,
                "PRESET_KEYBOARD,PRESET_BASS,PRESET_HIPKIT,PRESET_PERCUSSION_SAMPLER");
    }

    public static int dingPointCount(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        int points = 1 + prefs.getInt(POINT_COUNT, 0);
        prefs.edit().putInt(POINT_COUNT, points).commit();

        return points;
    }

    public static int getPointCount(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getInt(POINT_COUNT, 0);
    }

}
