package com.renyu.imagelibrary.preview;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.commonlibrary.network.OKHttpHelper;
import com.renyu.imagelibrary.R;
import com.renyu.imagelibrary.R2;

import java.io.File;

import butterknife.BindView;

/**
 * Created by renyu on 2017/8/10.
 */

public class SubsamplingActivity extends BaseActivity {

    @BindView(R2.id.iv_sub)
    SubsamplingScaleImageView iv_sub;

    OKHttpHelper httpHelper;

    ProgressDialog progressDialog;

    @Override
    public void initParams() {
        httpHelper=new OKHttpHelper();
        progressDialog=ProgressDialog.show(SubsamplingActivity.this, "", "正在加载文件");
    }

    @Override
    public int initViews() {
        return R.layout.activity_subsampling;
    }

    @Override
    public void loadData() {
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(getIntent().getStringExtra("url"))).setProgressiveRenderingEnabled(true).build();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);
        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(final Bitmap bitmap) {
                String s=getIntent().getStringExtra("url");
                String newFilePath=getIntent().getStringExtra("filePath")+s.substring(s.lastIndexOf("/"));
                File newFile=new File(newFilePath);
                if (newFile.exists()) {
                    newFile.delete();
                }
                ImageUtils.save(bitmap, newFile, Bitmap.CompressFormat.JPEG);
                Message message=new Message();
                message.what=1;
                message.arg1=bitmap.getWidth();
                message.obj=newFile.getPath();
                handler.sendMessage(message);
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                handler.sendEmptyMessage(-1);
            }
        }, CallerThreadExecutor.getInstance());
    }

    @Override
    public int setStatusBarColor() {
        return 0;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 1;
    }

    Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (progressDialog!=null) {
                progressDialog.dismiss();
                progressDialog=null;
            }
            if (msg.what==-1) {
                Toast.makeText(SubsamplingActivity.this, "文件加载失败", Toast.LENGTH_SHORT).show();
                finish();
            }
            else if (msg.what==1) {
                iv_sub.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
                iv_sub.setMinScale(ScreenUtils.getScreenWidth()*1.0f/msg.arg1);
                iv_sub.setImage(ImageSource.uri(Uri.parse("file://"+ msg.obj)), new ImageViewState(ScreenUtils.getScreenWidth()*1.0f/msg.arg1, new PointF(0, 0), 0));
            }
        }
    };
}
