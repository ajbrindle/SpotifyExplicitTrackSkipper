package com.sk7software.spotifyexplicittrackskipper.model;

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

    public AlbumArt getBestFitImage(int displayWidPix) {
        if (images.length == 1) {
            return images[0];
        } else {
            int biggest = 0;
            int biggestIdx = -1;
            int smallest = 999999;
            int smallestIdx = -1;
            int idx = 0;

            for (int i=0; i<images.length; i++) {
                AlbumArt a = images[i];
                if (a.getWidth() > biggest && (displayWidPix/a.getWidth() >= 3)) {
                    biggest = a.getWidth();
                    biggestIdx = i;
                }
                if (a.getWidth() < smallest) {
                    smallest = a.getWidth();
                    smallestIdx = i;
                }
            }

            if (biggestIdx >= 0) {
                idx = biggestIdx;
            } else if (smallestIdx >= 0) {
                idx = smallestIdx;
            }

            return images[idx];
        }
    }
}
