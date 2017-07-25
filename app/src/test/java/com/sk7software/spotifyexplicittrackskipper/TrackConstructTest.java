package com.sk7software.spotifyexplicittrackskipper;

import com.sk7software.spotifyexplicittrackskipper.music.Track;

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
        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(filename);
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            return new JSONObject(responseStrBuilder.toString());
        } catch (IOException ie) {
            return null;
        } catch (JSONException je) {
            return null;
        }
    }

    @Test
    public void testTrackJSON() {
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
    public void testNowPlayingJSON() {
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
    public void testToString() {
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
    public void testIncompleteToString() {
        JSONObject testJSON = fetchJSON("nowplaying.json");
        assertNotNull(testJSON);

        Track t = Track.createFromJSON(testJSON);
        assertEquals("The Killers / Mr. Brightside (Hot Fuss) ", t.toString());
    }

    @Test
    public void testInvalidJSON() {
        JSONObject testJSON = fetchJSON("invalid.json");
        assertNotNull(testJSON);

        Track t = Track.createFromJSON(testJSON);
        assertNull(t.getAlbum().getName());
    }
}