package com.renyu.imagelibrary.preview;

import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;

import com.blankj.utilcode.util.SizeUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.renyu.commonlibrary.basefrag.BaseFragment;
import com.renyu.imagelibrary.R;

import me.relex.photodraweeview.PhotoDraweeView;

/**
 * Created by renyu on 2017/3/8.
 */

public class ImagePreviewFragment extends BaseFragment {

    PhotoDraweeView photoDraweeView;

    OnPicChangedListener onPicChangedListener;

    public interface OnPicChangedListener {
        void picChanged(int position, ImageInfo imageInfo);
    }

    public void setOnPicChangedListener(OnPicChangedListener onPicChangedListener) {
        this.onPicChangedListener = onPicChangedListener;
    }

    public static ImagePreviewFragment newInstance(String url, int position) {
        ImagePreviewFragment fragment=new ImagePreviewFragment();
        Bundle bundle=new Bundle();
        bundle.putString("url", url);
        bundle.putInt("position", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void initParams() {
        photoDraweeView = view.findViewById(R.id.photo_view);

        String url=getArguments().getString("url");
        PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url.indexOf("http")!=-1?url:"file://"+url))
                .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(180), SizeUtils.dp2px(320))).build();
        controller.setImageRequest(request);
        controller.setOldController(photoDraweeView.getController());
        controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                if (imageInfo == null) {
                    return;
                }
                if (onPicChangedListener==null) {
                    return;
                }
                onPicChangedListener.picChanged(getArguments().getInt("position"), imageInfo);
                photoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
            }
        });
        photoDraweeView.setController(controller.build());
        photoDraweeView.setOnPhotoTapListener((view, x, y) -> {
            if (getActivity()!=null) {
                getActivity().finish();
            }
        });
        photoDraweeView.setOnLongClickListener(v -> true);
    }

    @Override
    public int initViews() {
        return R.layout.fragment_imagepreview;
    }

    @Override
    public void loadData() {

    }

    public void update(int width, int height) {
        photoDraweeView.update(width, height);
    }
}
