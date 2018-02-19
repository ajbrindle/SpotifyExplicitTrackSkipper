package com.sk7software.spotifyexplicittrackskipper.ui;

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

import com.sk7software.spotifyexplicittrackskipper.AppConstants;
import com.sk7software.spotifyexplicittrackskipper.BuildConfig;
import com.sk7software.spotifyexplicittrackskipper.TrackBroadcastReceiver;
import com.sk7software.spotifyexplicittrackskipper.exception.NotLoggedInException;
import com.sk7software.spotifyexplicittrackskipper.model.User;
import com.sk7software.spotifyexplicittrackskipper.util.PreferencesUtil;
import com.sk7software.spotifyexplicittrackskipper.R;
import com.sk7software.spotifyexplicittrackskipper.util.SpotifyUtil;
import com.sk7software.spotifyexplicittrackskipper.model.Auth;
import com.sk7software.spotifyexplicittrackskipper.util.DateUtil;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import static com.sk7software.spotifyexplicittrackskipper.AppConstants.CLIENT_ID;
import static com.sk7software.spotifyexplicittrackskipper.AppConstants.REDIRECT_URI;
import static com.sk7software.spotifyexplicittrackskipper.AppConstants.REDIRECT_URI_LITE;
import static com.sk7software.spotifyexplicittrackskipper.BuildConfig.FLAVOR;

public class AuthoriseActivity extends Activity implements View.OnClickListener  {

    private static final String SPOTIFY_AUTHORISATION_URL = "https://accounts.spotify.com/authorize/" +
            "?client_id=" + CLIENT_ID +
            "&response_type=code" +
            "&scope=" + getEncodedURL(AppConstants.SPOTIFY_SCOPES) +
            "&redirect_uri=" + getEncodedURL(REDIRECT_URI) +
            "&state=sk7" +
            "&show_dialog=true";

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 2234;
    private static final int MODE_LOGIN = 0;
    private static final int MODE_LOGOUT = 1;
    private static final String TAG = AuthoriseActivity.class.getSimpleName();

    private WebView webViewSpotify;
    private Button btnLogout;
    private Button btnLogin;
    private Button btnNext;
    private TextView txtLoggedIn;
    private TextView txtBroadcast;
    private TextView txtUserId;
    private ImageView imgUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorise);

        Log.d(TAG, "Build Flavour: " + FLAVOR);
        initialise();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (DateUtil.authExpired()) {
            toggleLoginLogout(MODE_LOGIN);
        }
    }

    private void initialise() {
        // Initialise context for preferences
        PreferencesUtil.init(getApplicationContext());

        btnLogout = (Button)findViewById(R.id.btnLogout);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnNext = (Button)findViewById(R.id.btnNext);
        txtLoggedIn = (TextView)findViewById(R.id.txtLoggedIn);
        txtBroadcast = (TextView)findViewById(R.id.txtBroadcast);
        txtUserId = (TextView)findViewById(R.id.txtUserId);
        imgUser = (ImageView)findViewById(R.id.imgUser);

        authenticate();
    }

    @Override
    public void onClick(View view) {
        if (view == null) return;
        if (view.getId() == R.id.btnLogout) {
            // Stop service
            Intent i = new Intent(getApplicationContext(), TrackBroadcastReceiver.class);
            stopService(i);
            Log.d(TAG, "Track broadcast service stopped");

            PreferencesUtil.getInstance().clearStringPreference(AppConstants.PREFERENCE_AUTH_TOKEN);
            PreferencesUtil.getInstance().clearStringPreference(AppConstants.PREFERENCE_REFRESH_TOKEN);
            PreferencesUtil.getInstance().clearStringPreference(AppConstants.PREFERENCE_AUTH_EXPIRY);
            finish();
        } else if (view.getId() == R.id.btnNext) {
            launchMainActivity();
        } else if (view.getId() == R.id.btnLogin) {
            authenticate();
        }
    }

    private void setApplicationPreferences(String response) {

        if (response.contains("<SpotifyAccess>")) {
            String accessToken = response.substring(response.indexOf("<Access>") + 8, response.indexOf("</Access>"));
            String refreshToken = response.substring(response.indexOf("<Refresh>") + 9, response.indexOf("</Refresh>"));
            String expirySeconds = response.substring(response.indexOf("<Expires>") + 9, response.indexOf("</Expires>"));
            String expiryTime = DateUtil.calcExpiryTime(Integer.parseInt(expirySeconds));
            Log.d(TAG, "Access token: " + accessToken);
            Log.d(TAG, "Refresh token: " + refreshToken);
            Log.d(TAG, "Expires in: " + expirySeconds);
            Log.d(TAG, "Expiry time: " + expiryTime);

            PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_TOKEN, accessToken);
            PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_REFRESH_TOKEN, refreshToken);
            PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_EXPIRY, expiryTime);
            updateUI();
            launchMainActivity();
        }
    }

    private void authenticate() {
        if (DateUtil.authExpired()) {
            Log.d(TAG, "Authenticating");
            if (FLAVOR.equals("lite")) {
                AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                        AuthenticationResponse.Type.TOKEN,
                        REDIRECT_URI_LITE);
                builder.setScopes(AppConstants.SPOTIFY_SCOPES.split(" "));
                builder.setShowDialog(true);
                AuthenticationRequest request = builder.build();
                AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
            } else {
                String refreshToken = PreferencesUtil.getInstance().getStringPreference(AppConstants.PREFERENCE_REFRESH_TOKEN);

                try {
                    // Attempt to refresh to auth token
                    SpotifyUtil.refreshSpotifyAuthToken(getApplicationContext(), refreshToken, new SpotifyUtil.SpotifyCallback() {
                        @Override
                        public void onRequestCompleted(Map<String, Object> callbackData) {
                            Auth a = (Auth) callbackData.get("auth");
                            String expiryTime = DateUtil.calcExpiryTime(a.getExpiresIn());
                            PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_TOKEN, a.getAccessToken());
                            PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_EXPIRY, expiryTime);
                            updateUI();
                            launchMainActivity();
                        }

                        @Override
                        public void onError(Exception e) {
                            authenticateInWebView();
                        }
                    });
                } catch (NotLoggedInException e) {
                    authenticateInWebView();
                }
            }
        } else {
            // Auth token not expired
            updateUI();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            Log.d(TAG, "Response type: " + response.getType());
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                String expiryTimeStr = DateUtil.calcExpiryTime(response.getExpiresIn());
                PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_TOKEN, response.getAccessToken());
                PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_EXPIRY, expiryTimeStr);
                PreferencesUtil.getInstance().clearStringPreference(AppConstants.PREFERENCE_REFRESH_TOKEN);
                Log.d(TAG, "Authentication expires: " + expiryTimeStr);
                updateUI();
                launchMainActivity();
            }
            else if (response.getType() == AuthenticationResponse.Type.ERROR) {
                Log.d(TAG, "Response error: " + response.getError());
                updateUI();
            } else {
                updateUI();
            }
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
        webViewSpotify.loadUrl(SPOTIFY_AUTHORISATION_URL);

    }

    private void launchMainActivity() {
        // Launch main activity
        Intent i = new Intent(AuthoriseActivity.this, MainActivity.class);
        startActivity(i);
    }

    private void toggleLoginLogout(int mode) {
        int logoutViz = (mode == MODE_LOGOUT ? View.VISIBLE : View.INVISIBLE);
        int loginViz = (mode == MODE_LOGIN ? View.VISIBLE : View.GONE);
        btnLogout.setVisibility(logoutViz);
        btnNext.setVisibility(logoutViz);
        txtLoggedIn.setVisibility(logoutViz);
        txtUserId.setVisibility(logoutViz);
        txtBroadcast.setVisibility(logoutViz);
        imgUser.setVisibility(logoutViz);
        btnLogin.setVisibility(loginViz);
    }

    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Show logged in details in text field
                SpotifyUtil.showLoginDetails(getApplicationContext(), new SpotifyUtil.SpotifyCallback() {
                    @Override
                    public void onRequestCompleted(Map<String, Object> callbackData) {
                        User u = (User)callbackData.get("user");
                        txtUserId.setText(u.getId());
                        new ImageLoadTask(u.getImages()[0].getUrl(), null, null, imgUser).execute();
                    }
                    @Override
                    public void onError(Exception e) {
                        txtUserId.setText("Error getting user info.  Log out and retry.");
                    }
                });
                toggleLoginLogout(MODE_LOGOUT);

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
