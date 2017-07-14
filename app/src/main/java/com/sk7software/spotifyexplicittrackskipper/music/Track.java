package com.sk7software.spotifyexplicittrackskipper.music;

import android.graphics.Bitmap;

import com.sk7software.spotifyexplicittrackskipper.AppConstants;

import org.json.JSONException;
import org.json.JSONObject;

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

    public Track(String title, String artist, String album, String spotifyId, String imageURL, String playTimeStr, long explicit, long skipped) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.spotifyId = spotifyId;
        this.imageURL = imageURL;
        this.playTime = calcPlayTime(playTimeStr);
        this.explicit = (explicit == 0 ? false : true);
        this.skipped = (skipped == 0 ? false : true);
    }


    public Track(JSONObject response, boolean skipped) {
        try {
            this.title = response.getString("name");
            this.artist = response.getJSONArray("artists").getJSONObject(0).getString("name");
            this.album = response.getJSONObject("album").getString("name");
            this.spotifyId = response.getString("id");
            this.playTime = new Date();
            this.skipped = skipped;
            this.explicit = response.getBoolean("explicit");
            this.imageURL = response.getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");

        } catch (JSONException je) {
            // Do nothing
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
