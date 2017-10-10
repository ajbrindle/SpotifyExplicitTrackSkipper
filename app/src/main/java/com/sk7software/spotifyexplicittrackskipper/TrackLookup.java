package com.sk7software.spotifyexplicittrackskipper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;
import com.sk7software.spotifyexplicittrackskipper.model.Auth;
import com.sk7software.spotifyexplicittrackskipper.ui.ActivityDataExchange;
import com.sk7software.spotifyexplicittrackskipper.model.Track;
import com.sk7software.spotifyexplicittrackskipper.util.DateUtil;
import com.sk7software.spotifyexplicittrackskipper.util.PreferencesUtil;
import com.sk7software.spotifyexplicittrackskipper.util.SpotifyUtil;

import java.util.Date;
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

    public TrackLookup(Context context) {
        this.context = context;
    }

    public void skipExplicit(String id) {
        lookupTrack(TRACK_URI + getTrackSpotifyId(id));
    }

    public void getTrackInfo() {
        lookupTrack(NOW_PLAYING_URI);
    }

    private void lookupTrack(final String id) {
        // Check whether authorisation has expired
        if (DateUtil.authExpired()) {
            String refreshToken = PreferencesUtil.getInstance().getStringPreference(AppConstants.PREFERENCE_REFRESH_TOKEN);
            SpotifyUtil.refreshSpotifyAuthToken(context, refreshToken, new SpotifyUtil.SpotifyCallback() {
                @Override
                public void onRequestCompleted(Map<String, Object> callbackData) {
                    Auth a = (Auth)callbackData.get("auth");
                    String expiryTime = DateUtil.calcExpiryTime(a.getExpiresIn());
                    PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_TOKEN, a.getAccessToken());
                    PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_EXPIRY, expiryTime);
                    fetchTrackInfo(id);
                }
                @Override
                public void onError(Exception e) {

                }
            });
        } else {
            fetchTrackInfo(id);
        }
    }

    private void fetchTrackInfo(String url) {
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
                Intent i = new Intent(AppConstants.APP_BROADCAST_INTENT);
                context.sendBroadcast(i);
            }

            @Override
            public void onError(Exception e) {
                // Update the UI if required
                Intent i = new Intent(AppConstants.APP_BROADCAST_INTENT);
                context.sendBroadcast(i);
            }
        });
    }

    private void storeTrack(Track t) {
        final DatabaseUtil db = DatabaseUtil.getInstance(context);

        // Check if this is the same as the most recent track in the list
        String latestTrack = db.getLatestTrackId();
        String nowPlaying = getTrackSpotifyId(t.getId());
        if (!nowPlaying.equals(latestTrack)) {
            // Store track info in database
            t.setPlayDate(new Date());
            Log.d(TAG, t.toString());
            db.addTrack(t);
        }
    }

    private String getTrackSpotifyId(String id) {
        if (id.contains(ID_PREFIX)) {
            return id.substring(ID_PREFIX.length());
        } else {
            return id;
        }
    }
}
