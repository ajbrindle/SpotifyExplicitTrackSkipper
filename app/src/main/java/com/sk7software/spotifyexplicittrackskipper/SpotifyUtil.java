package com.sk7software.spotifyexplicittrackskipper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sk7software.spotifyexplicittrackskipper.list.ImageLoadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andre_000 on 06/07/2017.
 */

public class SpotifyUtil {

    private static final String TAG = SpotifyUtil.class.getSimpleName();

    public interface SpotifyCallback {
        public void onRequestCompleted(String callbackData);
    }

    public static boolean authExpired() {
        String expiryTimeStr = PreferencesUtil.getInstance().getStringPreference(AppConstants.PREFERENCE_AUTH_EXPIRY);

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

    public static boolean refreshSpotifyAuthToken(Context context, String callbackData, SpotifyCallback callback) {
        String refreshToken = PreferencesUtil.getInstance().getStringPreference(AppConstants.PREFERENCE_REFRESH_TOKEN);
        if ("".equals(refreshToken)) {
            return false;
        } else {
            getNewAccessToken(context, refreshToken, callbackData, callback);
            return true;
        }
    }

    private static void getNewAccessToken(final Context context, final String refreshToken, final String callbackData, final SpotifyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://www.sk7software.com/spotify/SpotifyAuthorise/refresh.php?refreshToken=" + refreshToken;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String accessToken = response.getString("access_token");
                            Integer expirySeconds = response.getInt("expires_in");
                            String expiryTime = AppConstants.calcExpiryTime(expirySeconds.toString());
                            Log.d(TAG, "Access token: " + accessToken);
                            Log.d(TAG, "Expires in: " + expirySeconds);
                            Log.d(TAG, "Expiry time: " + expiryTime);
                            PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_TOKEN, accessToken);
                            PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_EXPIRY, expiryTime);
                            callback.onRequestCompleted(callbackData);
                        } catch (JSONException e) {
                            Log.d(TAG, "JSONException: " + e.getMessage());
                        }
                    }
                },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "Error => " + error.toString());
                            }
                        }
                );
        Log.d(TAG, jsObjRequest.toString());
        queue.add(jsObjRequest);
    }

    public static void showLoginDetails(final Context context, final TextView txt, final ImageView imgUser) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://api.spotify.com/v1/me";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String userId = response.getString("id");
                            String imageURL = response.getJSONArray("images").getJSONObject(0).getString("url");
                            txt.setText(userId);
                            new ImageLoadTask(imageURL, null, null, imgUser).execute();
                            Log.d(TAG, "User: " + userId);
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
        queue.add(jsObjRequest);
    }

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
}
