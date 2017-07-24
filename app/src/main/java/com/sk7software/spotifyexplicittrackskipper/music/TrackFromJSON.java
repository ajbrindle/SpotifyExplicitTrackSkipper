package com.sk7software.spotifyexplicittrackskipper.music;

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

public class TrackFromJSON {
    // From JSON
    private String name;
    private Artist[] artists;
    private Album album;
    private String id;
    private boolean explicit;

    // Set by application
    private boolean skipped;
    private Date playDate;

    private static final String TAG = TrackFromJSON.class.getSimpleName();

    public TrackFromJSON() {}

    public static TrackFromJSON createFromJSON(JSONObject response) {
        TrackFromJSON track = new TrackFromJSON();
        try {
            JSONObject trackInfo =
                    (response.has("item")
                            ? response.getJSONObject("item")
                            : response);

            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                track = mapper.readValue(trackInfo.toString(), TrackFromJSON.class);
            } catch (IOException e) {
                Log.d(TAG, "Error parsing track info: " + e.getMessage());
                return null;
            }
        } catch (JSONException je) {
            Log.d(TAG, "JSONException: " + je.getMessage());
            return null;
        }

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

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.PLAY_TIME_FORMAT);
        return getArtists()[0].getName() + " / " + getName() + " (" + album.getName() + ") " +
                (getPlayDate() != null ? "Played at: " + sdf.format(getPlayDate()) : "") +
                (isExplicit() ? " [explicit]" : "") +
                (isSkipped() ? " [skipped]" : "");
    }
}
