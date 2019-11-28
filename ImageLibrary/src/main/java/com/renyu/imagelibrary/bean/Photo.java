package com.renyu.imagelibrary.bean;

import android.net.Uri;

/**
 * Created by Clevo on 2016/8/31.
 */
public class Photo {
    private int id;
    private Uri path;
    private boolean isSelect;

    public Photo(int id, Uri path) {
        this.id = id;
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Uri getPath() {
        return path;
    }

    public void setPath(Uri path) {
        this.path = path;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
