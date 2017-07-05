package com.sk7software.spotifyexplicittrackskipper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerNotificationCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback
{

    private static final String CLIENT_ID = "3b479b12ae87444c9384b1e5a14ca708";
    private static final String REDIRECT_URI = "sk7setc://callback";
    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 2234;
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView txtProgress;
    private ToggleButton togExplicit;

//    private Player mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtProgress = (TextView)findViewById(R.id.txtProgress);
        togExplicit = (ToggleButton)findViewById(R.id.togExplicitTracks);
        togExplicit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                        AppConstants.APP_PREFERENCES_KEY, getApplicationContext().MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(AppConstants.PREFERENCE_SKIP_EXPLICIT, isChecked);
                editor.commit();
            }
        });
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Log.d(TAG, "Authentication result: " + resultCode);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            Log.d(TAG, "Access token: " + response.getAccessToken());
            Log.d(TAG, "Expires in: " + response.getExpiresIn());
            Log.d(TAG, "Error: " + response.getError());
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                txtProgress.setText(response.getAccessToken());
                SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                        AppConstants.APP_PREFERENCES_KEY, getApplicationContext().MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(AppConstants.PREFERENCE_AUTH_TOKEN, response.getAccessToken());
                editor.putString(AppConstants.PREFERENCE_AUTH_EXPIRY, calcExpiryTime(response.getExpiresIn()));
                editor.commit();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(EventType playerEvent, PlayerState state) {
        Log.d(TAG, "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType error, String message) {
        Log.d(TAG, "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "User logged in");
        //mPlayer.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V");
    }

    @Override
    public void onLoggedOut() {
        Log.d(TAG, "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable throwable) {
        Log.d(TAG, "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d(TAG, "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(TAG, "Received connection message: " + message);
    }

    private String calcExpiryTime(int expiresInSecs) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, expiresInSecs);
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.EXPIRY_TIME_FORMAT);
        String expiryTime = sdf.format(cal.getTime());
        Log.d(TAG, "Expiry time: " + expiryTime);
        return expiryTime;
    }
}
