package com.sk7software.spotifyexplicittrackskipper;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Andrew on 18/06/2017.
 */

public class AppConstants {
    // Preferences constants
    public static final String APP_PREFERENCES_KEY = "SK7_SPOTIFY_TRACK_PREFS";
    public static final String PREFERENCE_AUTH_TOKEN = "PREF_SPOTIFY_AUTH_TOKEN";
    public static final String PREFERENCE_REFRESH_TOKEN = "PREF_SPOTIFY_REFRESH_TOKEN";
    public static final String PREFERENCE_SKIP_EXPLICIT = "PREF_SKIP_EXPLICIT";
    public static final String PREFERENCE_AUTH_EXPIRY = "PREF_AUTH_EXPIRY";

    public static final String EXPIRY_TIME_FORMAT = "dd/MM/yyyy HH:mm";

    public static final String CLIENT_ID = "3b479b12ae87444c9384b1e5a14ca708";
    public static final String REDIRECT_URI = "http://www.sk7software.com/spotify/SpotifyAuthorise/token.php";

    public static String calcExpiryTime(String expiresInSecs) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, Integer.valueOf(expiresInSecs));
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.EXPIRY_TIME_FORMAT);
        String expiryTime = sdf.format(cal.getTime());
        return expiryTime;
    }
}
