package com.renyu.imagelibrary.camera;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.renyu.commonlibrary.params.InitParams;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ViedoRecordView extends SurfaceView implements MediaRecorder.OnErrorListener {
    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;

    private String mOutputDirPath = InitParams.IMAGE_PATH;

    private File mVecordFile = null;// 视频输出文件

    public ViedoRecordView(Context context) {
        this(context, null);
    }

    public ViedoRecordView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViedoRecordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(new CustomCallBack());
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private class CustomCallBack implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            freeCameraResource();
        }
    }

    /**
     * 初始化摄像头
     */
    public void openCamera() {
        try {
            if (mCamera != null) {
                freeCameraResource();
            }
            try {
                mCamera = Camera.open();
            } catch (Exception e) {
                e.printStackTrace();
                freeCameraResource();
            }
            if (mCamera == null)
                return;
            setCameraParams();
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
            mCamera.unlock();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置摄像头为竖屏
     */
    private void setCameraParams() {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size preSize = getCloselyPreSize(true, getWidth(), getHeight(), parameters.getSupportedPreviewSizes());
            parameters.setPreviewSize(preSize.width, preSize.height);
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            mCamera.setParameters(parameters);
        }
    }

    /**
     * 通过对比得到与宽高比最接近的预览尺寸（如果有相同尺寸，优先选择）
     *
     * @param isPortrait    是否竖屏
     * @param surfaceWidth  需要被进行对比的原宽
     * @param surfaceHeight 需要被进行对比的原高
     * @param preSizeList   需要对比的预览尺寸列表
     * @return 得到与原宽高比例最接近的尺寸
     */
    public static Camera.Size getCloselyPreSize(boolean isPortrait, int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {
        int reqTmpWidth;
        int reqTmpHeight;
        // 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
        if (isPortrait) {
            reqTmpWidth = surfaceHeight;
            reqTmpHeight = surfaceWidth;
        } else {
            reqTmpWidth = surfaceWidth;
            reqTmpHeight = surfaceHeight;
        }
        //先查找preview中是否存在与surfaceview相同宽高的尺寸
        for (Camera.Size size : preSizeList) {
            if ((size.width == reqTmpWidth) && (size.height == reqTmpHeight)) {
                return size;
            }
        }

        // 得到与传入的宽高比最接近的size
        float reqRatio = ((float) reqTmpWidth) / reqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : preSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }
        return retSize;
    }

    /**
     * 释放摄像头资源
     */
    private void freeCameraResource() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }

    private void createRecordDir() {
        //录制的视频保存文件夹
        File sampleDir = new File(mOutputDirPath);//录制视频的保存地址
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        //创建文件
        try {
            mVecordFile = File.createTempFile("recording", ".mp4", sampleDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initRecord() {
        mMediaRecorder = new MediaRecorder();
        try {
            mMediaRecorder.reset();
            if (mCamera != null)
                mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setOnErrorListener(this);
            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//视频源
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);//音频源
            mMediaRecorder.setOrientationHint(90);//输出旋转90度，保持坚屏录制
            CamcorderProfile cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            mMediaRecorder.setProfile(cProfile);
            mMediaRecorder.setOutputFile(mVecordFile.getAbsolutePath());
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始录制
     */
    public void startRecord() {
        new Thread(() -> {
            createRecordDir();
            initRecord();
        }).start();
    }

    /**
     * 停止录制（释放相机后重新打开相机）
     */
    public void finishRecord() {
        new Thread(() -> {
            stop();
            openCamera();
        }).start();
    }

    /**
     * 停止录制并释放相机资源
     */
    private void stop() {
        stopRecord();
        releaseRecord();
        freeCameraResource();
    }

    /**
     * 停止录制
     */
    private void stopRecord() {
        if (mMediaRecorder != null) {
            //设置后不会崩
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 释放资源
     */
    private void releaseRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            try {
                mMediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder = null;
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null)
                mr.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 录制的视频文件
     */
    public File getVecordFile() {
        return mVecordFile;
    }
}