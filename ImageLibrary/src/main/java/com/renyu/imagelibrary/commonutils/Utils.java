package com.renyu.imagelibrary.commonutils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.renyu.commonlibrary.params.InitParams;
import com.renyu.imagelibrary.camera.CameraActivity;
import com.renyu.imagelibrary.crop.UCrop;
import com.renyu.imagelibrary.photopicker.PhotoPickerActivity;
import com.renyu.imagelibrary.preview.ImagePreviewActivity;
import com.renyu.imagelibrary.preview.SubsamplingActivity;

import java.io.File;
import java.util.ArrayList;

import id.zelory.compressor.Compressor;

/**
 * Created by renyu on 2017/1/3.
 */

public class Utils {

    /**
     * 选择调用系统相册
     * @param activity
     * @param requestCode
     */
    public static void chooseImage(Activity activity, int requestCode) {
        //调用调用系统相册
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 拍照
     * @param activity
     * @param requestCode
     */
    public static void takePicture(Activity activity, int requestCode) {
        Intent intent=new Intent(activity, CameraActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * UCrop剪裁
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
     * @param activity
     * @param maxNum
     * @param requestCode
     */
    public static void choicePic(Activity activity, int maxNum, int requestCode) {
        Intent intent=new Intent(activity, PhotoPickerActivity.class);
        Bundle bundle=new Bundle();
        bundle.putInt("maxNum", maxNum);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 图片压缩
     * @param context
     * @param filePath
     * @param destinationDirectoryPath
     * @return
     */
    public static File compressPic(Context context, String filePath, String destinationDirectoryPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        File cropFile = null;
        try {
            cropFile = new Compressor(context)
                    .setMaxWidth(options.outWidth/2)
                    .setMaxHeight(options.outHeight/2)
                    .setQuality(70)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(destinationDirectoryPath)
                    .compressToFile(new File(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cropFile;
    }

    /**
     * 拍照后刷新系统相册
     * @param context
     * @param newFile
     */
    public static void refreshAlbum(Context context, String newFile) {
        //刷新文件
        Intent intent_scan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent_scan.setData(Uri.fromFile(new File(newFile)));
        context.sendBroadcast(intent_scan);
    }

    /**
     * 相册预览
     * @param context
     * @param canDownload
     * @param position
     * @param canEdit
     * @param urls
     */
    public static void showPreview(Context context, boolean canDownload, int position, boolean canEdit, ArrayList<String> urls) {
        Intent intent=new Intent(context, ImagePreviewActivity.class);
        Bundle bundle=new Bundle();
        bundle.putBoolean("canDownload", canDownload);
        bundle.putInt("position", position);
        bundle.putBoolean("canEdit", canEdit);
        bundle.putStringArrayList("urls", urls);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    /**
     * 打开长图预览
     * @param context
     * @param url
     * @param filePath
     */
    public static void showLongPreview(Context context, String url, String filePath) {
        Intent intent=new Intent(context, SubsamplingActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("filePath", filePath);
        context.startActivity(intent);
    }

    //用于微信分享时用白色替换bitmap中的透明色
    public static Bitmap changeColor(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] colorArray = new int[w * h];
        int n = 0;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int color = getMixtureWhite(bitmap.getPixel(j, i));
                colorArray[n++] = color;
            }
        }
        return Bitmap.createBitmap(colorArray, w, h, Bitmap.Config.ARGB_8888);
    }

    //获取和白色混合颜色
    private static int getMixtureWhite(int color) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.rgb(getSingleMixtureWhite(red, alpha), getSingleMixtureWhite(green, alpha),
                getSingleMixtureWhite(blue, alpha));
    }

    // 获取单色的混合值
    private static int getSingleMixtureWhite(int color, int alpha) {
        int newColor = color * alpha / 255 + 255 - alpha;
        return newColor > 255 ? 255 : newColor;
    }
}
