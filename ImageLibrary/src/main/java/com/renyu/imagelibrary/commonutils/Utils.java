package com.renyu.imagelibrary.commonutils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
import com.blankj.utilcode.util.SizeUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.renyu.commonlibrary.params.InitParams;
import com.renyu.imagelibrary.camera.CameraActivity;
import com.renyu.imagelibrary.camera.CameraFragment;
import com.renyu.imagelibrary.camera.CameraLandscapeActivity;
import com.renyu.imagelibrary.camera.CameraLandscapeFragment;
import com.renyu.imagelibrary.crop.UCrop;
import com.renyu.imagelibrary.photopicker.PhotoPickerActivity;
import com.renyu.imagelibrary.preview.ImagePreviewActivity;
import id.zelory.compressor.Compressor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by renyu on 2017/1/3.
 */

public class Utils {
    /**
     * 选择调用系统相册
     *
     * @param activity
     * @param requestCode
     */
    public static void chooseImage(Activity activity, int requestCode) {
        //调用调用系统相册
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 拍照
     *
     * @param activity
     * @param requestCode
     */
    public static void takePicture(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, CameraActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 自定义小功能组件的拍照
     *
     * @param activity
     * @param requestCode
     */
    public static void takePicture2(Activity activity, int requestCode, ArrayList<CameraFragment.CameraFunction> lists) {
        Intent intent = new Intent(activity, CameraActivity.class);
        intent.putExtra("cameraFunctions", lists);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 拍照
     *
     * @param activity
     * @param requestCode
     */
    public static void takeLandscapePicture(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, CameraLandscapeActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 自定义小功能组件的拍照
     *
     * @param activity
     * @param requestCode
     */
    public static void takeLandscapePicture2(Activity activity, int requestCode, ArrayList<CameraLandscapeFragment.CameraFunction> lists) {
        Intent intent = new Intent(activity, CameraLandscapeActivity.class);
        intent.putExtra("cameraFunctions", lists);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * UCrop剪裁
     *
     * @param sourcePath
     * @param activity
     * @param requestCode
     * @param ratio       宽/高
     */
    public static void cropImage(String sourcePath, Activity activity, int requestCode, float ratio) {
        String destinationPath = InitParams.IMAGE_PATH + "/" + System.currentTimeMillis() + ".jpg";
        UCrop uCrop = UCrop.of(Uri.fromFile(new File(sourcePath)), Uri.fromFile(new File(destinationPath)));
        UCrop.Options options = new UCrop.Options();
        if (ratio != 0) {
            options.withAspectRatio(ratio, 1);
        } else {
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
     *
     * @param activity
     * @param maxNum
     * @param requestCode
     */
    public static void choicePic(Activity activity, int maxNum, int requestCode) {
        Intent intent = new Intent(activity, PhotoPickerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("maxNum", maxNum);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 选择图片
     *
     * @param fragment
     * @param maxNum
     * @param requestCode
     */
    public static void choicePic(Fragment fragment, int maxNum, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), PhotoPickerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("maxNum", maxNum);
        intent.putExtras(bundle);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 图片压缩
     *
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
                    .setMaxWidth(options.outWidth / 2)
                    .setMaxHeight(options.outHeight / 2)
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
     *
     * @param context
     * @param newFile
     */
    public static void refreshAlbum(Context context, String newFile) {
        if (new File(newFile).exists()) {
            try {
                MediaStore.Images.Media.insertImage(context.getContentResolver(), newFile, "", "");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取相册中最新一张图片
     *
     * @param context
     * @return
     */
    public static String getLatestPhoto(Context context) {
        // 拍摄照片的地址
        String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
        // 拍摄照片的地址ID
        String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);
        // 查询路径和修改时间
        String[] projection = {MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_MODIFIED};
        //
        String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        //
        String[] selectionArgs = {CAMERA_IMAGE_BUCKET_ID};
        // 遍历camera文件夹
        String imagePath = "";
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC");
        if (cursor.moveToFirst()) {
            imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return imagePath;
    }

    private static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    public static void loadFresco(String path, float width, float height, SimpleDraweeView simpleDraweeView) {
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(path))
                .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(width), SizeUtils.dp2px(height))).build();
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).setAutoPlayAnimations(true).build();
        simpleDraweeView.setController(draweeController);
        simpleDraweeView.setTag(path);
    }

    /**
     * 相册预览
     *
     * @param context
     * @param canDownload
     * @param position
     * @param canEdit
     * @param urls
     */
    public static void showPreview(Context context, boolean canDownload, int position, boolean canEdit, ArrayList<String> urls) {
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("canDownload", canDownload);
        bundle.putInt("position", position);
        bundle.putBoolean("canEdit", canEdit);
        bundle.putStringArrayList("urls", urls);
        intent.putExtras(bundle);
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
