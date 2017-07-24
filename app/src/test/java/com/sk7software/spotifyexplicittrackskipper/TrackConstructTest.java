package com.sk7software.spotifyexplicittrackskipper;

import com.sk7software.spotifyexplicittrackskipper.music.TrackFromJSON;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    public void testTrackJSON() throws Exception {
        JSONObject testJSON = fetchJSON("mrbrightside.json");
        assertNotNull(testJSON);

        TrackFromJSON t = TrackFromJSON.createFromJSON(testJSON);
        assertEquals("Mr. Brightside", t.getName());
        assertEquals("Hot Fuss (Deluxe Version)", t.getAlbum().getName());
        assertEquals("The Killers", t.getArtists()[0].getName());
        assertEquals(3, t.getAlbum().getImages().length);
        assertEquals("https://i.scdn.co/image/d0186ad64df7d6fc5f65c20c7d16f4279ffeb815", t.getAlbum().getImages()[1].getUrl());
        assertFalse(t.isExplicit());
    }

    @Test
    public void testNowPlayingJSON() throws Exception {
        JSONObject testJSON = fetchJSON("nowplaying.json");
        assertNotNull(testJSON);

        TrackFromJSON t = TrackFromJSON.createFromJSON(testJSON);
        assertEquals("Mr. Brightside", t.getName());
        assertEquals("Hot Fuss", t.getAlbum().getName());
        assertEquals("The Killers", t.getArtists()[0].getName());
        assertEquals(3, t.getAlbum().getImages().length);
        assertEquals("https://i.scdn.co/image/8c1e066b5d1045038437d92815d49987f519e44f", t.getAlbum().getImages()[1].getUrl());
        assertFalse(t.isExplicit());
    }

}