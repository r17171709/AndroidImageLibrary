package com.renyu.imagelibrary.commonutils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.renyu.commonlibrary.params.InitParams;
import com.renyu.imagelibrary.bean.ChoiceSizeBean;
import com.renyu.imagelibrary.camera.CameraActivity;
import com.renyu.imagelibrary.camera.CameraFragment;
import com.renyu.imagelibrary.camera.CameraLandscapeActivity;
import com.renyu.imagelibrary.crop.UCrop;
import com.renyu.imagelibrary.photopicker.PhotoPickerActivity;
import com.renyu.imagelibrary.photopicker.VideoPickerActivity;
import com.renyu.imagelibrary.preview.ImagePreviewActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import id.zelory.compressor.Compressor;

import static android.provider.BaseColumns._ID;

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
    public static void takePicture(Activity activity, int requestCode, boolean needLandscape) {
        Intent intent = new Intent(activity, needLandscape ? CameraLandscapeActivity.class : CameraActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 自定义小功能组件的拍照
     *
     * @param activity
     * @param requestCode
     */
    public static void takePicture2(Activity activity, int requestCode, ArrayList<CameraFragment.CameraFunction> lists, boolean needLandscape) {
        Intent intent = new Intent(activity, needLandscape ? CameraLandscapeActivity.class : CameraActivity.class);
        intent.putExtra("cameraFunctions", lists);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void takePicture3(Activity activity, int requestCode, ArrayList<CameraFragment.ImageVideoFunction> lists, boolean needLandscape) {
        Intent intent = new Intent(activity, needLandscape ? CameraLandscapeActivity.class : CameraActivity.class);
        intent.putExtra("imageVideoFunctions", lists);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void takePicture4(Activity activity, int requestCode, ArrayList<CameraFragment.CameraFunction> lists, ArrayList<CameraFragment.ImageVideoFunction> lists2, boolean needLandscape) {
        Intent intent = new Intent(activity, needLandscape ? CameraLandscapeActivity.class : CameraActivity.class);
        intent.putExtra("cameraFunctions", lists);
        intent.putExtra("imageVideoFunctions", lists2);
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
     * 选择视频
     *
     * @param activity
     * @param maxNum
     * @param requestCode
     */
    public static void choiceVideo(Activity activity, int maxNum, int requestCode) {
        Intent intent = new Intent(activity, VideoPickerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("maxNum", maxNum);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 选择视频
     *
     * @param fragment
     * @param maxNum
     * @param requestCode
     */
    public static void choiceVideo(Fragment fragment, int maxNum, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), VideoPickerActivity.class);
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
                    .setMaxWidth(options.outWidth == -1 ? 1080 : options.outWidth / 2)
                    .setMaxHeight(options.outHeight == -1 ? 1920 : options.outHeight / 2)
                    .setQuality(70)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(destinationDirectoryPath)
                    .compressToFile(new File(filePath), System.currentTimeMillis() + ".jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cropFile;
    }

    /**
     * 插入图片后刷新系统相册
     *
     * @param path 图片路径地址
     */
    public static void refreshAlbum(String path) {
        File file = new File(path);
        ContentResolver localContentResolver = com.blankj.utilcode.util.Utils.getApp().getContentResolver();
        ContentValues localContentValues = getMediaContentValues(file, System.currentTimeMillis(), "image/jpeg");
        Uri localUri = localContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, localContentValues);
    }

    /**
     * 插入视频后刷新系统相册
     *
     * @param path 视频路径地址
     */
    public static void refreshVideo(String path) {
        if (new File(path).exists()) {
            File file = new File(path);
            ContentResolver localContentResolver = com.blankj.utilcode.util.Utils.getApp().getContentResolver();
            ContentValues localContentValues = getMediaContentValues(file, System.currentTimeMillis(), "video/3gp");
            localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);
        }
    }

    private static ContentValues getMediaContentValues(File paramFile, long paramLong, String mimeType) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(MediaStore.Video.Media.TITLE, paramFile.getName());
        localContentValues.put(MediaStore.Video.Media.DISPLAY_NAME, paramFile.getName());
        localContentValues.put(MediaStore.Video.Media.MIME_TYPE, mimeType);
        localContentValues.put(MediaStore.Video.Media.DATE_TAKEN, Long.valueOf(paramLong));
        localContentValues.put(MediaStore.Video.Media.DATE_MODIFIED, Long.valueOf(paramLong));
        localContentValues.put(MediaStore.Video.Media.DATE_ADDED, Long.valueOf(paramLong));
        localContentValues.put(MediaStore.Video.Media.DATA, paramFile.getAbsolutePath());
        localContentValues.put(MediaStore.Video.Media.SIZE, Long.valueOf(paramFile.length()));
        return localContentValues;
    }

    /**
     * 向Mediastore添加内容
     *
     * @param file
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void creatUUIDFile(File file, String mineType) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, mineType);
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        ContentResolver resolver = com.blankj.utilcode.util.Utils.getApp().getContentResolver();
        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri item = resolver.insert(collection, values);

        try (ParcelFileDescriptor pfd = resolver.openFileDescriptor(item, "w", null)) {
            BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
            ParcelFileDescriptor.AutoCloseOutputStream outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(pfd);
            BufferedOutputStream bot = new BufferedOutputStream(outputStream);
            byte[] bt = new byte[2048];
            int len;
            while ((len = bin.read(bt)) >= 0) {
                bot.write(bt, 0, len);
                bot.flush();
            }
            bin.close();
            bot.close();

            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            resolver.update(item, values, null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取相册中最新一张图片
     *
     * @param context
     * @return
     */
    public static Uri getLatestPhoto(Context context) {
        if (Build.VERSION_CODES.Q > Build.VERSION.SDK_INT) {
            // 照片文件夹路径ID
            String CAMERA_IMAGE_BUCKET_ID = getBucketId(Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera");
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
            if (TextUtils.isEmpty(imagePath)) {
                return null;
            } else {
                return Uri.parse("file://" + imagePath);
            }
        } else {
            // 查询路径和修改时间
            String[] projection = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATE_MODIFIED};
            //
            String selection = MediaStore.Images.Media.RELATIVE_PATH + " = ?";
            //
            String[] selectionArgs = {"DCIM/Camera/"};
            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC");
            Uri uri = null;
            if (cursor.moveToFirst()) {
                uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        cursor.getLong(cursor.getColumnIndex(_ID)));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return uri;
        }
    }

    private static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    public static void loadFresco(String path, float width, float height, SimpleDraweeView simpleDraweeView) {
        loadFresco(path, width, height, simpleDraweeView, null, null);
    }

    public static void loadFresco(Uri path, float width, float height, SimpleDraweeView simpleDraweeView) {
        loadFresco(path, width, height, simpleDraweeView, null, null);
    }

    public static void loadFresco(String path, float width, float height, SimpleDraweeView simpleDraweeView, GenericDraweeHierarchy hierarchy, ControllerListener<ImageInfo> listener) {
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(path))
                .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(width), SizeUtils.dp2px(height))).build();
        PipelineDraweeControllerBuilder draweeControllerBuilder = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).setAutoPlayAnimations(true);
        if (listener != null) {
            draweeControllerBuilder.setControllerListener(listener);
        }
        if (hierarchy != null) {
            simpleDraweeView.setHierarchy(hierarchy);
        }
        simpleDraweeView.setController(draweeControllerBuilder.build());
        simpleDraweeView.setTag(path);
    }

    public static void loadFresco(Uri path, float width, float height, SimpleDraweeView simpleDraweeView, GenericDraweeHierarchy hierarchy, ControllerListener<ImageInfo> listener) {
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(path)
                .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(width), SizeUtils.dp2px(height))).build();
        PipelineDraweeControllerBuilder draweeControllerBuilder = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).setAutoPlayAnimations(true);
        if (listener != null) {
            draweeControllerBuilder.setControllerListener(listener);
        }
        if (hierarchy != null) {
            simpleDraweeView.setHierarchy(hierarchy);
        }
        simpleDraweeView.setController(draweeControllerBuilder.build());
        simpleDraweeView.setTag(path.toString());
    }

    /**
     * 相册预览
     *
     * @param activity
     * @param position
     * @param urls
     */
    public static void showPreview(AppCompatActivity activity, int position, ArrayList<Uri> urls) {
        Intent intent = new Intent(activity, ImagePreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putParcelableArrayList("urls", urls);
        intent.putExtras(bundle);
        activity.startActivity(intent);
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

    /**
     * 获得最接近屏幕的尺寸
     *
     * @param sizeList
     * @return
     */
    public static Camera.Size getCurrentScreenSize(List<Camera.Size> sizeList) {
        if (sizeList != null && sizeList.size() > 0) {
            int screenHeight = ScreenUtils.getScreenHeight();
            int screenWidth = ScreenUtils.getScreenWidth();
            if (ScreenUtils.isLandscape()) {
                screenHeight = ScreenUtils.getScreenWidth();
                screenWidth = ScreenUtils.getScreenHeight();
            }
            ChoiceSizeBean[] arry = new ChoiceSizeBean[sizeList.size()];
            int temp = 0;
            for (Camera.Size size : sizeList) {
                arry[temp++] = new ChoiceSizeBean(size.height, size.width);
            }
            Arrays.sort(arry, Collections.reverseOrder());
            // 选择比例接近的尺寸
            ArrayList<ChoiceSizeBean> tmp = new ArrayList<>();
            for (int i = 0; i < arry.length; i++) {
                // 排除比例不同的
//                Log.d("CameraUtils", arry[i].getHeight() + " " + arry[i].getWidth() + " " + arry[i].getWidth() * 1.0f / arry[i].getHeight());
                // 比例差值不能太大
                if (Math.abs(arry[i].getWidth() * 1.0f / arry[i].getHeight() - 16f / 9) > 0.1) {
                    continue;
                }
                tmp.add(arry[i]);
            }
            if (tmp.size() == 0) {
                return null;
            }
            // 根据屏幕尺寸进行筛选
            ChoiceSizeBean largeLast = tmp.get(0);
            for (ChoiceSizeBean choiceSizeBean : tmp) {
                if (screenWidth <= choiceSizeBean.getHeight() && screenHeight <= choiceSizeBean.getWidth()) {
                    if (choiceSizeBean.compareTo(largeLast) <= 0) {
                        largeLast = choiceSizeBean;
                    }
                }
            }
            ChoiceSizeBean smallLast = tmp.get(tmp.size() - 1);
            for (ChoiceSizeBean choiceSizeBean : tmp) {
                if (screenWidth >= choiceSizeBean.getHeight() && screenHeight >= choiceSizeBean.getWidth()) {
                    if (choiceSizeBean.compareTo(smallLast) >= 0) {
                        smallLast = choiceSizeBean;
                    }
                }
            }
            // 最终选择
            ChoiceSizeBean last = null;
            if (largeLast != null) {
                // 判断最大的尺寸是不是超过2倍屏幕
                if (largeLast.getWidth() > 2 * screenHeight || largeLast.getHeight() >= 2 * screenWidth) {
                    largeLast = null;
                } else {
                    last = largeLast;
                }
            }
            if (largeLast == null) {
                last = smallLast;
            }

            for (Camera.Size size : sizeList) {
                if (size.width == last.getWidth() && size.height == last.getHeight()) {
                    return size;
                }
            }
        }
        return null;
    }

    public static void copyFile(Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = com.blankj.utilcode.util.Utils.getApp().getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        copyStream(input, output, new byte[1024]);
    }

    public static void copyStream(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    /**
     * 相册使用：生成视频截图
     *
     * @param path
     * @return
     */
    public static String getVideoThumb(String path, String id) {
        String imagePath = InitParams.IMAGE_PATH + File.separator + new File(path).getName() + ".jpg";
        if (new File(imagePath).exists() && new File(imagePath).length() > 0) {
            return imagePath;
        }
        Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(com.blankj.utilcode.util.Utils.getApp().getContentResolver(), Integer.parseInt(id), MediaStore.Video.Thumbnails.MINI_KIND, null);
        if (bitmap == null) {
            return "";
        }
        ImageUtils.save(bitmap, imagePath, Bitmap.CompressFormat.JPEG);
        bitmap.recycle();
        return imagePath;
    }

    /**
     * 生成视频第一帧
     *
     * @param path
     * @return
     */
    public static String getVideoThumb(String path) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            bitmap = retriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        if (bitmap == null) {
            return null;
        }
        ImageUtils.save(bitmap, InitParams.IMAGE_PATH + File.separator + new File(path).getName() + ".jpg", Bitmap.CompressFormat.JPEG);
        bitmap.recycle();
        return InitParams.IMAGE_PATH + File.separator + new File(path).getName() + ".jpg";
    }
}
