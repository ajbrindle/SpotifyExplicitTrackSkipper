package com.sk7software.spotifyexplicittrackskipper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrew on 18/06/2017.
 */

public class TrackLookup {

    private static final String TRACK_URI = "https://api.spotify.com/v1/tracks/";
    private static final String ID_PREFIX = "spotify:track:";

    private static final String TAG = TrackLookup.class.getSimpleName();

    private Context context;

    public TrackLookup(Context context) {
        this.context = context.getApplicationContext();
    }

    public void skipExplicit(String id) {
        lookupTrack(setId(id));
    }

    private void lookupTrack(final String id) {
        // Check whether authorisation has expired
        if (SpotifyUtil.authExpired()) {
            Toast.makeText(context, "Spotify authorisation has expired", Toast.LENGTH_SHORT);
            SpotifyUtil.refreshSpotifyAuthToken(context, id, new SpotifyUtil.SpotifyCallback() {
                @Override
                public void onRequestCompleted(String callbackData) {
                    fetchTrackInfo(context, id);
                }
            });
        } else {
            fetchTrackInfo(context, id);
        }
    }

    private void fetchTrackInfo(final Context context, String id) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = TRACK_URI + id;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean explicit = response.getBoolean("explicit");
                            Log.d(TAG, "Explicit: " + explicit);
                            if (explicit) {
                                SpotifyUtil.skipTrack(context);
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "JSONException: " + e.getMessage());
                        }
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO Auto-generated method stub
                                Log.d(TAG, "Error => " + error.toString());
                            }
                        }
                ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String token = PreferencesUtil.getStringPreference(AppConstants.PREFERENCE_AUTH_TOKEN);
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        Log.d(TAG, jsObjRequest.toString());
        queue.add(jsObjRequest);
    }


    private String setId(String id) {
        if (id.contains(ID_PREFIX)) {
            return id.substring(ID_PREFIX.length());
        } else {
            return id;
        }
    }
}
