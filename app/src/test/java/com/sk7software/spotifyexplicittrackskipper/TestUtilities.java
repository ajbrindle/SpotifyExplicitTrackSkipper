package com.sk7software.spotifyexplicittrackskipper;

import android.database.Cursor;
import android.graphics.Bitmap;

import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;
import com.sk7software.spotifyexplicittrackskipper.model.Track;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Created by andre_000 on 27/07/2017.
 */

public class TestUtilities {
    public static final long TEST_TIME = 1501022845003L;

    public static int countRows(String tableName, DatabaseUtil db) {
        Cursor c = null;
        try {
            c = db.getDatabase().rawQuery("SELECT COUNT(*) FROM " + tableName, null);
            if (c != null) {
                c.moveToFirst();
                return c.getInt(0);
            }

            return -1;

        } catch (Exception e) {
            return -1;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public static void insertTracks(int numTracks, long referenceTime, DatabaseUtil db) {
        switch(numTracks) {
            case 3:
                Track t3 = new Track("Tennis Court", "Lorde", "Pure Heroine", "wxyz9876",
                        "http://www.example.com/image3", (new Date()).getTime()-20000, 1, 0);
                db.addTrack(t3, 1800);
            case 2:
                Track t2 = new Track("Wonderwall", "Oasis", "What's the Story Morning Glory", "efgh5678",
                        "http://www.example.com/image2", (new Date()).getTime()-10000, 0, 0);
                db.addTrack(t2, 1800);
            case 1:
                Track t1 = new Track("Glory Days", "Bruce Springsteen", "Born in the USA", "abcd1234",
                        "http://www.example.com/image1", referenceTime == 0 ? (new Date()).getTime() : TEST_TIME, 0, 0);
                db.addTrack(t1, 1800);
        }
    }

    public static void insertAlbumArt(DatabaseUtil db) {
        Bitmap b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        db.saveAlbumArt("abcd1234", b);
    }

    public static JSONObject fetchJSON(InputStream in) {
        try {
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
}
