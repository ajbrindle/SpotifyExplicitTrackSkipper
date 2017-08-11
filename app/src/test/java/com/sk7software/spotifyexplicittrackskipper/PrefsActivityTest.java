package com.sk7software.spotifyexplicittrackskipper;

import android.widget.Spinner;
import android.widget.Switch;

import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;
import com.sk7software.spotifyexplicittrackskipper.ui.PrefsActivity;
import com.sk7software.spotifyexplicittrackskipper.util.PreferencesUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.sk7software.spotifyexplicittrackskipper.TestUtilities.countRows;
import static com.sk7software.spotifyexplicittrackskipper.TestUtilities.insertAlbumArt;
import static com.sk7software.spotifyexplicittrackskipper.TestUtilities.insertTracks;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by andre_000 on 27/07/2017.
 */

@RunWith(CustomRobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class PrefsActivityTest {

    private DatabaseUtil db;
    private PreferencesUtil prefs;

    @Before
    public void setup() {
        RuntimeEnvironment.application.deleteDatabase(DatabaseUtil.DATABASE_NAME);
        db = DatabaseUtil.getInstance(RuntimeEnvironment.application);
        PreferencesUtil.init(RuntimeEnvironment.application.getApplicationContext());
        prefs = PreferencesUtil.getInstance();
        prefs.clearAllPreferences();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testClearImageCacheButton() {
        insertAlbumArt(db);
        assertEquals(countRows("IMAGE_CACHE", db), 1);

        PrefsActivity pa = Robolectric.buildActivity(PrefsActivity.class).setup().get();
        pa.findViewById(R.id.btnClear).performClick();
        assertEquals(countRows("IMAGE_CACHE", db), 0);
    }

    @Test
    public void testClearTrackHistoryButton() {
        insertTracks(2, 0, db);
        assertEquals(countRows("TRACK_HISTORY", db), 2);

        PrefsActivity pa = Robolectric.setupActivity(PrefsActivity.class);
        pa.findViewById(R.id.btnPurgeList).performClick();
        assertEquals(countRows("TRACK_HISTORY", db), 0);
    }

    @Test
    public void testKeepAliveSpinnerDisabled() {
        prefs.addPreference(AppConstants.PREFERENCE_KEEP_ALIVE, false);
        PrefsActivity pa = Robolectric.setupActivity(PrefsActivity.class);
        Switch sw = (Switch)pa.findViewById(R.id.swiKeepAlive);
        assertFalse(pa.findViewById(R.id.spiKeepAlive).isEnabled());
        assertFalse(sw.isChecked());
    }

    @Test
    public void testKeepAliveSpinnerEnabled() {
        prefs.addPreference(AppConstants.PREFERENCE_KEEP_ALIVE, true);
        PrefsActivity pa = Robolectric.setupActivity(PrefsActivity.class);
        Switch sw = (Switch)pa.findViewById(R.id.swiKeepAlive);
        Spinner s = (Spinner)pa.findViewById(R.id.spiKeepAlive);
        assertTrue(sw.isChecked());
        assertTrue(s.isEnabled());
        assertEquals(s.getSelectedItem(), "90");
    }

    @Test
    public void testKeepAliveSpinnerValue() {
        prefs.addPreference(AppConstants.PREFERENCE_KEEP_ALIVE, true);
        prefs.addPreference(AppConstants.PREFERENCE_KEEP_ALIVE_INTERVAL, 120);
        PrefsActivity pa = Robolectric.setupActivity(PrefsActivity.class);
        Spinner s = (Spinner)pa.findViewById(R.id.spiKeepAlive);
        assertTrue(s.isEnabled());
        assertEquals(s.getSelectedItem(), "120");
    }

    @Test
    public void testTrackHistoryDefaultValue() {
        PrefsActivity pa = Robolectric.setupActivity(PrefsActivity.class);
        Spinner s = (Spinner)pa.findViewById(R.id.spiHistory);
        assertEquals(s.getSelectedItem(), "75");
    }

    @Test
    public void testTrackHistoryValue() {
        prefs.addPreference(AppConstants.PREFERNECE_MAX_HISTORY_ITEMS, 200);
        PrefsActivity pa = Robolectric.setupActivity(PrefsActivity.class);
        Spinner s = (Spinner)pa.findViewById(R.id.spiHistory);
        assertEquals(s.getSelectedItem(), "200");
    }

    @Test
    public void testSwipeActionDefaultValue() {
        PrefsActivity pa = Robolectric.setupActivity(PrefsActivity.class);
        Spinner s = (Spinner)pa.findViewById(R.id.spiSwipeAction);
        assertEquals(s.getSelectedItemId(), 0);
    }

    @Test
    public void testSwipeActionValue() {
        prefs.addPreference(AppConstants.PREFERENCE_SWIPE_ACTION, 1);
        PrefsActivity pa = Robolectric.setupActivity(PrefsActivity.class);
        Spinner s = (Spinner)pa.findViewById(R.id.spiSwipeAction);
        assertEquals(s.getSelectedItemId(), 1);
    }

    @Test
    public void testDoneButton() {
        prefs.addPreference(AppConstants.PREFERENCE_KEEP_ALIVE, false);
        PrefsActivity pa = Robolectric.setupActivity(PrefsActivity.class);
        pa.findViewById(R.id.swiKeepAlive).performClick();
        assertTrue(pa.findViewById(R.id.spiKeepAlive).isEnabled());
        Spinner s1 = (Spinner)pa.findViewById(R.id.spiKeepAlive);
        Spinner s2 = (Spinner)pa.findViewById(R.id.spiHistory);
        s1.setSelection(6);
        s2.setSelection(5);
        pa.findViewById(R.id.btnDone).performClick();

        assertEquals(prefs.getIntPreference(AppConstants.PREFERENCE_KEEP_ALIVE_INTERVAL), 600);
        assertEquals(prefs.getIntPreference(AppConstants.PREFERNECE_MAX_HISTORY_ITEMS), 150);
        assertTrue(prefs.getBooleanPreference(AppConstants.PREFERENCE_KEEP_ALIVE));
    }
}
