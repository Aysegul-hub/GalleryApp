package com.example.galleryapp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Album {

    private String name;
    private final LocalDateTime creationDate;
    private final List<String> mediaPaths;

    public Album(String name) {
        this.name = name;
        this.creationDate = LocalDateTime.now();
        this.mediaPaths = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public List<String> getMediaPaths() {
        return mediaPaths;
    }

    public void addMedia(String path) {
        if (!mediaPaths.contains(path)) {
            mediaPaths.add(path);
        }
    }

    public void removeMedia(String path) {
        mediaPaths.remove(path);
    }
}