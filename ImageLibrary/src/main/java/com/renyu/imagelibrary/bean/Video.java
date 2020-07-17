package com.renyu.imagelibrary.bean;

import android.net.Uri;

/**
 * Created by Administrator on 2020/6/11.
 */
public class Video {
    private int id;
    private String path;
    private String duration;
    private boolean isSelect;
    // 音频文件真实路径
    private Uri uri;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
