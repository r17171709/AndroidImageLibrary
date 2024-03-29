package com.renyu.androidimagelibrary;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.yalantis.ucrop.util.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;

public class MainActivity extends AppCompatActivity {
    private MyRxFFmpegSubscriber myRxFFmpegSubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 剪裁
//        Utils.cropImage(InitParams.IMAGE_PATH + File.separator + "1.jpg", MainActivity.this, CommonParams.RESULT_CROP, 0);

        // 相机使用
//        ArrayList<CameraFragment.CameraFunction> lists = new ArrayList<>();
//        lists.add(CameraFragment.CameraFunction.PhotoPicker);
//        lists.add(CameraFragment.CameraFunction.ChangeCamera);
//        Utils.takePhoto(this, 100, lists, true);

        // 大图浏览
//        ArrayList<Uri> uris = new ArrayList<>();
//        uris.add(Uri.parse("http://a.hiphotos.baidu.com/image/pic/item/377adab44aed2e73c3dc082b8a01a18b87d6fa84.jpg"));
//        uris.add(Uri.parse("https://mathiasbynens.be/demo/animated-webp-supported.webp"));
//        uris.add(Uri.parse("http://d.hiphotos.baidu.com/image/h%3D300/sign=b9cd963b6663f624035d3f03b745eb32/203fb80e7bec54e77d03071cb4389b504ec26ac0.jpg"));
//        uris.add(Uri.parse("https://upload.wikimedia.org/wikipedia/commons/2/2c/Rotating_earth_%28large%29.gif"));
//        uris.add(Uri.parse("http://d.hiphotos.baidu.com/image/pic/item/b64543a98226cffc254c32acb4014a90f603ea4e.jpg"));
//        uris.add(Uri.parse("http://b.hiphotos.baidu.com/image/pic/item/b8014a90f603738d30915925be1bb051f919ecda.jpg"));
//        uris.add(Uri.parse("http://f.hiphotos.baidu.com/image/pic/item/b03533fa828ba61e75af3a314c34970a304e596f.jpg"));
//        uris.add(Uri.parse("http://c.hiphotos.baidu.com/image/pic/item/ac4bd11373f08202e85f943a46fbfbedaa641be1.jpg"));
//        uris.add(Uri.parse("https://ww1.sinaimg.cn/mw690/005Fj2RDgw1f9mvl4pivvj30c82ougw3.jpg"));
//        uris.add(Uri.parse("http://a.hiphotos.baidu.com/image/pic/item/b2de9c82d158ccbfc471743b14d8bc3eb03541e6.jpg"));
//        Intent intent = new Intent(this, PreviewActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putInt("position", 0);
//        bundle.putParcelableArrayList("urls", uris);
//        bundle.putInt("rightNavImage", R.mipmap.ic_launcher);
//        bundle.putParcelable("rightNavClick", new RightNavClick());
//        intent.putExtras(bundle);
//        startActivity(intent);

        // 图片压缩
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()+"/1.jpg", options);
//        try {
//            File cropFile = new Compressor(getApplicationContext())
//                    .setMaxWidth(options.outWidth/2)
//                    .setMaxHeight(options.outHeight/2)
//                    .setQuality(80)
//                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
//                    .setDestinationDirectoryPath(InitParams.CACHE_PATH)
//                    .compressToFile(new File(Environment.getExternalStorageDirectory().getPath()+"/1.jpg"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // 视频选择
//        Utils.choiceVideo(this, 4, CommonParams.RESULT_VIDEOPICKER);

        // 视频压缩
//        runFFmpegRxJava(new File(InitParams.IMAGE_PATH + "/input.mp4"), new File(InitParams.IMAGE_PATH + "/result.mp4"));

        // 拍视频
//        Utils.takeVideo(this, CommonParams.RESULT_TAKEPHOTO, 30, false);

        // 拍照
//        Utils.takePhoto(this, CommonParams.RESULT_TAKEPHOTO, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myRxFFmpegSubscriber != null) {
            myRxFFmpegSubscriber.dispose();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CommonParams.RESULT_CROP && resultCode == RESULT_OK) {
            String filePath = data.getExtras().getString("path");
        } else if (requestCode == CommonParams.RESULT_VIDEOPICKER && resultCode == RESULT_OK) {
            ArrayList<Uri> filePaths = data.getExtras().getParcelableArrayList("choiceVideos");
            if (filePaths.size() > 0) {
                new Thread(() -> {
                    try {
                        FileInputStream inputStream = new FileInputStream(new File(FileUtils.getPath(this, filePaths.get(0))));

                        // AndroidQ以下使用
//                        ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(filePaths.get(0), "r", null);
//                        if (fileDescriptor != null) {
//                            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
//                            writeFileFromIS(new File(InitParams.IMAGE_PATH + "/demo.mp4"), inputStream);
//                            runOnUiThread(() -> {
//                                ToastUtils.showShort("复制完成");
//                            });
//                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } else if (requestCode == CommonParams.RESULT_TAKEPHOTO && resultCode == RESULT_OK) {
            String inputDir = data.getStringExtra("path");
            if (inputDir.endsWith("mp4")) {

            }
        }
    }

    private boolean writeFileFromIS(File file, InputStream is) {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            byte data[] = new byte[8192];
            int len;
            while ((len = is.read(data, 0, 8192)) != -1) {
                os.write(data, 0, len);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void runFFmpegRxJava(File srcfile, File destFile) {
//        private String command = "ffmpeg -y -i " + InitParams.IMAGE_PATH + "/input.mp4 -b 2097k -r 30 -vcodec libx264 -preset superfast " + InitParams.IMAGE_PATH + "/result.mp4";
//        private String command = "ffmpeg -y -i " + InitParams.IMAGE_PATH + "/input.mp4 -vf scale=iw/2:ih/2 -vcodec libx264 -b " + 1920 * 1080 / 2 + " " + InitParams.IMAGE_PATH + "/result.mp4";

        if (!srcfile.exists()) {
            return;
        }
        if (!destFile.exists()) {
            try {
                destFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ToastUtils.showShort("准备开始转换");

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(srcfile.getPath());
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        int targetWidth = Integer.parseInt(width) / 2;
        int targetHeight = Integer.parseInt(height) / 2;
        int targetBitrate = targetWidth * targetHeight * 2;
        String commend = "ffmpeg -y -i " + srcfile.getPath() + " -vf scale=iw/2:ih/2 -vcodec libx264 -b " + targetBitrate + " " + destFile.getPath();

        myRxFFmpegSubscriber = new MyRxFFmpegSubscriber(this);
        RxFFmpegInvoke.getInstance().runCommandRxJava(commend.split(" ")).subscribe(myRxFFmpegSubscriber);
    }

    public static class MyRxFFmpegSubscriber extends RxFFmpegSubscriber {
        private WeakReference<MainActivity> weakReference;

        public MyRxFFmpegSubscriber(MainActivity mainActivity) {
            Log.d("TAG", "onStart");
            weakReference = new WeakReference<MainActivity>(mainActivity);
        }

        @Override
        public void onFinish() {
            MainActivity mainActivity = weakReference.get();
            if (mainActivity != null) {
                Log.d("TAG", "onFinish");
            }
        }

        @Override
        public void onProgress(int progress, long progressTime) {
            MainActivity mainActivity = weakReference.get();
            if (mainActivity != null) {
                Log.d("TAG", "onProgress:  " + progress);
            }
        }

        @Override
        public void onCancel() {
            MainActivity mainActivity = weakReference.get();
            if (mainActivity != null) {
                Log.d("TAG", "onCancel");
            }
        }

        @Override
        public void onError(String message) {
            MainActivity mainActivity = weakReference.get();
            if (mainActivity != null) {
                Log.d("TAG", "onError");
            }
        }
    }
}
