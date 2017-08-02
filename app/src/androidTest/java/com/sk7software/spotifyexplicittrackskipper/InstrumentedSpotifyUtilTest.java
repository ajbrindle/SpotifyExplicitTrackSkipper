package com.sk7software.spotifyexplicittrackskipper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;
import com.sk7software.spotifyexplicittrackskipper.model.Auth;
import com.sk7software.spotifyexplicittrackskipper.model.Track;
import com.sk7software.spotifyexplicittrackskipper.model.User;
import com.sk7software.spotifyexplicittrackskipper.util.DateUtil;
import com.sk7software.spotifyexplicittrackskipper.util.PreferencesUtil;
import com.sk7software.spotifyexplicittrackskipper.util.SpotifyUtil;

import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;



/**
 * Created by Andrew on 25/07/2017.
 */

@RunWith(AndroidJUnit4.class)
public class InstrumentedSpotifyUtilTest {

    private MockWebServer server;
    private DatabaseUtil database;

    private static final Context context = InstrumentationRegistry.getContext();

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        SpotifyUtil.SPOTIFY_REFRESH_URL = server.url("/").toString() + "?refreshToken=";
        SpotifyUtil.SPOTIFY_USER_URL = server.url("/").toString();
        PreferencesUtil.init(context);
        PreferencesUtil.getInstance().clearAllPreferences();
    }

    @After
    public void tearDown() throws Exception {
//        // Delay before shutting down server (to allow test to run asynchronously)
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {}
//
//        server.shutdown();
    }

    @Test
    public void testRefreshToken() throws IOException {
        assertTrue(DateUtil.authExpired());
        PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_REFRESH_TOKEN, "123");
        String fileName = "refresh.json";

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(getStringFromStream(InstrumentationRegistry.getContext().getResources().getAssets().open(fileName))));

        SpotifyUtil.refreshSpotifyAuthToken(context, "123", new SpotifyUtil.SpotifyCallback() {
            @Override
            public void onRequestCompleted(Map<String, Object> callbackData) {
                Auth a = (Auth)callbackData.get("auth");
                assertEquals("NgA6ZcYIixn8bUQ", a.getAccessToken());
                assertEquals(3600, a.getExpiresIn());
            }
            @Override
            public void onError(Exception e) {
                assertFalse(true);
            }
        });
    }

    @Test
    public void testRefreshTokenInvalidJSON() throws IOException {
        PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_REFRESH_TOKEN, "123");
        String fileName = "garbage.json";

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(getStringFromStream(InstrumentationRegistry.getContext().getResources().getAssets().open(fileName))));

        SpotifyUtil.refreshSpotifyAuthToken(context, "123", new SpotifyUtil.SpotifyCallback() {
            @Override
            public void onRequestCompleted(Map<String, Object> callbackData) {
                assertFalse(true);
            }
            @Override
            public void onError(Exception e) {
                assertTrue(e instanceof IOException);
            }
        });
    }

    @Test
    public void testUser() throws IOException {
        String fileName = "user.json";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(getStringFromStream(InstrumentationRegistry.getContext().getResources().getAssets().open(fileName))));

        SpotifyUtil.showLoginDetails(context, new SpotifyUtil.SpotifyCallback() {
            @Override
            public void onRequestCompleted(Map<String, Object> callbackData) {
                User u = (User)callbackData.get("user");
                assertEquals("testuser", u.getId());
            }
            @Override
            public void onError(Exception e) {
                assertFalse(true);
            }
        });
    }

    @Test
    public void testUserInvalidJSON() throws IOException {
        String fileName = "garbage.json";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(getStringFromStream(InstrumentationRegistry.getContext().getResources().getAssets().open(fileName))));

        SpotifyUtil.showLoginDetails(context, new SpotifyUtil.SpotifyCallback() {
            @Override
            public void onRequestCompleted(Map<String, Object> callbackData) {
                assertTrue(false);
            }
            @Override
            public void onError(Exception e) {
                assertTrue(e instanceof IOException);
            }
        });
    }

    @Test
    public void testTrack() throws IOException, JSONException {
        String fileName = "mrbrightside.json";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(getStringFromStream(InstrumentationRegistry.getContext().getResources().getAssets().open(fileName))));

        SpotifyUtil.fetchTrackInfo(context, server.url("/").toString(), new SpotifyUtil.SpotifyCallback() {
            @Override
            public void onRequestCompleted(Map<String, Object> callbackData) {
                Track t = (Track)callbackData.get("track");
                assertEquals("Mr. Brightside", t.getName());
                assertEquals("Hot Fuss (Deluxe Version)", t.getAlbum().getName());
                assertEquals("The Killers", t.getArtists()[0].getName());
                assertEquals(3, t.getAlbum().getImages().length);
                assertEquals("https://i.scdn.co/image/d0186ad64df7d6fc5f65c20c7d16f4279ffeb815", t.getAlbum().getImages()[1].getUrl());
                assertFalse(t.isExplicit());
            }
            @Override
            public void onError(Exception e) {
                assertFalse(true);
            }
        });
    }

    @Test
    public void testNowPlayingTrack() throws IOException, JSONException {
        String fileName = "nowplaying.json";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(getStringFromStream(InstrumentationRegistry.getContext().getResources().getAssets().open(fileName))));

        SpotifyUtil.fetchTrackInfo(context, server.url("/").toString(), new SpotifyUtil.SpotifyCallback() {
            @Override
            public void onRequestCompleted(Map<String, Object> callbackData) {
                Track t = (Track)callbackData.get("track");
                assertEquals("Mr. Brightside", t.getName());
                assertEquals("Hot Fuss", t.getAlbum().getName());
                assertEquals("The Killers", t.getArtists()[0].getName());
                assertEquals(3, t.getAlbum().getImages().length);
                assertEquals("https://i.scdn.co/image/8c1e066b5d1045038437d92815d49987f519e44f", t.getAlbum().getImages()[1].getUrl());
                assertFalse(t.isExplicit());
            }
            @Override
            public void onError(Exception e) {
                assertFalse(true);
            }
        });
    }

    @Test
    public void testTrackBadlyFormattedJSON() throws IOException, JSONException {
        String fileName = "fail.json";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(getStringFromStream(InstrumentationRegistry.getContext().getResources().getAssets().open(fileName))));

        SpotifyUtil.fetchTrackInfo(context, server.url("/").toString(), new SpotifyUtil.SpotifyCallback() {
            @Override
            public void onRequestCompleted(Map<String, Object> callbackData) {
                assertFalse(true);
            }
            @Override
            public void onError(Exception e) {
                assertTrue(e instanceof JSONException);
            }
        });
    }

    @Test
    public void testTrackInvalidJSON() throws IOException, JSONException {
        String fileName = "fail.json";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(getStringFromStream(InstrumentationRegistry.getContext().getResources().getAssets().open(fileName))));

        SpotifyUtil.fetchTrackInfo(context, server.url("/").toString(), new SpotifyUtil.SpotifyCallback() {
            @Override
            public void onRequestCompleted(Map<String, Object> callbackData) {
                assertFalse(true);
            }
            @Override
            public void onError(Exception e) {
                assertTrue(e instanceof IOException);
            }
        });
    }

    @Test
    public void testError() throws IOException, JSONException {
        String fileName = "mrbrightside.json";
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(getStringFromStream(InstrumentationRegistry.getContext().getResources().getAssets().open(fileName))));

        SpotifyUtil.fetchTrackInfo(context, server.url("/").toString(), new SpotifyUtil.SpotifyCallback() {
            @Override
            public void onRequestCompleted(Map<String, Object> callbackData) {
                assertFalse(true);
            }
            @Override
            public void onError(Exception e) {
                assertTrue(true);
            }
        });
    }

    private static String getStringFromStream(final InputStream input) {
        try {
            //Don't bother with try/catch because we are in a test case anyway.
            final InputStreamReader isr = new InputStreamReader(input);
            final BufferedReader bufferedReader = new BufferedReader(isr);

            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            bufferedReader.close();
            isr.close();

            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }
}