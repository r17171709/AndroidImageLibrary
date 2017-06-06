package com.renyu.imagelibrary.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Clevo on 2016/8/31.
 */
public class PhotoDirectory {
    private String id;
    private String coverPath;
    private String name;
    private long   dateAdded;
    private List<Photo> photos = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public void addPhoto(int id, String path) {
        photos.add(new Photo(id, path));
    }

    public void addPhoto(int index, int id, String path) {
        photos.add(index, new Photo(id, path));
    }
}
