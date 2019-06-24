package com.renyu.imagelibrary.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.renyu.commonlibrary.basefrag.BaseFragment;
import com.renyu.commonlibrary.params.InitParams;
import com.renyu.imagelibrary.R;
import com.renyu.imagelibrary.commonutils.Utils;
import com.renyu.imagelibrary.params.CommonParams;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.autosize.internal.CancelAdapt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraFragment extends BaseFragment implements SurfaceHolder.Callback, Camera.PictureCallback, CancelAdapt {
    // 相机可用小功能
    public enum CameraFunction {
        // 切换镜头
        ChangeCamera,
        // 闪光灯
        Flash,
        // 相册
        PhotoPicker
    }

    public static CameraFragment getInstance(ArrayList<CameraFunction> functions) {
        CameraFragment cameraFragment = new CameraFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("cameraFunctions", functions);
        cameraFragment.setArguments(bundle);
        return cameraFragment;
    }

    public static CameraFragment getInstance() {
        ArrayList<CameraFunction> functions = new ArrayList<>();
        functions.add(CameraFunction.ChangeCamera);
        functions.add(CameraFunction.Flash);
        CameraFragment cameraFragment = new CameraFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("cameraFunctions", functions);
        cameraFragment.setArguments(bundle);
        return cameraFragment;
    }

    private static final String TAG = CameraFragment.class.getSimpleName();
    private static final String CAMERA_ID_KEY = "camera_id";
    private static final String CAMERA_FLASH_KEY = "flash_mode";

    private int mCameraID;
    private String mFlashMode;
    private Camera mCamera;
    private SquareCameraPreview mPreviewView;
    private SurfaceHolder mSurfaceHolder;
    private ProgressBar progress = null;
    private ImageView takePhotoBtn = null;
    private RelativeLayout layout_camera_func1 = null;
    private RelativeLayout layout_camera_func2 = null;

    private CameraOrientationListener mOrientationListener;

    private boolean isSurfaceDestory = false;

    // 拍摄成功之后回调
    private TakenCompleteListener takenCompleteListener = null;

    private String dirPath = "";

    public interface TakenCompleteListener {
        void getPath(String filePath);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mOrientationListener = new CameraOrientationListener(activity);
        takenCompleteListener = (TakenCompleteListener) activity;
    }

    @Override
    public void initParams() {

    }

    @Override
    public int initViews() {
        return R.layout.fragment_camera;
    }

    @Override
    public void loadData() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FileUtils.createOrExistsDir(InitParams.IMAGE_PATH);
        dirPath = InitParams.IMAGE_PATH + "/" + System.currentTimeMillis() + ".jpg";
        FileUtils.createFileByDeleteOldFile(new File(dirPath));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            mCameraID = getBackCameraID();
            mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
        } else {
            mCameraID = savedInstanceState.getInt(CAMERA_ID_KEY);
            mFlashMode = savedInstanceState.getString(CAMERA_FLASH_KEY);
        }

        mOrientationListener.enable();

        mPreviewView = view.findViewById(R.id.camera_preview_view);
        mPreviewView.getHolder().addCallback(CameraFragment.this);
        progress = view.findViewById(R.id.progress);

        layout_camera_func1 = view.findViewById(R.id.layout_camera_func1);
        layout_camera_func2 = view.findViewById(R.id.layout_camera_func2);
        try {
            ArrayList<CameraFunction> functionArrayList = ((ArrayList<CameraFunction>) getArguments().getSerializable("cameraFunctions"));
            addFunctionViews(functionArrayList.get(0), layout_camera_func1);
            addFunctionViews(functionArrayList.get(1), layout_camera_func2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        takePhotoBtn = view.findViewById(R.id.capture_image_button);
        takePhotoBtn.setOnClickListener(v -> takePicture());
    }

    /**
     * 添加小功能
     *
     * @param cameraFunction
     * @param viewGroup
     */
    private void addFunctionViews(CameraFunction cameraFunction, ViewGroup viewGroup) {
        View view = null;
        if (cameraFunction == CameraFunction.ChangeCamera) {
            view = LayoutInflater.from(context).inflate(R.layout.view_changecamera, null, false);
            initChangeCamera(view);
        } else if (cameraFunction == CameraFunction.Flash) {
            view = LayoutInflater.from(context).inflate(R.layout.view_flash, null, false);
            initFlash(view);
        } else if (cameraFunction == CameraFunction.PhotoPicker) {
            String path = Utils.getLatestPhoto(context);
            if (!TextUtils.isEmpty(path)) {
                view = LayoutInflater.from(context).inflate(R.layout.view_photopicker, null, false);
                initPhotoPicker(view, "file://" + path);
            }
        }
        if (view != null) {
            viewGroup.addView(view);
        }
    }

    private void initChangeCamera(View view) {
        final ImageView swapCameraBtn = view.findViewById(R.id.change_camera);
        PackageManager pm = context.getPackageManager();
        //同时拥有前后置摄像头才可以切换
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            swapCameraBtn.setVisibility(View.VISIBLE);
        } else {
            swapCameraBtn.setVisibility(View.GONE);
        }
        swapCameraBtn.setOnClickListener(v -> {
            if (mCameraID == CameraInfo.CAMERA_FACING_FRONT) {
                mCameraID = getBackCameraID();
            } else {
                mCameraID = getFrontCameraID();
            }
            restartPreview();
        });
    }

    private void initFlash(View view) {
        final View changeCameraFlashModeBtn = view.findViewById(R.id.flash);
        final TextView autoFlashIcon = view.findViewById(R.id.auto_flash_icon);
        changeCameraFlashModeBtn.setOnClickListener(v -> {
            if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_AUTO)) {
                mFlashMode = Camera.Parameters.FLASH_MODE_ON;
                autoFlashIcon.setText("On");
            } else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_ON)) {
                mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
                autoFlashIcon.setText("Off");
            } else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_OFF)) {
                mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
                autoFlashIcon.setText("Auto");
            }
            setupCamera();
        });
    }

    private void initPhotoPicker(View view, String path) {
        SimpleDraweeView iv_camera_photopicker = view.findViewById(R.id.iv_camera_photopicker);
        Utils.loadFresco(path, SizeUtils.dp2px(45), SizeUtils.dp2px(45), iv_camera_photopicker);
        iv_camera_photopicker.setOnClickListener((v -> {
            Utils.choicePic(CameraFragment.this, 1, CommonParams.RESULT_PHOTOPICKER);
        }));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CAMERA_ID_KEY, mCameraID);
        outState.putString(CAMERA_FLASH_KEY, mFlashMode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CommonParams.RESULT_PHOTOPICKER && resultCode == Activity.RESULT_OK) {
            ArrayList<String> temp = data.getExtras().getStringArrayList("choiceImages");
            if (temp.size() > 0) {
                takenCompleteListener.getPath(temp.get(0));
            }
        }
    }

    private boolean getCamera(int cameraID) {
        Log.d(TAG, "get camera with id " + cameraID);
        try {
            mCamera = Camera.open(cameraID);
            mPreviewView.setCamera(mCamera);
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Can't open camera with id " + cameraID);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Start the camera preview
     */
    private void startCameraPreview() {
        determineDisplayOrientation();
        setupCamera();

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Can't start camera preview due to IOException " + e);
            e.printStackTrace();
        }
    }

    /**
     * Stop the camera preview
     */
    private void stopCameraPreview() {
        // Nulls out callbacks, stops face detection
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
            }
        } catch (Exception e) {

        }
        mPreviewView.setCamera(null);
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly
     */
    private void determineDisplayOrientation() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);

        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        int displayOrientation;

        // Camera direction
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mCamera.setDisplayOrientation(displayOrientation);
    }

    /**
     * Setup the camera parameters
     */
    private void setupCamera() {
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        Size bestPreviewSize = determineBestPreviewSize(parameters);
        Size bestPictureSize = determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);

        // Set continuous picture focus, if it's supported
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(mFlashMode)) {
            parameters.setFlashMode(mFlashMode);
        } else {
            // 闪光灯条件不满足要求，则不显示该功能
            ArrayList<CameraFunction> functionArrayList = ((ArrayList<CameraFunction>) getArguments().getSerializable("cameraFunctions"));
            if (functionArrayList.get(0) == CameraFunction.Flash) {
                layout_camera_func1.removeAllViews();
            } else if (functionArrayList.get(1) == CameraFunction.Flash) {
                layout_camera_func2.removeAllViews();
            }
        }

        if (isSupportedPictureFormats(parameters.getSupportedPictureFormats(),
                ImageFormat.JPEG)) {
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(100);
        }

        // Lock in the changes
        mCamera.setParameters(parameters);
    }

    private Size determineBestPreviewSize(Camera.Parameters parameters) {
        return Utils.getCurrentScreenSize(parameters.getSupportedPreviewSizes());
    }

    private Size determineBestPictureSize(Camera.Parameters parameters) {
        return Utils.getCurrentScreenSize(parameters.getSupportedPictureSizes());
    }

    private void restartPreview() {
        stopCameraPreview();
        mCamera.release();

        if (getCamera(mCameraID))
            startCameraPreview();
    }

    private int getFrontCameraID() {
        PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return CameraInfo.CAMERA_FACING_FRONT;
        }

        return getBackCameraID();
    }

    private int getBackCameraID() {
        return CameraInfo.CAMERA_FACING_BACK;
    }

    /**
     * Take a picture
     */
    private void takePicture() {
        progress.setVisibility(View.VISIBLE);
        takePhotoBtn.setVisibility(View.GONE);

        mOrientationListener.rememberOrientation();

        // Shutter callback occurs after the image is captured. This can
        // be used to trigger a sound to let the user know that image is taken
        Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        };

        // Raw callback occurs when the raw image data is available
        Camera.PictureCallback raw = null;

        // postView callback occurs when a scaled, fully processed
        // postView image is available.
        Camera.PictureCallback postView = null;

        // jpeg callback occurs when the compressed image is available
        mCamera.takePicture(shutterCallback, raw, postView, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mOrientationListener.disable();

        // stop the preview
        stopCameraPreview();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        // 没有拍照，直接删除
        if (FileUtils.getFileLength(dirPath) == 0) {
            FileUtils.deleteFile(dirPath);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;

        if (isSurfaceDestory) {
            restartPreview();
            isSurfaceDestory = false;
        } else {
            if (getCamera(mCameraID))
                startCameraPreview();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // The surface is destroyed with the visibility of the SurfaceView is set to View.Invisible
        isSurfaceDestory = true;
    }

    /**
     * A picture has been taken
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        camera.startPreview();
        mPreviewView.onPictureTaken();

        progress.setVisibility(View.GONE);
        takePhotoBtn.setVisibility(View.VISIBLE);

        Observable.just(data).map(bytes -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            //旋转情况
            int orientation = mOrientationListener.getRememberedNormalOrientation();
            options.inJustDecodeBounds = false;
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            //照片旋转检测
            Matrix matrix = new Matrix();
            if (orientation != ExifInterface.ORIENTATION_UNDEFINED) {
                matrix.setRotate(orientation);
                if (mCameraID == CameraInfo.CAMERA_FACING_FRONT) {
                    matrix.postScale(-1, 1);
                }
            }
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            return ImageUtils.save(bmp, dirPath, Bitmap.CompressFormat.JPEG, true);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean && takenCompleteListener != null) {
                            takenCompleteListener.getPath(dirPath);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (takenCompleteListener != null) {
                            takenCompleteListener.getPath(null);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * When orientation changes, onOrientationChanged(int) of the listener will be called
     */
    private class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = getCameraPictureRotation(orientation);
            }
        }

        void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        int getRememberedNormalOrientation() {
            return mRememberedNormalOrientation;
        }
    }

    private int getCameraPictureRotation(int orientation) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(mCameraID, info);
        int rotation;

        orientation = (orientation + 45) / 90 * 90;

        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else { // back-facing camera
            rotation = (info.orientation + orientation) % 360;
        }

        return rotation;
    }

    private boolean isSupportedPictureFormats(List<Integer> supportedPictureFormats, int jpeg) {
        for (int i = 0; i < supportedPictureFormats.size(); i++) {
            if (jpeg == supportedPictureFormats.get(i)) {
                return true;
            }
        }
        return false;
    }
}
