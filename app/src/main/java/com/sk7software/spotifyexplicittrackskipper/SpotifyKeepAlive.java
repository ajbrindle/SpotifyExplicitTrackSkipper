package com.sk7software.spotifyexplicittrackskipper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.SystemUpdatePolicy;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SpotifyKeepAlive extends BroadcastReceiver
{
    private static final String TAG = SpotifyKeepAlive.class.getSimpleName();
    private static int KEEP_ALIVE_INTERVAL_S = 90;
    private static boolean alarmSet = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        sendPing(context);
        setAlarm(context);
    }

    public void initialise(Context context, int interval) {
        if (!alarmSet) {
            Log.d(TAG, "Initialising keep alive");
            alarmSet = true;
            if (interval > 0) KEEP_ALIVE_INTERVAL_S = interval;
            setAlarm(context);
        }
    }

    private void setAlarm(Context context)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.PLAY_TIME_DISPLAY_FORMAT);
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, SpotifyKeepAlive.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        long wakeupTime = System.currentTimeMillis() + (1000 * KEEP_ALIVE_INTERVAL_S);
        am.set(AlarmManager.RTC, wakeupTime, pi);
        Log.d(TAG, "Keep alive alarm set for: " + sdf.format(new Date(wakeupTime)));
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, SpotifyKeepAlive.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        Log.d(TAG, "Keep alive cancelled");
        alarmSet = false;
    }

    public static void sendPing(Context context) {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.internal.receiver.MediaButtonReceiver"));
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK));
        context.sendBroadcast(i);
        Log.d(TAG, "Keep alive sent");
    }
}
