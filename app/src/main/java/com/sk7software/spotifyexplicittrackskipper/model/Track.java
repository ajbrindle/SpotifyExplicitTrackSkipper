package com.sk7software.spotifyexplicittrackskipper.model;

import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk7software.spotifyexplicittrackskipper.AppConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by andre_000 on 24/07/2017.
 */

public class Track {
    // From JSON
    private String name;
    private Artist[] artists;
    private Album album;
    private String id;
    private boolean explicit;

    // Set by application
    private boolean skipped;
    private Date playDate;

    private static final String TAG = Track.class.getSimpleName();

    public Track() {}

    public Track(String name, String artist, String album, String id, String imageURL, long playTimeMs, long explicit, long skipped) {
        this.name = name;
        this.artists = new Artist[1];
        this.artists[0] = new Artist();
        this.artists[0].setName(artist);
        this.album = new Album();
        this.album.setName(album);
        this.id = id;

        AlbumArt[] img = new AlbumArt[1];
        img[0] = new AlbumArt();
        img[0].setUrl(imageURL);
        this.album.setImages(img);

        this.playDate = new Date(playTimeMs);
        this.explicit = (explicit == 0 ? false : true);
        this.skipped = (skipped == 0 ? false : true);
    }

    public static Track createFromJSON(JSONObject response) throws IOException, JSONException {
        Track track = new Track();
        JSONObject trackInfo =
                (response.has("item")
                        ? response.getJSONObject("item")
                        : response);

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        track = mapper.readValue(trackInfo.toString(), Track.class);

        return track;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Artist[] getArtists() {
        return artists;
    }

    public void setArtists(Artist[] artists) {
        this.artists = artists;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    public Date getPlayDate() {
        return playDate;
    }

    public void setPlayDate(Date playDate) {
        this.playDate = playDate;
    }

    public String getArtistName() {
        if (artists.length > 0) {
            return artists[0].getName();
        } else {
            throw new IllegalStateException("Artist not defined");
        }
    }

    public String getAlbumName() {
        return album.getName();
    }

    public String getAlbumArt() {
        if (album.getImages().length == 1) {
            return album.getImages()[0].getUrl();
        } else if (album.getImages().length > 1) {
            return album.getImages()[1].getUrl();
        } else {
            throw new IllegalStateException("Album artwork not defined");
        }
    }

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.PLAY_TIME_FORMAT);
        return getArtists()[0].getName() + " / " + getName() + " (" + album.getName() + ") " +
                (getPlayDate() != null ? "Played at: " + sdf.format(getPlayDate()) : "") +
                (isExplicit() ? " [explicit]" : "") +
                (isSkipped() ? " [skipped]" : "");
    }
}
