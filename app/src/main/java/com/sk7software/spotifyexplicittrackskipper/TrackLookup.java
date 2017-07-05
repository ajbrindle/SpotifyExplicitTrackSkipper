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

    public void skipExplicit (String id) {
        lookupTrack(setId(id));
    }

    private void lookupTrack(String id) {
        // Check whether authorisation has expired
        SharedPreferences prefs = context.getSharedPreferences(
                AppConstants.APP_PREFERENCES_KEY, context.MODE_PRIVATE);
        String expiryTime = prefs.getString(AppConstants.PREFERENCE_AUTH_EXPIRY, "");
        if (hasExpired(expiryTime)) {
            Toast.makeText(context, "Spotify authorisation has expired", Toast.LENGTH_SHORT);
            return;
        }

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
                                skipTrack();
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "JSONException: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "Error => " + error.toString());
                    }
                }
        )
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                SharedPreferences prefs = context.getSharedPreferences(
                        AppConstants.APP_PREFERENCES_KEY, context.MODE_PRIVATE);
                String token = prefs.getString(AppConstants.PREFERENCE_AUTH_TOKEN, "");
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        Log.d(TAG, jsObjRequest.toString());
        queue.add(jsObjRequest);
    }

    private boolean hasExpired(String expiryTimeStr) {
        if (expiryTimeStr.length() == 0) {
            return true;
        } else {
            DateFormat formatter = new SimpleDateFormat(AppConstants.EXPIRY_TIME_FORMAT);
            try {
                Date expiryTime = formatter.parse(expiryTimeStr);
                if (expiryTime.before(new Date())) {
                    return true;
                }
            } catch (ParseException e) {
                Log.d(TAG, "Error parsing expiry time: " + e.getMessage());
                return true;
            }
        }

        return false;
    }

    private void skipTrack() {
        SharedPreferences prefs = context.getSharedPreferences(
                AppConstants.APP_PREFERENCES_KEY, context.MODE_PRIVATE);
        boolean skip = prefs.getBoolean(AppConstants.PREFERENCE_SKIP_EXPLICIT, false);

        if (skip) {
            Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
            i.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.internal.receiver.MediaButtonReceiver"));
            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
            context.sendBroadcast(i);

            //release the button
            i = new Intent(Intent.ACTION_MEDIA_BUTTON);
            i.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.internal.receiver.MediaButtonReceiver"));
            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
            context.sendBroadcast(i);
        }
    }

    private String setId(String id) {
        if (id.contains(ID_PREFIX)) {
            return id.substring(ID_PREFIX.length());
        } else {
            return id;
        }
    }
}
