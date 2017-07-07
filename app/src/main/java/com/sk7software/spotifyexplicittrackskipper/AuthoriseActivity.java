package com.sk7software.spotifyexplicittrackskipper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class AuthoriseActivity extends Activity  {

    private static final String SPOTIFY_AUTHORISATION_URL = "https://accounts.spotify.com/authorize/" +
            "?client_id=" + AppConstants.CLIENT_ID +
            "&response_type=code" +
            "&redirect_uri=" + getEncodedURL(AppConstants.REDIRECT_URI) +
            "&state=sk7"; // +
    //"&show_dialog=true";

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 2234;
    private static final String TAG = AuthoriseActivity.class.getSimpleName();

    private TextView txtProgress;
    private WebView webViewSpotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorise);

        // Initialise context for preferences
        PreferencesUtil.setContext(getApplicationContext());

        txtProgress = (TextView) findViewById(R.id.txtProgress);
        txtProgress.setText("Authorising with Spotify....");

        authenticate();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
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

            PreferencesUtil.addPreference(AppConstants.PREFERENCE_AUTH_TOKEN, accessToken);
            PreferencesUtil.addPreference(AppConstants.PREFERENCE_REFRESH_TOKEN, refreshToken);
            PreferencesUtil.addPreference(AppConstants.PREFERENCE_AUTH_EXPIRY, expiryTime);

            launchMainActivity();
        }
    }

    private void authenticate() {
        if (SpotifyUtil.authExpired()) {
            // Determine if there is a refresh token
            if (!SpotifyUtil.refreshSpotifyAuthToken(getApplicationContext(), null, new SpotifyUtil.SpotifyCallback() {
                @Override
                public void onRequestCompleted(String callbackData) {
                    launchMainActivity();
                }
            })) {
                authenticateInWebView();
            }
        } else {
            // Auth token not expired
            launchMainActivity();
        }
    }

    private void authenticateInWebView() {
        // Call authorisation API in webview
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
                view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementById('access').innerText);");
            }
        });
        webViewSpotify.loadUrl(SPOTIFY_AUTHORISATION_URL);

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
