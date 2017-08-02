package com.sk7software.spotifyexplicittrackskipper.model;

import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Andrew on 02/08/2017.
 */

public class User {
    private String id;
    private AlbumArt[] images;

    private static final String TAG = User.class.getSimpleName();

    public static User createFromJSON(JSONObject userInfo) throws IOException {
        User user = new User();
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        user = mapper.readValue(userInfo.toString(), User.class);

        return user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AlbumArt[] getImages() {
        return images;
    }

    public void setImages(AlbumArt[] images) {
        this.images = images;
    }
}
