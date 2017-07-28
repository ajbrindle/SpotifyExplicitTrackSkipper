package com.sk7software.spotifyexplicittrackskipper.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
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

    private static final String TAG = ImageLoadTask.class.getSimpleName();

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
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.d(TAG, "Error fetching album art: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);

        if (result != null) {
            imageView.setImageBitmap(result);

            if (id != null) {
                db.saveAlbumArt(id, result);
            }
        }
    }
}
