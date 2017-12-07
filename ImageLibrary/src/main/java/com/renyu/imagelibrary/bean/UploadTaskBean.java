package com.renyu.imagelibrary.bean;

/**
 * Created by renyu on 2017/12/7.
 */

public class UploadTaskBean {
    // 原始文件
    String filePath;
    // 上传后的文件
    String url;
    // 上传进度
    int progress;
    // 状态 准备中，上传中、出错、完成
    UploadState statue;

    public enum UploadState {
        UPLOADPREPARE, UPLOADING, UPLOADFAIL, UPLOADSUCCESS
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public UploadState getStatue() {
        return statue;
    }

    public void setStatue(UploadState statue) {
        this.statue = statue;
    }
}
