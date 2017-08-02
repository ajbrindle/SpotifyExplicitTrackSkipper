package com.sk7software.spotifyexplicittrackskipper;

import com.sk7software.spotifyexplicittrackskipper.util.DateUtil;
import com.sk7software.spotifyexplicittrackskipper.util.PreferencesUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.text.SimpleDateFormat;
import java.util.Date;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Andrew on 29/07/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DateUtilTest {

    private static final SimpleDateFormat EXP_FORMAT = new SimpleDateFormat(AppConstants.EXPIRY_TIME_FORMAT);

    @Before
    public void setup() {
        PreferencesUtil.init(RuntimeEnvironment.application);
        PreferencesUtil.getInstance().clearAllPreferences();
    }

    @Test
    public void testNoExpiryDate() {
        assertTrue(DateUtil.authExpired());
    }

    @Test
    public void testExpired() {
        long expTime = new Date().getTime() - 10000;
        PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_EXPIRY,
                EXP_FORMAT.format(new Date(expTime)));
        assertTrue(DateUtil.authExpired());
    }

    @Test
    public void testNotExpired() {
        long expTime = new Date().getTime() + 100000;
        PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_AUTH_EXPIRY,
                EXP_FORMAT.format(new Date(expTime)));
        assertFalse(DateUtil.authExpired());
    }

}
