package com.sk7software.spotifyexplicittrackskipper.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.sk7software.spotifyexplicittrackskipper.AppConstants;
import com.sk7software.spotifyexplicittrackskipper.music.Track;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseUtil extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "com.sk7software.spotifyexplicittrackskipper.db";
    private static final String TAG = DatabaseUtil.class.getSimpleName();
    private static final SimpleDateFormat PLAY_TIME_FORMAT = new SimpleDateFormat(AppConstants.PLAY_TIME_FORMAT);
    private static DatabaseUtil dbInstance;

    private Context context;

    public static DatabaseUtil getInstance(Context context) {
        if (dbInstance == null) {
            dbInstance = new DatabaseUtil(context);
        }

        return dbInstance;
    }

    private DatabaseUtil(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        Log.d(TAG, "DB constructor");
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "DB onCreate()");
        initialise(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldv, int newv) {
        Log.d(TAG, "DB onUpgrade()");

        if (oldv == 1 && newv == 2) {
            // Add image thumbnail table
            String createTable =
                    "CREATE TABLE IMAGE_CACHE (" +
                            "SPOTIFY_ID TEXT PRIMARY KEY," +
                            "IMAGE_DATA BLOB" +
                            ");";
            db.execSQL(createTable);
        }
    }


    private void initialise(SQLiteDatabase db) {
        Log.d(TAG, "DB initialise()");
        String createTable =
                "CREATE TABLE TRACK_HISTORY (" +
                        "_ID INTEGER PRIMARY KEY," +
                        "TITLE TEXT," +
                        "ARTIST TEXT," +
                        "ALBUM TEXT," +
                        "IMAGE_URL TEXT," +
                        "SPOTIFY_ID TEXT," +
                        "PLAY_TIME TEXT," +
                        "EXPLICIT INTEGER," +
                        "SKIPPED INTEGER" +
                        ");";
        db.execSQL(createTable);
    }

    public void addTrack(Track track) {
        String sql = "INSERT INTO TRACK_HISTORY " +
                "(title, artist, album, spotify_id, image_url, play_time, explicit, skipped) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?);";
        SQLiteDatabase db = getSQLiteDatabase();
        SQLiteStatement statement = db.compileStatement(sql);

        String playTime = PLAY_TIME_FORMAT.format(new Date());

        int col = 1;
        statement.bindString(col++, track.getTitle());
        statement.bindString(col++, track.getArtist());
        statement.bindString(col++, track.getAlbum());
        statement.bindString(col++, track.getSpotifyId());
        statement.bindString(col++, track.getImageURL());
        statement.bindString(col++, PLAY_TIME_FORMAT.format(track.getPlayTime()));
        statement.bindLong(col++, (track.isExplicit() ? 1 : 0));
        statement.bindLong(col++, (track.isSkipped() ? 1 : 0));

        long rowId = statement.executeInsert();
    }

    public String getLatestTrackId() {
        Cursor cursor = null;
        String trackId = "";
        SQLiteDatabase db = getSQLiteDatabase();

        try {
            cursor = db.query("TRACK_HISTORY", new String[]{"SPOTIFY_ID"},
                    null, null, null, null, "_ID DESC", null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                trackId = cursor.getString(0);
            }
        } finally {
            cursor.close();
        }

        return trackId;
    }

    public List<Track> getTracks(int limit) {
        Cursor cursor = null;
        List<Track> tracks = new ArrayList<>();
        SQLiteDatabase db = getSQLiteDatabase();

        try {
            cursor = db.query("TRACK_HISTORY", new String[]{"TITLE", "ARTIST", "ALBUM", "SPOTIFY_ID",
                                                            "IMAGE_URL", "PLAY_TIME", "EXPLICIT", "SKIPPED"},
                                null, null, null, null, "_ID DESC", null);
            while (cursor.moveToNext()) {
                tracks.add(new Track(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                                     cursor.getString(3), cursor.getString(4), cursor.getString(5),
                                     cursor.getLong(6), cursor.getLong(7)));
            }
        } finally {
            cursor.close();
        }

        return tracks;
    }

    public boolean imageExists(String id) {
        Cursor cursor = null;
        SQLiteDatabase db = getSQLiteDatabase();
        boolean imageExists = false;

        try {
            cursor = db.query("IMAGE_CACHE", new String[]{"SPOTIFY_ID"},
                    "SPOTIFY_ID=?", new String[] {String.valueOf(id)}, null, null, null, null);
            if (cursor.getCount() > 0) {
                imageExists = true;
            }
        } finally {
            cursor.close();
            return imageExists;
        }
    }

    public void saveAlbumArt(String id, Bitmap img) {
        // Check whether image exists first (as lookups are done in background threads)
        if (imageExists(id)) { return; }

        String sql = "INSERT INTO IMAGE_CACHE " +
                "(spotify_id, image_data) " +
                "VALUES(?, ?);";
        SQLiteDatabase db = getSQLiteDatabase();
        SQLiteStatement statement = db.compileStatement(sql);

        // Convert image to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        byte[] imgBytes = stream.toByteArray();

        int col = 1;
        statement.bindString(col++, id);
        statement.bindBlob(col++, imgBytes);

        long rowId = statement.executeInsert();
    }

    public Bitmap retrieveAlbumArt(String id) {
        Cursor cursor = null;
        byte[] imgBytes = null;
        Bitmap image = null;
        SQLiteDatabase db = getSQLiteDatabase();

        try {
            cursor = db.query("IMAGE_CACHE", new String[]{"SPOTIFY_ID", "IMAGE_DATA"},
                    "SPOTIFY_ID=?", new String[] {String.valueOf(id)}, null, null, null, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                imgBytes = cursor.getBlob(1);
                image = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
            }
        } finally {
            cursor.close();
        }

        return image;
    }

    public void clearImageCache() {
        String sql = "DELETE FROM IMAGE_CACHE;";
        SQLiteDatabase db = getSQLiteDatabase();
        db.execSQL(sql);
    }

    public void deleteTrack(String spotifyId, Date playTime) {
        String sql = "DELETE FROM TRACK_HISTORY WHERE SPOTIFY_ID = ? AND PLAY_TIME = ?";
        SQLiteDatabase db = getSQLiteDatabase();
        SQLiteStatement statement = db.compileStatement(sql);

        statement.bindString(1, spotifyId);
        statement.bindString(2, PLAY_TIME_FORMAT.format(playTime));
        long rowId = statement.executeUpdateDelete();

//        statement.clearBindings();
//        sql = "DELETE FROM IMAGE_CACHE WHERE SPOTIFY_ID = ?";
//        statement = db.compileStatement(sql);
//        statement.bindString(1, spotifyId);
//        rowId = statement.executeUpdateDelete();
    }

    private SQLiteDatabase getSQLiteDatabase() {
        return getInstance(context).getWritableDatabase();
    }

}
