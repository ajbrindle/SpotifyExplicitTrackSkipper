package com.sk7software.spotifyexplicittrackskipper;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Andrew on 18/06/2017.
 */

public class AppConstants {
    public static final String APP_PREFERENCES_KEY = "SK7_SPOTIFY_TRACK_PREFS";
    public static final String PREFERENCE_AUTH_TOKEN = "PREF_SPOTIFY_AUTH_TOKEN";
    public static final String PREFERENCE_REFRESH_TOKEN = "PREF_SPOTIFY_REFRESH_TOKEN";
    public static final String PREFERENCE_SKIP_EXPLICIT = "PREF_SKIP_EXPLICIT";
    public static final String PREFERENCE_AUTH_EXPIRY = "PREF_AUTH_EXPIRY";

    public static final String EXPIRY_TIME_FORMAT = "dd/MM/yyyy HH:mm";

    public static String calcExpiryTime(String expiresInSecs) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, Integer.valueOf(expiresInSecs));
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.EXPIRY_TIME_FORMAT);
        String expiryTime = sdf.format(cal.getTime());
        return expiryTime;
    }
}
