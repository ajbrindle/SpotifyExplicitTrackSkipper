package com.sk7software.spotifyexplicittrackskipper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class TrackBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = TrackBroadcastReceiver.class.getSimpleName();

    static final class BroadcastTypes {
        static final String SPOTIFY_PACKAGE = "com.spotify.music";
        static final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        static final String QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";
        static final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";
    }

    public TrackBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // This is sent with all broadcasts, regardless of type. The value is taken from
        // System.currentTimeMillis(), which you can compare to in order to determine how
        // old the event is.
        long timeSentInMs = intent.getLongExtra("timeSent", 0L);

        String action = intent.getAction();

        if (action.equals(BroadcastTypes.METADATA_CHANGED)) {
            String trackId = intent.getStringExtra("id");
            String artistName = intent.getStringExtra("artist");
            String trackName = intent.getStringExtra("track");
            Log.d(TAG, "Track: " + trackName + "; Artist: " + artistName + " (" + trackId + ")");
            TrackLookup t = new TrackLookup(context.getApplicationContext());
            t.skipExplicit(trackId);
        }
    }
}
