package com.sk7software.spotifyexplicittrackskipper.music;

/**
 * Created by andre_000 on 24/07/2017.
 */

public class Album {
    private String name;
    private AlbumArt[] images;

    public Album() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlbumArt[] getImages() {
        return images;
    }

    public void setImages(AlbumArt[] images) {
        this.images = images;
    }
}
