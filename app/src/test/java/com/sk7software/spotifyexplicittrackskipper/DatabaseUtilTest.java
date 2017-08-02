package com.sk7software.spotifyexplicittrackskipper;

import android.graphics.Bitmap;

import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;
import com.sk7software.spotifyexplicittrackskipper.model.Track;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.List;

import static com.sk7software.spotifyexplicittrackskipper.TestUtilities.TEST_TIME;
import static com.sk7software.spotifyexplicittrackskipper.TestUtilities.countRows;
import static com.sk7software.spotifyexplicittrackskipper.TestUtilities.insertAlbumArt;
import static com.sk7software.spotifyexplicittrackskipper.TestUtilities.insertTracks;
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
        assertTrue(countRows("TRACK_HISTORY", db) == 0);
        assertTrue(countRows("IMAGE_CACHE", db) == 0);
    }

    @Test
    public void testInsertTrack() {
        insertTracks(1, 0, db);
        assertTrue(countRows("TRACK_HISTORY", db) == 1);
    }

    @Test
    public void testLatestTrack() {
        insertTracks(2, 0, db);
        String id = db.getLatestTrackId();
        assertEquals(id, "abcd1234");
    }

    @Test
    public void testGetAllTracks() {
        insertTracks(3, 0, db);
        List<Track> tracks = db.getTracks(0);
        assertEquals(tracks.size(), 3);
    }

    @Test
    public void testLimitTracks() {
        insertTracks(3, 0, db);
        List<Track> tracks = db.getTracks(2);
        assertEquals(tracks.size(), 2);
        assertEquals(tracks.get(0).getId(), "abcd1234");
        assertEquals(tracks.get(1).getId(), "efgh5678");
    }

    @Test
    public void testInsertAlbumArt() {
        insertAlbumArt(db);
        assertTrue(countRows("IMAGE_CACHE", db) == 1);
    }

    @Test
    public void testImageExists() {
        insertAlbumArt(db);
        assertTrue(db.imageExists("abcd1234"));
    }

    @Test
    public void testImageDoesNotExist() {
        insertAlbumArt(db);
        assertFalse(db.imageExists("efgh5678"));
    }

    @Test
    public void testAlbumArt() {
        insertAlbumArt(db);
        Bitmap b = db.retrieveAlbumArt("abcd1234");
        assertEquals(b.getHeight(), 100);
        assertEquals(b.getWidth(), 100);
    }

    @Test
    public void testClearImageCache() {
        insertAlbumArt(db);
        db.clearImageCache();
        assertTrue(countRows("IMAGE_CACHE", db) == 0);
    }

    @Test
    public void testDeleteTrack() {
        insertTracks(3, TEST_TIME, db);
        db.deleteTrack("abcd1234", new Date(TEST_TIME));
        assertTrue(countRows("TRACK_HISTORY", db) == 2);
    }

    @Test
    public void testDeleteAllTracks() {
        insertTracks(3, 0, db);
        db.deleteAllTracks();
        assertTrue(countRows("TRACK_HISTORY", db) == 0);
    }

}
