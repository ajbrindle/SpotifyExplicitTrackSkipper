package com.sk7software.spotifyexplicittrackskipper.list;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by andre_000 on 13/07/2017.
 */

public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

    private String url;
    private ImageView imageView;
    private DatabaseUtil db;
    private String id;

    public ImageLoadTask(String url, String id, DatabaseUtil db, ImageView imageView) {
        this.url = url;
        this.id = id;
        this.db = db;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            URL urlConnection = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlConnection
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap albumArt = BitmapFactory.decodeStream(input);
            return albumArt;
            //return scaleBitmap(0.75, albumArt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        imageView.setImageBitmap(result);

        if (id != null) {
            db.saveAlbumArt(id, result);
        }
    }

    private Bitmap scaleBitmap(double scaleFactor, Bitmap srcBitmap) {
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();
        int dstWidth = (int)(srcWidth*scaleFactor);
        int dstHeight = (int)(srcHeight*scaleFactor);
        return Bitmap.createScaledBitmap(srcBitmap, dstWidth, dstHeight, true);
    }
}
