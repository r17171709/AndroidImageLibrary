package com.renyu.imagelibrary.camera;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.blankj.utilcode.util.ScreenUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.imagelibrary.R;

public class CameraPreviewActivity extends BaseActivity {
    SimpleDraweeView iv_camerapreview;

    @Override
    public void initParams() {
        iv_camerapreview = findViewById(R.id.iv_camerapreview);
        findViewById(R.id.layout_camera_ensure).setOnClickListener((view) -> {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("path", getIntent().getStringExtra("path"));
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
        });
        findViewById(R.id.layout_camera_retake).setOnClickListener((view) -> {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        });
    }

    @Override
    public int initViews() {
        return R.layout.activity_camerapreview;
    }

    @Override
    public void loadData() {
        String path = getIntent().getStringExtra("path");
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file://"+path))
                .setResizeOptions(new ResizeOptions(ScreenUtils.getScreenWidth() / 2, ScreenUtils.getScreenHeight() / 2)).build();
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).setAutoPlayAnimations(true).build();
        iv_camerapreview.setController(draweeController);
        iv_camerapreview.setTag(path);
    }

    @Override
    public int setStatusBarColor() {
        return 0;
    }

    @Override
    public int setStatusBarTranslucent() {
        return Color.BLACK;
    }
}
