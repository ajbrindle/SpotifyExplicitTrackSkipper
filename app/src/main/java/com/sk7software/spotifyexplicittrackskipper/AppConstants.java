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
    public static final String PREFERNECE_MAX_HISTORY_ITEMS = "PREF_MAX_HISTORY";
    public static final String PREFERENCE_KEEP_ALIVE = "PREF_KEEP_ALIVE";
    public static final String PREFERENCE_KEEP_ALIVE_INTERVAL = "PREF_KEEP_ALIVE_INTERVAL";
    public static final String PREFERENCE_SWIPE_ACTION = "PREF_SWIPE_ACTION";

    public static final String APP_BROADCAST_INTENT = "com.sk7software.spotify.trackchanged";
    public static final String STOP_SERVICE_BROADCAST_INTENT = "com.sk7software.spotify.servicestop";

    public static final String EXPIRY_TIME_FORMAT = "dd/MM/yyyy HH:mm";
    public static final String PLAY_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String PLAY_TIME_DISPLAY_FORMAT = "dd/MM/yyyy HH:mm:ss";

    public static final String CLIENT_ID = "3b479b12ae87444c9384b1e5a14ca708";
//    public static final String REDIRECT_URI = "http://www.sk7software.com/spotify/SpotifyAuthorise/token.php";
    public static final String REDIRECT_URI = "https://spotifyauthorise.eu-gb.mybluemix.net/token.php";
    public static final String REDIRECT_URI_LITE = "sk7setc://callback";
    public static final String SPOTIFY_SCOPES = "user-read-currently-playing user-read-playback-state user-modify-playback-state";

    public static final String ADMOB_APP_ID = "ca-app-pub-9041522149264298~7515417162";
    public static final String ADMOB_APP_UNIT_ID = "ca-app-pub-9041522149264298/4422349965";

    public static final String NO_REFRESH = "NO_REFRESH";

    public static final int SWIPE_ACTION_DELETE = 0;
    public static final int SWIPE_ACTION_TAG = 1;
}
