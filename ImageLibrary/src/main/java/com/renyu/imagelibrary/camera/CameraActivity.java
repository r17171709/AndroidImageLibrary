package com.renyu.imagelibrary.camera;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.commonlibrary.commonutils.BarUtils;
import com.renyu.commonlibrary.permission.annotation.NeedPermission;
import com.renyu.commonlibrary.permission.annotation.PermissionDenied;
import com.renyu.imagelibrary.R;

import java.util.ArrayList;

public class CameraActivity extends BaseActivity implements CameraFragment.TakenCompleteListener {
    @Override
    public void initParams() {

    }

    @Override
    public int initViews() {
        return R.layout.activity_camera;
    }

    @Override
    public void loadData() {
        permissionApply();
    }

    @NeedPermission(permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
            deniedDesp = "为了您可以正常使用照相机，\n请点击\"设置\"-\"权限\"-打开 \"存储空间\"与\"相机\" 权限。\n最后点击两次后退按钮，即可返回。")
    public void permissionApply() {
        if (getSupportFragmentManager().getFragments().size() == 0) {
            ArrayList<CameraFragment.CameraFunction> lists = new ArrayList<>();
            lists.add(CameraFragment.CameraFunction.PhotoPicker);
            lists.add(CameraFragment.CameraFunction.ChangeCamera);
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.fragment_container, CameraFragment.getInstance(lists)).commitAllowingStateLoss();
        }
    }

    @PermissionDenied(permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
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
