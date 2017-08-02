package com.sk7software.spotifyexplicittrackskipper.util;

import android.util.Log;

import com.sk7software.spotifyexplicittrackskipper.AppConstants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Andrew on 01/08/2017.
 */

public class DateUtil {
    private static final String TAG = DateUtil.class.getSimpleName();

    public static String calcExpiryTime(int expiresInSecs) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, expiresInSecs);
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.EXPIRY_TIME_FORMAT);
        String expiryTime = sdf.format(cal.getTime());
        return expiryTime;
    }

    public static boolean authExpired() {
        String expiryTimeStr = PreferencesUtil.getInstance().getStringPreference(AppConstants.PREFERENCE_AUTH_EXPIRY);

        if (expiryTimeStr.length() == 0) {
            return true;
        } else {
            DateFormat formatter = new SimpleDateFormat(AppConstants.EXPIRY_TIME_FORMAT);
            try {
                Date expiryTime = formatter.parse(expiryTimeStr);
                if (expiryTime.before(new Date())) {
                    return true;
                }
            } catch (ParseException e) {
                Log.d(TAG, "Error parsing expiry time: " + e.getMessage());
                return true;
            }
        }
        return false;
    }
}
