package com.sk7software.spotifyexplicittrackskipper;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;
import com.sk7software.spotifyexplicittrackskipper.model.Track;
import com.sk7software.spotifyexplicittrackskipper.ui.MainActivity;
import com.sk7software.spotifyexplicittrackskipper.ui.PrefsActivity;
import com.sk7software.spotifyexplicittrackskipper.ui.TrackAdapter;
import com.sk7software.spotifyexplicittrackskipper.util.PreferencesUtil;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowView;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;
import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Created by Andrew on 02/08/2017.
 */

@RunWith(CustomRobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainActivityTest {

    private DatabaseUtil db;
    private PreferencesUtil prefs;

    @Before
    public void setup() {
        RuntimeEnvironment.application.deleteDatabase(DatabaseUtil.DATABASE_NAME);
        db = DatabaseUtil.getInstance(RuntimeEnvironment.application);
        PreferencesUtil.init(RuntimeEnvironment.application.getApplicationContext());
        prefs = PreferencesUtil.getInstance();
        prefs.clearAllPreferences();
        prefs.addPreference(AppConstants.PREFERNECE_MAX_HISTORY_ITEMS, 10);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testYes() {
        assertTrue(true);
    }

    @Test
    public void testSettingsIntent() {
        MainActivity ma = Robolectric.setupActivity(MainActivity.class);
        MenuItem settings = new RoboMenuItem(R.id.action_settings); //shadowOf(ma).getOptionsMenu().findItem(R.id.action_settings);
        ma.onOptionsItemSelected(settings);
        Intent expectedIntent = new Intent(ma, PrefsActivity.class);
        assertTrue(shadowOf(ma).getNextStartedActivity().filterEquals(expectedIntent));
    }

    @Test
    public void testSkipExplicitClick() {
        assertFalse(PreferencesUtil.getInstance().getBooleanPreference(AppConstants.PREFERENCE_SKIP_EXPLICIT));
        MainActivity ma = Robolectric.setupActivity(MainActivity.class);
        ma.findViewById(R.id.swiExplicitTracks).performClick();
        assertTrue(PreferencesUtil.getInstance().getBooleanPreference(AppConstants.PREFERENCE_SKIP_EXPLICIT));
        ma.findViewById(R.id.swiExplicitTracks).performClick();
        assertFalse(PreferencesUtil.getInstance().getBooleanPreference(AppConstants.PREFERENCE_SKIP_EXPLICIT));
    }

}
