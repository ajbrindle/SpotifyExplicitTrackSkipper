package com.sk7software.spotifyexplicittrackskipper;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by andre_000 on 26/07/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class PreferencesUtilTest {

    private PreferencesUtil prefs;

    @Before
    public void setup() {
        Context c = RuntimeEnvironment.application;
        SharedPreferences sp = c.getSharedPreferences(AppConstants.APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        if (sp != null) {
            SharedPreferences.Editor e = sp.edit();
            e.clear();
            e.commit();
        }
        PreferencesUtil.init(c);
        prefs = PreferencesUtil.getInstance();
    }

    @Test
    public void testAddStringPreference() {
        prefs.addPreference("PREF1", "pref1Value");
        assertEquals(prefs.getStringPreference("PREF1"), "pref1Value");
    }

    @Test
    public void testAddIntPreference() {
        prefs.addPreference("PREF1", 99);
        assertEquals(prefs.getIntPreference("PREF1"), 99);
    }

    @Test
    public void testAddBooleanPreference() {
        prefs.addPreference("PREF1", true);
        assertTrue(prefs.getBooleanPreference("PREF1"));
    }

    @Test
    public void testDefaultPreferences() {
        assertEquals(prefs.getStringPreference("INVALID"), "");
        assertEquals(prefs.getIntPreference("INVALID"), 0);
        assertFalse(prefs.getBooleanPreference("INVALID"));
    }

    @Test
    public void testClearStringPreference() {
        prefs.addPreference("PREF1", "pref1Value");
        assertEquals(prefs.getStringPreference("PREF1"), "pref1Value");
        prefs.clearStringPreference("PREF1");
        assertEquals(prefs.getStringPreference("PREF1"), "");
    }

    @Test
    public void testClearAll() {
        prefs.addPreference("TRUE", true);
        prefs.addPreference("ONE", 1);
        prefs.clearAllPreferences();
        assertFalse(prefs.getBooleanPreference("TRUE"));
        assertEquals(prefs.getIntPreference("ONE"), 0);
    }

    @Test(expected = IllegalStateException.class)
    public void testNotInitialised() {
        prefs.clearAllPreferences();
        PreferencesUtil.reset();
        prefs = PreferencesUtil.getInstance();
    }

}
