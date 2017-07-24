package com.sk7software.spotifyexplicittrackskipper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by andre_000 on 06/07/2017.
 */

public class PreferencesUtil {

    private static PreferencesUtil instance;
    private final SharedPreferences prefs;

    private PreferencesUtil(Context context) {
        prefs = context.getSharedPreferences(AppConstants.APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public synchronized static void init(Context context) {
        if (instance == null) {
            instance = new PreferencesUtil(context);
        }
    }

    public static PreferencesUtil getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Preferences not initialised");
        } else {
            return instance;
        }
    }

    public void addPreference(String name, String value) {
        prefs.edit().putString(name, value).commit();
    }

    public void addPreference(String name, int value) {
        prefs.edit().putInt(name, value).commit();
    }

    public void addPreference(String name, boolean value) {
        prefs.edit().putBoolean(name, value).commit();
    }

    public String getStringPreference(String name) {
        return prefs.getString(name, "");
    }

    public int getIntPreference(String name) {
        return prefs.getInt(name, 0);
    }

    public void tmpDel(String name) {
        prefs.edit().remove(name).commit();
    }

    public boolean getBooleanPreference(String name) {
        return prefs.getBoolean(name, false);
    }

    public void clearStringPreference(String name) {
        prefs.edit().putString(name, "").commit();
    }
}
