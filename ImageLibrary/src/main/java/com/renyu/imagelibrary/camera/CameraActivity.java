package com.renyu.imagelibrary.camera;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.commonlibrary.commonutils.BarUtils;
import com.renyu.commonlibrary.permission.annotation.NeedPermission;
import com.renyu.commonlibrary.permission.annotation.PermissionDenied;
import com.renyu.imagelibrary.R;

import java.util.ArrayList;

import me.jessyan.autosize.internal.CancelAdapt;

public class CameraActivity extends BaseActivity implements CameraFragment.TakenCompleteListener, CancelAdapt {
    @Override
    public void initParams() {
        View view_nav_line = findViewById(R.id.view_nav_line);
        view_nav_line.setVisibility(View.GONE);
        ImageButton ib_nav_left = findViewById(R.id.ib_nav_left);
        ib_nav_left.setImageResource(R.mipmap.ic_arrow_write_left);
        ib_nav_left.setOnClickListener(v -> finish());
        RelativeLayout nav_layout = findViewById(R.id.nav_layout);
        nav_layout.post(() -> BarUtils.adjustStatusBar(this, ((ViewGroup) (nav_layout.getParent())), Color.TRANSPARENT));
    }

    @Override
    public int initViews() {
        return R.layout.activity_camera;
    }

    @Override
    public void loadData() {
        permissionApply();
    }

    @NeedPermission(permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
            deniedDesp = "为了您可以正常使用照相机，\n请点击\"设置\"-\"权限\"-打开 \"存储空间\"与\"相机\" 权限。\n最后点击两次后退按钮，即可返回。")
    public void permissionApply() {
        if (getSupportFragmentManager().getFragments().size() == 0) {
            CameraFragment cameraFragment = null;
            if (getIntent().getSerializableExtra("cameraFunctions") != null && getIntent().getSerializableExtra("imageVideoFunctions") != null) {
                cameraFragment = CameraFragment.getInstance((ArrayList<CameraFragment.CameraFunction>) getIntent().getSerializableExtra("cameraFunctions"),
                        (ArrayList<CameraFragment.ImageVideoFunction>) getIntent().getSerializableExtra("imageVideoFunctions"),
                        getIntent().getIntExtra("maxTime", 0));
            } else if (getIntent().getSerializableExtra("cameraFunctions") == null && getIntent().getSerializableExtra("imageVideoFunctions") == null) {
                // 全部为空，采用拍摄照片模式
                ArrayList<CameraFragment.CameraFunction> cameraFunctions = new ArrayList<>();
                cameraFunctions.add(CameraFragment.CameraFunction.ChangeCamera);
                cameraFunctions.add(CameraFragment.CameraFunction.Flash);
                ArrayList<CameraFragment.ImageVideoFunction> imageVideoFunctions = new ArrayList<>();
                imageVideoFunctions.add(CameraFragment.ImageVideoFunction.IMAGE);
                cameraFragment = CameraFragment.getInstance(cameraFunctions, imageVideoFunctions, 0);
            } else if (getIntent().getSerializableExtra("cameraFunctions") != null) {
                // 只有相机小功能场景，使用拍摄照片模式
                ArrayList<CameraFragment.ImageVideoFunction> imageVideoFunctions = new ArrayList<>();
                imageVideoFunctions.add(CameraFragment.ImageVideoFunction.IMAGE);
                cameraFragment = CameraFragment.getInstance((ArrayList<CameraFragment.CameraFunction>) getIntent().getSerializableExtra("cameraFunctions"), imageVideoFunctions, 0);
            } else if (getIntent().getSerializableExtra("imageVideoFunctions") != null) {
                ArrayList<CameraFragment.CameraFunction> cameraFunctions = new ArrayList<>();
                cameraFunctions.add(CameraFragment.CameraFunction.ChangeCamera);
                // 如果只有拍摄模式，并且不包含录制功能，则不启动闪光灯功能
                ArrayList<CameraFragment.ImageVideoFunction> imageVideoFunctions = (ArrayList<CameraFragment.ImageVideoFunction>) getIntent().getSerializableExtra("imageVideoFunctions");
                if (imageVideoFunctions.contains(CameraFragment.ImageVideoFunction.IMAGE)) {
                    cameraFunctions.add(CameraFragment.CameraFunction.Flash);
                }
                cameraFragment = CameraFragment.getInstance(cameraFunctions, imageVideoFunctions, getIntent().getIntExtra("maxTime", 0));
            }
            if (cameraFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container, cameraFragment).commitAllowingStateLoss();
            }
        }
    }

    @PermissionDenied(permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})
    public void permissionDenied() {
        finish();
    }

    @Override
    public int setStatusBarColor() {
        return 0;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BarUtils.setDark(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void getPath(String filePath) {
        //刷新相册
//        Utils.refreshAlbum(this, filePath);
        //返回上一级目录
        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putString("path", filePath);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }
}
