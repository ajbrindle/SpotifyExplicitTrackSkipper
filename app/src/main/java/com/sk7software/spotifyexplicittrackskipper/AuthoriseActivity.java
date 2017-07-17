package com.sk7software.spotifyexplicittrackskipper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class AuthoriseActivity extends Activity implements View.OnClickListener  {

    private static final String SPOTIFY_AUTHORISATION_URL = "https://accounts.spotify.com/authorize/" +
            "?client_id=" + AppConstants.CLIENT_ID +
            "&response_type=code" +
            "&scope=" + getEncodedURL(AppConstants.SPOTIFY_SCOPES) +
            "&redirect_uri=" + getEncodedURL(AppConstants.REDIRECT_URI) +
            "&state=sk7" +
            "&show_dialog=true";

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 2234;
    private static final String TAG = AuthoriseActivity.class.getSimpleName();

    private WebView webViewSpotify;
    private Button btnLogout;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorise);

        // Initialise context for preferences
        PreferencesUtil.setContext(getApplicationContext());

        btnLogout = (Button)findViewById(R.id.btnLogout);
        btnNext = (Button)findViewById(R.id.btnNext);
        authenticate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //updateUI();
    }

    @Override
    public void onClick(View view) {
        if (view == null) return;
        if (view.getId() == R.id.btnLogout) {
            PreferencesUtil.clearStringPreference(AppConstants.PREFERENCE_AUTH_TOKEN);
            PreferencesUtil.clearStringPreference(AppConstants.PREFERENCE_REFRESH_TOKEN);
            PreferencesUtil.clearStringPreference(AppConstants.PREFERENCE_AUTH_EXPIRY);
            finish();
        } else if (view.getId() == R.id.btnNext) {
            launchMainActivity();
        }
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
        final ProgressDialog pd = ProgressDialog.show(this, "", "Authenticating with Spotify...", true);
        webViewSpotify = (WebView) findViewById(R.id.webViewSpotify);
        webViewSpotify.setVisibility(View.VISIBLE);
        webViewSpotify.getSettings().setJavaScriptEnabled(true);
        webViewSpotify.getSettings().setLoadWithOverviewMode(true);
        webViewSpotify.addJavascriptInterface(new AuthoriseJavaScriptInterface(), "INTERFACE");
        webViewSpotify.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                pd.show();
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                pd.dismiss();
                view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementById('access').innerText);");
            }
        });
//        webViewSpotify.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementById('access').innerText);");
//            }
//        });
        webViewSpotify.loadUrl(SPOTIFY_AUTHORISATION_URL);

    }

    private void launchMainActivity() {
        updateUI();

        // Launch main activity
        Intent i = new Intent(AuthoriseActivity.this, MainActivity.class);
        startActivity(i);
    }

    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Show logged in details in text field
                TextView txtLoggedIn = (TextView)findViewById(R.id.txtLoggedIn);
                TextView txtUserId = (TextView)findViewById(R.id.txtUserId);
                ImageView imgUser = (ImageView)findViewById(R.id.imgUser);
                SpotifyUtil.showLoginDetails(getApplicationContext(), txtUserId, imgUser);
                btnLogout.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
                txtLoggedIn.setVisibility(View.VISIBLE);
                txtUserId.setVisibility(View.VISIBLE);
                imgUser.setVisibility(View.VISIBLE);

                if (webViewSpotify != null) {
                    webViewSpotify.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private static String getEncodedURL(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
