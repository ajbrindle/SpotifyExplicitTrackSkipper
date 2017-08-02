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
import com.sk7software.spotifyexplicittrackskipper.model.Track;

import java.io.ByteArrayOutputStream;
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

    private SQLiteDatabase database;

    public static synchronized DatabaseUtil getInstance(Context context) {
        if (dbInstance == null) {
            dbInstance = new DatabaseUtil(context);
            dbInstance.database = dbInstance.getSQLiteDatabase(context);
        }

        return dbInstance;
    }

    private DatabaseUtil(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "DB constructor");
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "DB onCreate()");
        initialise(db, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldv, int newv) {
        Log.d(TAG, "DB onUpgrade()");
        initialise(db, oldv, newv);
    }

    @Override
    public void close() {
        super.close();
        dbInstance = null;
        Log.d(TAG, "DB close()");
    }

    private void initialise(SQLiteDatabase db, int oldv, int newv) {
        Log.d(TAG, "DB initialise()");

        if (oldv == 0) {
            String createTable =
                    "CREATE TABLE TRACK_HISTORY (" +
                            "_ID INTEGER PRIMARY KEY," +
                            "TITLE TEXT," +
                            "ARTIST TEXT," +
                            "ALBUM TEXT," +
                            "IMAGE_URL TEXT," +
                            "SPOTIFY_ID TEXT," +
                            "PLAY_TIME INTEGER," +
                            "EXPLICIT INTEGER," +
                            "SKIPPED INTEGER" +
                            ");";
            db.execSQL(createTable);
        }
        if (oldv <= 1 && newv == 2) {
            // Add image thumbnail table
            String createTable =
                    "CREATE TABLE IMAGE_CACHE (" +
                            "SPOTIFY_ID TEXT PRIMARY KEY," +
                            "IMAGE_DATA BLOB" +
                            ");";
            db.execSQL(createTable);
        }
    }

    public void addTrack(Track track) {
        String sql = "INSERT INTO TRACK_HISTORY " +
                "(title, artist, album, spotify_id, image_url, play_time, explicit, skipped) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?);";
        SQLiteStatement statement = database.compileStatement(sql);

        String playTime = PLAY_TIME_FORMAT.format(new Date());

        int col = 1;
        statement.bindString(col++, track.getName());
        statement.bindString(col++, track.getArtistName());
        statement.bindString(col++, track.getAlbumName());
        statement.bindString(col++, track.getId());
        statement.bindString(col++, track.getAlbumArt());
        statement.bindLong(col++, track.getPlayDate().getTime());
        statement.bindLong(col++, (track.isExplicit() ? 1 : 0));
        statement.bindLong(col++, (track.isSkipped() ? 1 : 0));

        long rowId = statement.executeInsert();
    }

    public String getLatestTrackId() {
        Cursor cursor = null;
        String trackId = "";

        try {
            cursor = database.query("TRACK_HISTORY", new String[]{"SPOTIFY_ID"},
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

        try {
            cursor = database.query("TRACK_HISTORY", new String[]{"TITLE", "ARTIST", "ALBUM", "SPOTIFY_ID",
                                                            "IMAGE_URL", "PLAY_TIME", "EXPLICIT", "SKIPPED"},
                                null, null, null, null, "_ID DESC", (limit > 0 ? Integer.toString(limit) : null));
            while (cursor.moveToNext()) {
                tracks.add(new Track(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                                     cursor.getString(3), cursor.getString(4), cursor.getLong(5),
                                     cursor.getLong(6), cursor.getLong(7)));
            }
        } finally {
            cursor.close();
        }

        return tracks;
    }

    public boolean imageExists(String id) {
        Cursor cursor = null;
        boolean imageExists = false;

        try {
            cursor = database.query("IMAGE_CACHE", new String[]{"SPOTIFY_ID"},
                    "SPOTIFY_ID=?", new String[] {String.valueOf(id)}, null, null, null, null);
            if (cursor.getCount() > 0) {
                imageExists = true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            return imageExists;
        }
    }

    public void saveAlbumArt(String id, Bitmap img) {
        // Check whether image exists first (as lookups are done in background threads)
        if (imageExists(id)) { return; }

        String sql = "INSERT INTO IMAGE_CACHE " +
                "(spotify_id, image_data) " +
                "VALUES(?, ?);";
        SQLiteStatement statement = database.compileStatement(sql);

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

        try {
            cursor = database.query("IMAGE_CACHE", new String[]{"SPOTIFY_ID", "IMAGE_DATA"},
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
        String sql = "DELETE FROM IMAGE_CACHE";
        database.execSQL(sql);
    }

    public void deleteTrack(String spotifyId, Date playTime) {
        String sql = "DELETE FROM TRACK_HISTORY WHERE SPOTIFY_ID = ? AND PLAY_TIME = ?";
        SQLiteStatement statement = database.compileStatement(sql);

        statement.bindString(1, spotifyId);
        statement.bindLong(2, playTime.getTime());
        long rowId = statement.executeUpdateDelete();
    }

    public void deleteAllTracks() {
        String sql = "DELETE FROM TRACK_HISTORY";
        SQLiteStatement statement = database.compileStatement(sql);

        long rowId = statement.executeUpdateDelete();
    }

    private SQLiteDatabase getSQLiteDatabase(Context context) {
        return getInstance(context).getWritableDatabase();
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

}
