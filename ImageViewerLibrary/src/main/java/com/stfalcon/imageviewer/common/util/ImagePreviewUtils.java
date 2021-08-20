package com.stfalcon.imageviewer.common.util;

import android.graphics.drawable.Animatable;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import java.util.ArrayList;

import me.relex.photodraweeview.Attacher;
import me.relex.photodraweeview.PhotoDraweeView;

public class ImagePreviewUtils {
    private StfalconImageViewer<Uri> viewer = null;

    /**
     * 相册预览
     *
     * @param position
     * @param urls
     */
    public void showPreview(FragmentActivity activity, int position, ArrayList<Uri> urls) {
        showPreview(activity, null, position, urls);
    }

    public void showPreview(FragmentActivity activity, SimpleDraweeView simpleDraweeView, int position, ArrayList<Uri> urls) {
        viewer = new StfalconImageViewer.Builder<Uri>(activity, urls, new ImageLoader<Uri>() {
            @Override
            public void loadImage(PhotoDraweeView imageView, Uri image) {
                PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
                controller.setUri(image);
                controller.setOldController(imageView.getController());
                controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        if (imageInfo == null) {
                            return;
                        }
                        imageView.update(imageInfo.getWidth(), imageInfo.getHeight());
                    }
                });
                imageView.setController(controller.build());
            }

            @Override
            public void loadImagePreview(SimpleDraweeView imageView, Uri image) {
                Attacher mAttacher = new Attacher(imageView);
                PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
                controller.setUri(image);
                controller.setOldController(imageView.getController());
                controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        if (imageInfo == null) {
                            return;
                        }
                        mAttacher.update(imageInfo.getWidth(), imageInfo.getHeight());
                    }
                });
                imageView.setController(controller.build());
            }
        }).withHiddenStatusBar(false)
                .withTransitionFrom(simpleDraweeView)
                .withStartPosition(position)
                .withImageChangeListener(position1 -> viewer.updateTransitionImage(simpleDraweeView))
                .show();
    }

    public void closePreview() {
        viewer = null;
    }
}
