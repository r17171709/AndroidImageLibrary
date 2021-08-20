package com.stfalcon.imageviewer.loader;

import com.facebook.drawee.view.SimpleDraweeView;

import me.relex.photodraweeview.PhotoDraweeView;

public interface ImageLoader<T> {
    void loadImage(PhotoDraweeView imageView, T image);

    void loadImagePreview(SimpleDraweeView imageView, T image);
}
