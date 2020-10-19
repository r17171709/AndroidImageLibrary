package com.renyu.androidimagelibrary;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.TimeUtils;
import com.libyuv.LibyuvUtil;
import com.renyu.commonlibrary.params.InitParams;
import com.zhaoss.weixinrecorded.util.CameraHelp;
import com.zhaoss.weixinrecorded.util.RecordUtil;

import java.io.File;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import io.microshow.rxffmpeg.RxFFmpegInvoke;

public class RecordActivity extends AppCompatActivity {
    private RecordUtil recordUtil;
    private String videoPath;
    private String audioPath;
    private CameraHelp mCameraHelp;

    private SurfaceView surfaceView;
    private SurfaceHolder mSurfaceHolder;

    private ArrayBlockingQueue<byte[]> mYUVQueue = new ArrayBlockingQueue<>(10);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        LibyuvUtil.loadLibrary();
        mCameraHelp = new CameraHelp();

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.post(() -> {
            int width = surfaceView.getWidth();
            int height = surfaceView.getHeight();
            float viewRatio = width * 1f / height;
            float videoRatio = 9f / 16f;
            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            if (viewRatio > videoRatio) {
                layoutParams.height = (int) (width / viewRatio);
            } else {
                layoutParams.width = (int) (height * viewRatio);
            }
            surfaceView.setLayoutParams(layoutParams);
        });
        mCameraHelp.setPreviewCallback((data, camera) -> {
            if (mYUVQueue.size() >= 10) {
                mYUVQueue.poll();
            }
            mYUVQueue.add(data);
        });
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceHolder = holder;
                mCameraHelp.openCamera(RecordActivity.this, Camera.CameraInfo.CAMERA_FACING_BACK, mSurfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraHelp.release();
            }
        });
        surfaceView.setOnClickListener(v -> mCameraHelp.callFocusMode());

        findViewById(R.id.start_record).setOnClickListener((v) -> {
            startRecord();
        });
        findViewById(R.id.stop_record).setOnClickListener((v) -> {
            endRecord();
        });
    }

    private void startRecord() {
        videoPath = InitParams.IMAGE_PATH + File.separator + System.currentTimeMillis() + ".h264";
        audioPath = InitParams.IMAGE_PATH + File.separator + System.currentTimeMillis() + ".pcm";
        final boolean isFrontCamera = mCameraHelp.getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT;
        final int rotation;
        if (isFrontCamera) {
            rotation = 270;
        } else {
            rotation = 90;
        }
        recordUtil = new RecordUtil(videoPath, audioPath, mCameraHelp.getWidth(), mCameraHelp.getHeight(), rotation, isFrontCamera, mYUVQueue);
        recordUtil.start();
    }

    private void endRecord() {
        if (recordUtil != null) {
            recordUtil.stop();
            recordUtil = null;
        }

        // 生成Mp4
        String commandMp4 = "ffmpeg -i " + videoPath + " -vcodec copy -f mp4 /storage/emulated/0/1/test.mp4";
        // 生成aac
        String commandAAC = "ffmpeg -f s16le -ar 44100 -ac 1 -i " + audioPath + " -acodec libfdk_aac -b:a 64000 -y /storage/emulated/0/1/test.m4a";
        // 合成文件
        String commandConvert = "ffmpeg -i /storage/emulated/0/1/test.mp4 -i /storage/emulated/0/1/test.m4a -vcodec copy -acodec copy /storage/emulated/0/1/output.mp4";

        new Thread(() -> {
            Log.d("TAG", "转换开始时间：" + TimeUtils.date2String(new Date()));
            RxFFmpegInvoke.getInstance().runFFmpegCmd(commandMp4.split(" "));
            RxFFmpegInvoke.getInstance().runFFmpegCmd(commandAAC.split(" "));
            RxFFmpegInvoke.getInstance().runFFmpegCmd(commandConvert.split(" "));
            Log.d("TAG", "转换结束时间：" + TimeUtils.date2String(new Date()));
        }).start();
    }
}
