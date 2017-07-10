package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {

    public final static String POINT_COUNT = "POINT_COUNT";
    public final static String LAST_SAMPLER_ID = "LAST_SAMPLER_ID";


    public static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    public static int getDefaultSamplerId(Context c) {
        return getPrefs(c).getInt(LAST_SAMPLER_ID, 1);
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
