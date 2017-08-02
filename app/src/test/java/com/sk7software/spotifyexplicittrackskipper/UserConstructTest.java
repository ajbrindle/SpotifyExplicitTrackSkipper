package com.sk7software.spotifyexplicittrackskipper;

import com.sk7software.spotifyexplicittrackskipper.model.User;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Andrew on 02/08/2017.
 */

public class UserConstructTest {

    private JSONObject fetchJSON(String filename) {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(filename);
        return TestUtilities.fetchJSON(in);
    }

    @Test
    public void testUserJSON() throws IOException {
        JSONObject testJSON = fetchJSON("user.json");
        assertNotNull(testJSON);

        User u = User.createFromJSON(testJSON);
        assertEquals("testuser", u.getId());
    }
}
