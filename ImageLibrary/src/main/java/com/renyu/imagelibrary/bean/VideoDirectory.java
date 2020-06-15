package com.renyu.imagelibrary.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2020/6/11.
 */
public class VideoDirectory {
    private String bucket_id;
    private String bucket_display_name;
    private List<Video> videos = new ArrayList<>();

    public String getBucket_id() {
        return bucket_id;
    }

    public void setBucket_id(String bucket_id) {
        this.bucket_id = bucket_id;
    }

    public String getBucket_display_name() {
        return bucket_display_name;
    }

    public void setBucket_display_name(String bucket_display_name) {
        this.bucket_display_name = bucket_display_name;
    }

    public void addVideo(Video video) {
        videos.add(video);
    }

    public void addVideos(List<Video> videos) {
        this.videos.addAll(videos);
    }

    public List<Video> getVideos() {
        return videos;
    }
}
