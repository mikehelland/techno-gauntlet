package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {

    public final static String POINT_COUNT = "POINT_COUNT";



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
