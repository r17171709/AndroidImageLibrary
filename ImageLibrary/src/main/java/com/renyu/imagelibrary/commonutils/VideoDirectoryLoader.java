package com.renyu.imagelibrary.commonutils;

import android.content.Context;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;

/**
 * Created by Administrator on 2020/6/11.
 */
public class VideoDirectoryLoader extends CursorLoader {
    private final String[] VIDEO_PROJECTION = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
    };

    public VideoDirectoryLoader(@NonNull Context context) {
        super(context);

        setProjection(VIDEO_PROJECTION);
        setUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        setSortOrder(MediaStore.Video.Media.DATE_ADDED + " desc");
    }
}
