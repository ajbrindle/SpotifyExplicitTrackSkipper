package com.sk7software.spotifyexplicittrackskipper;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;
import com.sk7software.spotifyexplicittrackskipper.model.Auth;
import com.sk7software.spotifyexplicittrackskipper.ui.TrackAdapter;
import com.sk7software.spotifyexplicittrackskipper.model.Track;
import com.sk7software.spotifyexplicittrackskipper.util.DateUtil;
import com.sk7software.spotifyexplicittrackskipper.util.PreferencesUtil;
import com.sk7software.spotifyexplicittrackskipper.util.SpotifyUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrew on 18/06/2017.
 */


// TODO - separate the UI and network elements into separate classes

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
        if (DateUtil.authExpired()) {
            String refreshToken = PreferencesUtil.getInstance().getStringPreference(AppConstants.PREFERENCE_REFRESH_TOKEN);

            Toast.makeText(context, "Spotify authorisation has expired", Toast.LENGTH_SHORT);
            SpotifyUtil.refreshSpotifyAuthToken(context, refreshToken, new SpotifyUtil.SpotifyCallback() {
                @Override
                public void onRequestCompleted(Map<String, Object> callbackData) {
                    Auth a = (Auth)callbackData.get("auth");
                    String expiryTime = DateUtil.calcExpiryTime(a.getExpiresIn());
                    PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_TOKEN, a.getAccessToken());
                    PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_EXPIRY, expiryTime);
                    fetchTrackInfo(context, id, updateUI);
                }
                @Override
                public void onError(Exception e) {

                }
            });
        } else {
            fetchTrackInfo(context, id, updateUI);
        }
    }

    private void fetchTrackInfo(final Context context, String url, final boolean updateUI) {
        SpotifyUtil.fetchTrackInfo(context, url, new SpotifyUtil.SpotifyCallback() {
            @Override
            public void onRequestCompleted(Map<String, Object> callbackData) {
                Track t = (Track)callbackData.get("track");
                t.overrideExplicit(DatabaseUtil.getInstance(context));
                boolean skipped = false;

                if (t.isExplicit()) {
                    skipped = SpotifyUtil.skipTrack(context);
                    t.setSkipped(skipped);
                } else {
                    SpotifyKeepAlive.sendPing(context);
                }

                // Store track in track history
                storeTrack(t);

                // Update the UI if required
                if (updateUI) {
                    updateUI(t);
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private void storeTrack(Track t) {
        final DatabaseUtil db = DatabaseUtil.getInstance(context);

        // Check if this is the same as the most recent track in the list
        String latestTrack = db.getLatestTrackId();
        String nowPlaying = setId(t.getId());
        if (!nowPlaying.equals(latestTrack)) {
            // Store track info in database
            t.setPlayDate(new Date());
            Log.d(TAG, t.toString());
            db.addTrack(t);
        }
    }

    private void updateUI(Track t) {
        final DatabaseUtil db = DatabaseUtil.getInstance(context);
        int limit = PreferencesUtil.getInstance().getIntPreference(AppConstants.PREFERNECE_MAX_HISTORY_ITEMS);
        final List<Track> tracksList = db.getTracks(limit);
        trackAdapter.updateTracks(tracksList);
        trackAdapter.setDB(db);
        trackView.setLayoutManager(new LinearLayoutManager(mainActivity));
        trackView.setAdapter(trackAdapter);
        trackAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);

    }

    private String setId(String id) {
        if (id.contains(ID_PREFIX)) {
            return id.substring(ID_PREFIX.length());
        } else {
            return id;
        }
    }
}
