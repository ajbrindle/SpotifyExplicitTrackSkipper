package com.sk7software.spotifyexplicittrackskipper;

import com.sk7software.spotifyexplicittrackskipper.model.Track;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class TrackConstructTest {

    private JSONObject fetchJSON(String filename) {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(filename);
        return TestUtilities.fetchJSON(in);
    }

    @Test
    public void testTrackJSON() throws IOException, JSONException {
        JSONObject testJSON = fetchJSON("mrbrightside.json");
        assertNotNull(testJSON);

        Track t = Track.createFromJSON(testJSON);
        assertEquals("Mr. Brightside", t.getName());
        assertEquals("Hot Fuss (Deluxe Version)", t.getAlbum().getName());
        assertEquals("The Killers", t.getArtists()[0].getName());
        assertEquals(3, t.getAlbum().getImages().length);
        assertEquals("https://i.scdn.co/image/d0186ad64df7d6fc5f65c20c7d16f4279ffeb815", t.getAlbum().getImages()[1].getUrl());
        assertFalse(t.isExplicit());
    }

    @Test
    public void testNowPlayingJSON() throws IOException, JSONException {
        JSONObject testJSON = fetchJSON("nowplaying.json");
        assertNotNull(testJSON);

        Track t = Track.createFromJSON(testJSON);
        assertEquals("Mr. Brightside", t.getName());
        assertEquals("Hot Fuss", t.getAlbum().getName());
        assertEquals("The Killers", t.getArtists()[0].getName());
        assertEquals(3, t.getAlbum().getImages().length);
        assertEquals("https://i.scdn.co/image/8c1e066b5d1045038437d92815d49987f519e44f", t.getAlbum().getImages()[1].getUrl());
        assertFalse(t.isExplicit());
    }

    @Test
    public void testToString() throws IOException, JSONException {
        JSONObject testJSON = fetchJSON("nowplaying.json");
        assertNotNull(testJSON);

        Track t = Track.createFromJSON(testJSON);
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.PLAY_TIME_FORMAT);
        try {
            t.setPlayDate(sdf.parse("2017-07-24 10:03:45"));
        } catch (Exception e) {
        }
        t.setExplicit(true);
        t.setSkipped(true);
        assertEquals("The Killers / Mr. Brightside (Hot Fuss) Played at: 2017-07-24 10:03:45 [explicit] [skipped]", t.toString());
    }

    @Test
    public void testIncompleteToString() throws IOException, JSONException {
        JSONObject testJSON = fetchJSON("nowplaying.json");
        assertNotNull(testJSON);

        Track t = Track.createFromJSON(testJSON);
        assertEquals("The Killers / Mr. Brightside (Hot Fuss) ", t.toString());
    }

    @Test
    public void testInvalidJSON() throws IOException, JSONException {
        JSONObject testJSON = fetchJSON("invalid.json");
        assertNotNull(testJSON);

        Track t = Track.createFromJSON(testJSON);
        assertNull(t.getAlbum().getName());
    }

    @Test
    public void testBestFitImages() throws Exception {
        JSONObject testJSON = fetchJSON("mrbrightside.json");
        assertNotNull(testJSON);

        Track t = Track.createFromJSON(testJSON);
        assertEquals("https://i.scdn.co/image/d0186ad64df7d6fc5f65c20c7d16f4279ffeb815", t.getAlbumArt(1000));
        assertEquals("https://i.scdn.co/image/7c3ec33d478f5f517eeb5339c2f75f150e4d601e", t.getAlbumArt(800));
        assertEquals("https://i.scdn.co/image/ac68a9e4a867ec3ce8249cd90a2d7c73755fb487", t.getAlbumArt(5000));
        assertEquals("https://i.scdn.co/image/7c3ec33d478f5f517eeb5339c2f75f150e4d601e", t.getAlbumArt(10));
    }
}