package com.sk7software.spotifyexplicittrackskipper.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sk7software.spotifyexplicittrackskipper.AppConstants;
import com.sk7software.spotifyexplicittrackskipper.BuildConfig;
import com.sk7software.spotifyexplicittrackskipper.TrackBroadcastReceiver;
import com.sk7software.spotifyexplicittrackskipper.exception.NotLoggedInException;
import com.sk7software.spotifyexplicittrackskipper.model.Auth;
import com.sk7software.spotifyexplicittrackskipper.model.Track;
import com.sk7software.spotifyexplicittrackskipper.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andre_000 on 06/07/2017.
 */

public class SpotifyUtil {

//    public static final String SPOTIFY_REFRESH_URL = "http://www.sk7software.com/spotify/SpotifyAuthorise/refresh.php?refreshToken=";
    public static final String SPOTIFY_REFRESH_URL = "https://spotifyauthorise.eu-gb.mybluemix.net/refresh.php?refreshToken=";
    public static final String SPOTIFY_USER_URL = "https://api.spotify.com/v1/me";
    public static final String SPOTIFY_NEXT_URL = "https://api.spotify.com/v1/me/player/next";

    private static final String TAG = SpotifyUtil.class.getSimpleName();
    private static RequestQueue queue;

    private synchronized static RequestQueue getQueue(Context context) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }
        return queue;
    }

    public interface SpotifyCallback {
        public void onRequestCompleted(Map<String, Object>callbackData);
        public void onError(Exception e);
    }

    public static void refreshSpotifyAuthToken(Context context, String refreshToken, SpotifyUtil.SpotifyCallback callback)
        throws NotLoggedInException {
        if ("".equals(refreshToken) && BuildConfig.FLAVOR.equals("lite")) {
            // Can't refresh so update service to say not skipping tracks
            Intent i = new Intent(context, TrackBroadcastReceiver.class);
            context.stopService(i);

            // Set the refresh token to "no refresh" to stop the service going into a stop/start loop
            PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_REFRESH_TOKEN, AppConstants.NO_REFRESH);
            i.putExtra("skipExplicit", false);
            i.putExtra("loggedOut", true);
            Log.d(TAG, "Restarted service in logged-out mode");
            context.startService(i);
            throw new NotLoggedInException();
        } else if (AppConstants.NO_REFRESH.equals(refreshToken)) {
            // Do nothing
            throw new NotLoggedInException();
        } else {
            getNewAccessToken(context, refreshToken, callback);
        }
    }

    private static void getNewAccessToken(final Context context, final String refreshToken, final SpotifyCallback callback) {
        String url = SPOTIFY_REFRESH_URL + refreshToken;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Map<String, Object> callbackData = new HashMap<>();
                            Auth a = Auth.createFromJSON(response);
                            callbackData.put("auth", a);
                            callback.onRequestCompleted(callbackData);
                        } catch (IOException e) {
                            Log.d(TAG, "Error parsing auth info: " + e.getMessage());
                            callback.onError(e);
                        }
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "Error => " + error.toString());
                                callback.onError(error);
                            }
                        }
                );
        Log.d(TAG, jsObjRequest.toString());
        getQueue(context).add(jsObjRequest);
    }

    public static void showLoginDetails(final Context context, final SpotifyCallback callback) {
        String url = SPOTIFY_USER_URL;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            User u = User.createFromJSON(response);
                            Map<String, Object> callbackData = new HashMap<>();
                            callbackData.put("user", u);
                            callback.onRequestCompleted(callbackData);
                            Log.d(TAG, "User: " + u.getId());
                        } catch (IOException e) {
                            Log.d(TAG, "Error parsing user response: " + e.getMessage());
                            callback.onError(e);
                        }
                    }
                },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "Error => " + error.toString());
                                callback.onError(error);
                            }
                        }
                ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String token = PreferencesUtil.getInstance().getStringPreference(AppConstants.PREFERENCE_AUTH_TOKEN);
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        Log.d(TAG, jsObjRequest.toString());
        getQueue(context).add(jsObjRequest);
    }

    public static void fetchTrackInfo(final Context context, final String url, final SpotifyCallback callback) {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Track t = Track.createFromJSON(response);
                            Map<String, Object> callbackData = new HashMap<>();
                            Log.d(TAG, "Explicit: " + t.isExplicit());
                            callbackData.put("track", t);
                            callback.onRequestCompleted(callbackData);
                        } catch (JSONException je) {
                            Log.d(TAG, "JSONException: " + je.getMessage());
                            callback.onError(je);
                        } catch (IOException ioe) {
                            Log.d(TAG, "Error parsing track response: " + ioe.getMessage());
                            callback.onError(ioe);
                        }
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "Error => " + error.toString());
                                callback.onError(error);
                            }
                        }
                ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String token = PreferencesUtil.getInstance().getStringPreference(AppConstants.PREFERENCE_AUTH_TOKEN);
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 4, 1));
        getQueue(context).add(jsObjRequest);
    }

    /*
    public static boolean skipTrack(Context context) {
        boolean skip = PreferencesUtil.getInstance().getBooleanPreference(AppConstants.PREFERENCE_SKIP_EXPLICIT);

        if (skip) {
            // Press the "next" button
            Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
            i.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.internal.receiver.MediaButtonReceiver"));
            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
            context.sendBroadcast(i);

            // Release the "next" button
            i = new Intent(Intent.ACTION_MEDIA_BUTTON);
            i.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.internal.receiver.MediaButtonReceiver"));
            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
            context.sendBroadcast(i);
        }

        return skip;
    }
    */

    public static boolean skipTrack(Context context) {
        boolean skip = PreferencesUtil.getInstance().getBooleanPreference(AppConstants.PREFERENCE_SKIP_EXPLICIT);

        if (skip) {
            StringRequest stringRequest = new StringRequest
                    (Request.Method.POST, SPOTIFY_NEXT_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "Track skipped: " + response);
                        }
                    },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d(TAG, "Error => " + error.toString());
                                }
                            }
                    ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    String token = PreferencesUtil.getInstance().getStringPreference(AppConstants.PREFERENCE_AUTH_TOKEN);
                    params.put("Authorization", "Bearer " + token);
                    return params;
                }
            };
            getQueue(context).add(stringRequest);
        }
        return skip;
    }
}
