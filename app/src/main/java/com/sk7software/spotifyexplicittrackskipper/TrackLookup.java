package com.sk7software.spotifyexplicittrackskipper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;
import com.sk7software.spotifyexplicittrackskipper.list.TrackAdapter;
import com.sk7software.spotifyexplicittrackskipper.music.Track;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrew on 18/06/2017.
 */

public class TrackLookup {

    private static final String TRACK_URI = "https://api.spotify.com/v1/tracks/";
    private static final String NOW_PLAYING_URI = "https://api.spotify.com/v1/me/player/currently-playing";
    private static final String ID_PREFIX = "spotify:track:";

    private static final String TAG = TrackLookup.class.getSimpleName();

    private Context context;
    private Activity mainActivity;
    private RecyclerView trackView;
    private TrackAdapter trackAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public TrackLookup(Context context) {
        this.context = context;
    }

    public void skipExplicit(String id) {
        lookupTrack(TRACK_URI + setId(id), false);
    }

    public void getTrackInfo(Activity mainActivity, RecyclerView trackView,
                             TrackAdapter trackAdapter, SwipeRefreshLayout swipeRefreshLayout) {
        this.mainActivity = mainActivity;
        this.trackView = trackView;
        this.trackAdapter = trackAdapter;
        this.swipeRefreshLayout = swipeRefreshLayout;
        lookupTrack(NOW_PLAYING_URI, true);
    }

    private void lookupTrack(final String id, final boolean updateUI) {
        // Check whether authorisation has expired
        if (SpotifyUtil.authExpired()) {
            Toast.makeText(context, "Spotify authorisation has expired", Toast.LENGTH_SHORT);
            SpotifyUtil.refreshSpotifyAuthToken(context, id, new SpotifyUtil.SpotifyCallback() {
                @Override
                public void onRequestCompleted(String callbackData) {
                    fetchTrackInfo(context, id, updateUI);
                }
            });
        } else {
            fetchTrackInfo(context, id, updateUI);
        }
    }

    private void fetchTrackInfo(final Context context, String url, final boolean updateUI) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Track t = new Track(response);

                        boolean skipped = false;
                        Log.d(TAG, "Explicit: " + t.isExplicit());
                        if (t.isExplicit()) {
                            skipped = SpotifyUtil.skipTrack(context);
                            t.setSkipped(skipped);
                        }

                        final DatabaseUtil db = DatabaseUtil.getInstance(context);

                        // Check if this is the same as the most recent track in the list
                        String latestTrack = db.getLatestTrackId();
                        String nowPlaying = setId(t.getSpotifyId());
                        if (!nowPlaying.equals(latestTrack)) {
                            // Store track info in database
                            Log.d(TAG, t.toString());
                            db.addTrack(t);
                        }

                        if (updateUI) {
                            int limit = PreferencesUtil.getIntPreference(AppConstants.PREFERNECE_MAX_HISTORY_ITEMS);
                            final List<Track> tracksList = db.getTracks(limit);
                            trackAdapter.updateTracks(tracksList);
                            trackAdapter.setDB(db);
                            trackView.setLayoutManager(new LinearLayoutManager(mainActivity));
                            trackView.setAdapter(trackAdapter);
                            trackAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);

                            ItemTouchHelper itemTouchHelper =
                                    new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                                        @Override
                                        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                                            // Remove item from backing list here
                                            int adapterPosition = viewHolder.getAdapterPosition();
                                            int layoutPosition = viewHolder.getLayoutPosition();
                                            db.deleteTrack(trackAdapter.getIdAtPosition(adapterPosition), trackAdapter.getPlayTimeAtPosition(adapterPosition));
                                            trackAdapter.removeItem(adapterPosition);
                                        }

                                        public boolean onMove(RecyclerView view, RecyclerView.ViewHolder v1, RecyclerView.ViewHolder v2) {
                                            final int fromPos = v1.getAdapterPosition();
                                            final int toPos = v2.getAdapterPosition();
                                            // move item in `fromPos` to `toPos` in adapter.
                                            return true;
                                        }
                                    });

                            itemTouchHelper.attachToRecyclerView(trackView);
                        }
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
                String token = PreferencesUtil.getStringPreference(AppConstants.PREFERENCE_AUTH_TOKEN);
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 4, 0));
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
