package com.sk7software.spotifyexplicittrackskipper;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.sk7software.spotifyexplicittrackskipper.ui.MainActivity;
import com.sk7software.spotifyexplicittrackskipper.util.PreferencesUtil;

public class TrackBroadcastReceiver extends Service {

    private static final String TAG = TrackBroadcastReceiver.class.getSimpleName();
    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 31570;

    static final class BroadcastTypes {
        static final String SPOTIFY_PACKAGE = "com.spotify.music";
        static final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        static final String QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";
        static final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastTypes.METADATA_CHANGED);
        filter.addAction(BroadcastTypes.PLAYBACK_STATE_CHANGED);
        registerReceiver(trackReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this)
                        .setContentTitle("Sanctify - Spotify Explicit Track Filter")
                        .setContentText("Sanctify is filtering explicit tracks from your Spotify playback.")
                        .setSmallIcon(R.drawable.trackskipper)
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .build();

        startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notification);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        unregisterReceiver(trackReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver trackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // This is sent with all broadcasts, regardless of type. The value is taken from
            // System.currentTimeMillis(), which you can compare to in order to determine how
            // old the event is.
            long timeSentInMs = intent.getLongExtra("timeSent", 0L);

            // Ensure there is a context for preferences
            PreferencesUtil.init(context);

            String action = intent.getAction();

            if (action.equals(BroadcastTypes.METADATA_CHANGED)) {
                String trackId = intent.getStringExtra("id");
                String artistName = intent.getStringExtra("artist");
                String trackName = intent.getStringExtra("track");
                Log.d(TAG, "Track: " + trackName + "; Artist: " + artistName + " (" + trackId + ")");
                TrackLookup t = new TrackLookup(context);
                t.skipExplicit(trackId);

                if (PreferencesUtil.getInstance().getBooleanPreference(AppConstants.PREFERENCE_KEEP_ALIVE)) {
                    int interval = PreferencesUtil.getInstance().getIntPreference(AppConstants.PREFERENCE_KEEP_ALIVE_INTERVAL);
                    SpotifyKeepAlive alarm = new SpotifyKeepAlive();
                    alarm.initialise(context, (interval > 0 ? interval : 90));
                }
            }
        }
    };
}
