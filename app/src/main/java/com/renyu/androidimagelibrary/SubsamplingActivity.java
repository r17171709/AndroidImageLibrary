package com.renyu.androidimagelibrary;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * Created by renyu on 2017/8/10.
 */

public class SubsamplingActivity extends AppCompatActivity {

    SubsamplingScaleImageView iv_sub;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subsampling);

        iv_sub= (SubsamplingScaleImageView) findViewById(R.id.iv_sub);
        iv_sub.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
        iv_sub.setMinScale(1080f/4000);
        iv_sub.setImage(ImageSource.asset("111.jpg"), new ImageViewState(1080f/4000, new PointF(0, 0), 0));
    }
}
