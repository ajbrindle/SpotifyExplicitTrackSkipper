package com.sk7software.spotifyexplicittrackskipper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by andre_000 on 06/07/2017.
 */

public class PreferencesUtil {

    private static Context context;

    public static void setContext(Context c) {

        if (context == null) {
            context = c;
        }
    }

    public static void addPreference(String name, String value) {
        PreferencesUtil.edit(context).putString(name, value).commit();
    }

    public static void addPreference(String name, int value) {
        PreferencesUtil.edit(context).putInt(name, value).commit();
    }

    public static void addPreference(String name, boolean value) {
        PreferencesUtil.edit(context).putBoolean(name, value).commit();
    }

    public static String getStringPreference(String name) {
        return prefs(context).getString(name, "");
    }

    public static int getIntPreference(String name) {
        return prefs(context).getInt(name, 0);
    }

    public static boolean getBooleanPreference(String name) {
        return prefs(context).getBoolean(name, false);
    }

    public static void clearStringPreference(String name) {
        PreferencesUtil.edit(context).putString(name, "").commit();
    }

    private static SharedPreferences.Editor edit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                AppConstants.APP_PREFERENCES_KEY, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        return editor;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(
                AppConstants.APP_PREFERENCES_KEY, context.MODE_PRIVATE);
    }
}
