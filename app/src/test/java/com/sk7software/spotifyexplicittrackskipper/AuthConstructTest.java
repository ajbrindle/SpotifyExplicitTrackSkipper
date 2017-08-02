package com.sk7software.spotifyexplicittrackskipper;

import com.sk7software.spotifyexplicittrackskipper.model.Auth;
import com.sk7software.spotifyexplicittrackskipper.model.User;

import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Andrew on 02/08/2017.
 */

public class AuthConstructTest {
    private JSONObject fetchJSON(String filename) {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(filename);
        return TestUtilities.fetchJSON(in);
    }

    @Test
    public void testAuthJSON() throws IOException {
        JSONObject testJSON = fetchJSON("auth.json");
        assertNotNull(testJSON);

        Auth a = Auth.createFromJSON(testJSON);
        assertEquals("NgCXRKMzYjw", a.getAccessToken());
        assertEquals(3600, a.getExpiresIn());
    }
}
