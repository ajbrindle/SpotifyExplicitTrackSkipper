package com.sk7software.spotifyexplicittrackskipper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class TrackBroadcastReceiver extends BroadcastReceiver {
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
        Log.d("TrackBroadcastReceiver", "Action: " + action);
        if (action.equals(BroadcastTypes.METADATA_CHANGED)) {
            String trackId = intent.getStringExtra("id");
            String artistName = intent.getStringExtra("artist");
            String albumName = intent.getStringExtra("album");
            String trackName = intent.getStringExtra("track");
            int trackLengthInSec = intent.getIntExtra("length", 0);
            Log.d("TrackBroadcastReceiver", "Track: " + trackName + " (" + trackId + ")");
            // If track is explicit, skip to next
            if (trackName.contains("Hell")) {
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
        } else if (action.equals(BroadcastTypes.PLAYBACK_STATE_CHANGED)) {
            boolean playing = intent.getBooleanExtra("playing", false);
            int positionInMs = intent.getIntExtra("playbackPosition", 0);
            // Do something with extracted information
        } else if (action.equals(BroadcastTypes.QUEUE_CHANGED)) {
            // Sent only as a notification, your app may want to respond accordingly.
        }
    }
}
