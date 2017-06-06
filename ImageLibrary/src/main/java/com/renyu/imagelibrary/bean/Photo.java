package com.renyu.imagelibrary.bean;

/**
 * Created by Clevo on 2016/8/31.
 */
public class Photo {
    private int id;
    private String path;
    private boolean isSelect;

    public Photo(int id, String path) {
        this.id = id;
        this.path = path;
    }

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

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
