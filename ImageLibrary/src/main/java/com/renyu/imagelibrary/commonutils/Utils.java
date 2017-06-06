package com.renyu.imagelibrary.commonutils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.renyu.commonlibrary.params.InitParams;
import com.renyu.imagelibrary.camera.CameraActivity;
import com.renyu.imagelibrary.crop.UCrop;
import com.renyu.imagelibrary.photopicker.PhotoPickerActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by renyu on 2017/1/3.
 */

public class Utils {

    /**
     * 选择调用系统相册
     */
    public static void chooseImage(Activity activity, int requestCode) {
        //调用调用系统相册
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 照相
     */
    public static void takePicture(Activity activity, int requestCode) {
        Intent intent=new Intent(activity, CameraActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 剪裁头像
     * @param sourcePath
     * @param activity
     * @param requestCode
     * @param ratio 宽/高
     */
    public static void cropImage(String sourcePath, Activity activity, int requestCode, float ratio) {
        String destinationPath=InitParams.IMAGE_PATH+"/"+System.currentTimeMillis()+".jpg";
        UCrop uCrop = UCrop.of(Uri.fromFile(new File(sourcePath)), Uri.fromFile(new File(destinationPath)));
        UCrop.Options options = new UCrop.Options();
        if (ratio!=0) {
            options.withAspectRatio(ratio, 1);
        }
        else {
            options.setFreeStyleCropEnabled(true);
        }
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(80);
        options.setHideBottomControls(true);
        uCrop.withOptions(options);
        uCrop.start(activity, requestCode);
    }

    /**
     * 选择图片
     */
    public static void choicePic(Activity activity, int maxNum, int requestCode) {
        Intent intent=new Intent(activity, PhotoPickerActivity.class);
        Bundle bundle=new Bundle();
        bundle.putInt("maxNum", maxNum);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 剪裁图片文件
     * @param filePath
     * @param ratio 宽/高
     */
    public static void cropFile(String filePath, String newFilePath, float ratio) {
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(filePath, options);
        float bmpWidth=options.outWidth;
        float bmpHeight=options.outHeight;
        int realWidth= (int) bmpWidth;
        int realHeight= (int) bmpHeight;
        int startX=0;
        int startY=0;
        // 宽度过大
        if (bmpWidth/bmpHeight>ratio) {
            realWidth= (int) (bmpHeight*ratio);
            realHeight= (int) bmpHeight;
            startX= (int) ((bmpWidth-realWidth)/2);
            startY= 0;
        }
        // 高度过大
        else if (bmpWidth/bmpHeight<ratio) {
            realWidth= (int) bmpWidth;
            realHeight= (int) (bmpWidth/ratio);
            startX= 0;
            startY= (int) ((bmpHeight-realHeight)/2);
        }
        BitmapFactory.Options newOpts=new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = false;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap=BitmapFactory.decodeFile(filePath, newOpts);
        bitmap=Bitmap.createBitmap(bitmap, startX, startY, realWidth, realHeight);
        //生成新图片
        try {
            FileOutputStream fos = new FileOutputStream(newFilePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照后刷新系统相册
     * @param context
     * @param newFile
     */
    public static void refreshAlbum(Context context, String newFile, String dirPath) {
        //刷新文件夹
        if(android.os.Build.VERSION.SDK_INT<android.os.Build.VERSION_CODES.KITKAT) {
            Intent scan_dir=new Intent(Intent.ACTION_MEDIA_MOUNTED);
            scan_dir.setData(Uri.fromFile(new File(dirPath)));
            context.sendBroadcast(scan_dir);
        }
        //刷新文件
        Intent intent_scan=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent_scan.setData(Uri.fromFile(new File(newFile)));
        context.sendBroadcast(intent_scan);
    }
}
