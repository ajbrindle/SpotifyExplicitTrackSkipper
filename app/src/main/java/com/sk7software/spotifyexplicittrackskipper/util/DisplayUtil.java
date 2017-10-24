package com.sk7software.spotifyexplicittrackskipper.util;

import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by Andrew on 24/10/2017.
 */

public class DisplayUtil {

    public static int getDisplayWidthPix(WindowManager wm) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int w = displayMetrics.widthPixels;
        int h = displayMetrics.heightPixels;
        return (w < h ? w : h);
    }

}
