package com.sk7software.spotifyexplicittrackskipper.music;

import android.graphics.Bitmap;
import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk7software.spotifyexplicittrackskipper.AppConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by andre_000 on 13/07/2017.
 */

public class Track {
    private String title;
    private String artist;
    private String album;
    private String spotifyId;
    private Date playTime;
    private boolean skipped;
    private boolean explicit;
    private String imageURL;

    private static final String TAG = Track.class.getSimpleName();

    public Track() {}

    public Track(String title, String artist, String album, String spotifyId, String imageURL, Date playTime, boolean explicit, boolean skipped) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.spotifyId = spotifyId;
        this.imageURL = imageURL;
        this.playTime = playTime;
        this.explicit = explicit;
        this.skipped = skipped;
    }

    public Track(String title, String artist, String album, String spotifyId, String imageURL, long playTimeMs, long explicit, long skipped) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.spotifyId = spotifyId;
        this.imageURL = imageURL;
        this.playTime = new Date(playTimeMs);
        this.explicit = (explicit == 0 ? false : true);
        this.skipped = (skipped == 0 ? false : true);
    }


    public Track(JSONObject response) {
        try {
            JSONObject trackInfo =
                    (response.has("item")
                            ? response.getJSONObject("item")
                            : response);
            this.title = trackInfo.getString("name");
            this.artist = trackInfo.getJSONArray("artists").getJSONObject(0).getString("name");
            this.album = trackInfo.getJSONObject("album").getString("name");
            this.spotifyId = trackInfo.getString("id");
            this.playTime = new Date();
            this.explicit = trackInfo.getBoolean("explicit");
            this.imageURL = trackInfo.getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");

        } catch (JSONException je) {
            Log.d(TAG, "JSONException: " + je.getMessage());
        }
    }


    private Date calcPlayTime(String playTimeStr) {
        try {
            return new SimpleDateFormat(AppConstants.PLAY_TIME_FORMAT).parse(playTimeStr);
        } catch (ParseException pe) {
            return new Date();
        }
    }

    public String getTitle() {

        return title;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public Date getPlayTime() {
        return playTime;
    }

    public void setPlayTime(Date playTime) {
        this.playTime = playTime;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public String getImageURL() { return imageURL; }

    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.PLAY_TIME_FORMAT);
        return artist + " / " + title + " (" + album + ") " +
                "Played at: " + sdf.format(playTime) +
                (isExplicit() ? " [explicit]" : "") +
                (isSkipped() ? " [skipped]" : "");
    }
}
