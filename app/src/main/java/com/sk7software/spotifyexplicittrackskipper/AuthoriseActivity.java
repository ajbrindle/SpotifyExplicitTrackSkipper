package com.sk7software.spotifyexplicittrackskipper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.Spotify;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class AuthoriseActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    private static final String CLIENT_ID = "3b479b12ae87444c9384b1e5a14ca708";
    private static final String REDIRECT_URI = "http://www.sk7software.com/spotify/SpotifyAuthorise/token.php"; //"sk7setc://callback";
    private static final String SPOTIFY_AUTHORISATION_URL = "https://accounts.spotify.com/authorize/" +
            "?client_id=" + CLIENT_ID +
            "&response_type=code" +
            "&redirect_uri=" + getEncodedURL(REDIRECT_URI) +
            "&state=sk7"; // +
    //"&show_dialog=true";

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 2234;
    private static final String TAG = AuthoriseActivity.class.getSimpleName();

    private TextView txtProgress;
    private WebView webViewSpotify;

//    private Player mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorise);

        txtProgress = (TextView) findViewById(R.id.txtProgress);
        txtProgress.setText("Authorising with Spotify....");

        if (SpotifyUtil.authExpired(getApplicationContext())) {
            // Determine if there is a refresh token
            if (!SpotifyUtil.refreshSpotifyAuthToken(getApplicationContext())) {
                class AuthoriseJavaScriptInterface {
                    @SuppressWarnings("unused")
                    @JavascriptInterface
                    public void processContent(String aContent) {
                        final String content = aContent;
                        setApplicationPreferences(aContent);
                    }
                }

                webViewSpotify = (WebView) findViewById(R.id.webViewSpotify);
                webViewSpotify.getSettings().setJavaScriptEnabled(true);
                webViewSpotify.addJavascriptInterface(new AuthoriseJavaScriptInterface(), "INTERFACE");

                webViewSpotify.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementById('access').innerText);");//[0].innerText);");
                    }
                });
                webViewSpotify.loadUrl(SPOTIFY_AUTHORISATION_URL);
            } else {
                // Auth token refreshed
                launchMainActivity();
            }
        } else {
            // Auth token not expired
            launchMainActivity();
        }
        //webViewSpotify.loadUrl("http://www.sk7software.com/spotify/SpotifyAuthorise/index.php");

        /*
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        */
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
                //editor.putString(AppConstants.PREFERENCE_AUTH_EXPIRY, calcExpiryTime(response.getExpiresIn()));
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

    private void setApplicationPreferences(String response) {
        if (response.contains("<SpotifyAccess>")) {
            String accessToken = response.substring(response.indexOf("<Access>") + 8, response.indexOf("</Access>"));
            String refreshToken = response.substring(response.indexOf("<Refresh>") + 9, response.indexOf("</Refresh>"));
            String expirySeconds = response.substring(response.indexOf("<Expires>") + 9, response.indexOf("</Expires>"));
            String expiryTime = AppConstants.calcExpiryTime(expirySeconds);
            Log.d(TAG, "Access token: " + accessToken);
            Log.d(TAG, "Refresh token: " + refreshToken);
            Log.d(TAG, "Expires in: " + expirySeconds);
            Log.d(TAG, "Expiry time: " + expiryTime);

            PreferencesUtil.addPreference(getApplicationContext(), AppConstants.PREFERENCE_AUTH_TOKEN, accessToken);
            PreferencesUtil.addPreference(getApplicationContext(), AppConstants.PREFERENCE_REFRESH_TOKEN, refreshToken);
            PreferencesUtil.addPreference(getApplicationContext(), AppConstants.PREFERENCE_AUTH_EXPIRY, expiryTime);

            launchMainActivity();
        }
    }

    private void launchMainActivity() {
        // Launch main activity
        Intent i = new Intent(AuthoriseActivity.this, MainActivity.class);
        startActivity(i);
    }

    private static String getEncodedURL(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
