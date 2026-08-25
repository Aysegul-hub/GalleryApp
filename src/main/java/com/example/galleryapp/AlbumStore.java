package com.example.galleryapp;

import java.util.ArrayList;
import java.util.List;

public class AlbumStore {

    private static final List<Album> albums = new ArrayList<>();

    public static List<Album> getAlbums() {
        return albums;
    }

    public static void addAlbum(Album album) {
        albums.add(album);
    }

    public static void deleteAlbum(Album album) {
        albums.remove(album);
    }
}
