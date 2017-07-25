package com.sk7software.spotifyexplicittrackskipper;

import android.database.Cursor;
import android.graphics.Bitmap;

import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;
import com.sk7software.spotifyexplicittrackskipper.music.Track;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by andre_000 on 25/07/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DatabaseUtilTest {

    private DatabaseUtil db;

    private static final long TEST_TIME = 1501022845003L;

    @Before
    public void setup() {
        RuntimeEnvironment.application.deleteDatabase(DatabaseUtil.DATABASE_NAME);
        db = DatabaseUtil.getInstance(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testCreateTables() {
        assertTrue(countRows("TRACK_HISTORY") == 0);
        assertTrue(countRows("IMAGE_CACHE") == 0);
    }

    @Test
    public void testInsertTrack() {
        insertTracks(1, 0);
        assertTrue(countRows("TRACK_HISTORY") == 1);
    }

    @Test
    public void testLatestTrack() {
        insertTracks(2, 0);
        String id = db.getLatestTrackId();
        assertEquals(id, "abcd1234");
    }

    @Test
    public void testGetAllTracks() {
        insertTracks(3, 0);
        List<Track> tracks = db.getTracks(0);
        assertEquals(tracks.size(), 3);
    }

    @Test
    public void testLimitTracks() {
        insertTracks(3, 0);
        List<Track> tracks = db.getTracks(2);
        assertEquals(tracks.size(), 2);
        assertEquals(tracks.get(0).getId(), "abcd1234");
        assertEquals(tracks.get(1).getId(), "efgh5678");
    }

    @Test
    public void testInsertAlbumArt() {
        insertAlbumArt();
        assertTrue(countRows("IMAGE_CACHE") == 1);
    }

    @Test
    public void testImageExists() {
        insertAlbumArt();
        assertTrue(db.imageExists("abcd1234"));
    }

    @Test
    public void testImageDoesNotExist() {
        insertAlbumArt();
        assertFalse(db.imageExists("efgh5678"));
    }

    @Test
    public void testAlbumArt() {
        insertAlbumArt();
        Bitmap b = db.retrieveAlbumArt("abcd1234");
        assertEquals(b.getHeight(), 100);
        assertEquals(b.getWidth(), 100);
    }

    @Test
    public void testClearImageCache() {
        insertAlbumArt();
        db.clearImageCache();
        assertTrue(countRows("IMAGE_CACHE") == 0);
    }

    @Test
    public void testDeleteTrack() {
        insertTracks(3, TEST_TIME);
        db.deleteTrack("abcd1234", new Date(TEST_TIME));
        assertTrue(countRows("TRACK_HISTORY") == 2);
    }

    @Test
    public void testDeleteAllTracks() {
        insertTracks(3, 0);
        db.deleteAllTracks();
        assertTrue(countRows("TRACK_HISTORY") == 0);
    }

    private int countRows(String tableName) {
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

    private void insertTracks(int numTracks, long referenceTime) {
        switch(numTracks) {
            case 3:
                Track t3 = new Track("Tennis Court", "Lorde", "Pure Heroine", "wxyz9876",
                        "http://www.example.com/image3", (new Date()).getTime()-20000, 1, 0);
                db.addTrack(t3);
            case 2:
                Track t2 = new Track("Wonderwall", "Oasis", "What's the Story Morning Glory", "efgh5678",
                        "http://www.example.com/image2", (new Date()).getTime()-10000, 0, 0);
                db.addTrack(t2);
            case 1:
                Track t1 = new Track("Glory Days", "Bruce Springsteen", "Born in the USA", "abcd1234",
                        "http://www.example.com/image1", referenceTime == 0 ? (new Date()).getTime() : TEST_TIME, 0, 0);
                db.addTrack(t1);
        }
    }

    private void insertAlbumArt() {
        Bitmap b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        db.saveAlbumArt("abcd1234", b);
    }
}
