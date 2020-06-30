package com.renyu.androidimagelibrary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.iceteck.silicompressorr.SiliCompressor;
import com.renyu.commonlibrary.commonutils.RxBus;
import com.renyu.commonlibrary.params.InitParams;
import com.renyu.imagelibrary.bean.CompressBean;
import com.renyu.imagelibrary.camera.VideoRecordActivity;
import com.renyu.imagelibrary.params.CommonParams;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 剪裁
//        Utils.cropImage(Environment.getExternalStorageDirectory().getPath() + File.separator + "1.jpg", MainActivity.this, CommonParams.RESULT_CROP, 0);

        // 相机使用
//        ArrayList<CameraFragment.CameraFunction> lists = new ArrayList<>();
//        lists.add(CameraFragment.CameraFunction.PhotoPicker);
//        lists.add(CameraFragment.CameraFunction.ChangeCamera);
//        Utils.takePicture2(this, 100, lists, true);

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

        disposable = RxBus.getDefault().toObservable(CompressBean.class).observeOn(AndroidSchedulers.mainThread()).doOnNext(compressBean -> {
            Log.d("TAGTAG", compressBean.getCompressPercent() + "");
        }).subscribe();

        // 视频录制
        Intent intent = new Intent(MainActivity.this, VideoRecordActivity.class);
        startActivityForResult(intent, CommonParams.RESULT_VIDEORECORD);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CommonParams.RESULT_CROP && resultCode == RESULT_OK) {
            String filePath = data.getExtras().getString("path");
        } else if (requestCode == CommonParams.RESULT_VIDEOPICKER && resultCode == RESULT_OK) {
            ArrayList<String> filePaths = data.getExtras().getStringArrayList("choiceVideos");
        } else if (requestCode == CommonParams.RESULT_VIDEORECORD && resultCode == RESULT_OK) {
            // 视频压缩
            new Thread(() -> {
                String inputDir = data.getStringExtra("path");
                String outputDir = InitParams.IMAGE_PATH;
                if (new File(inputDir).exists()) {
                    try {
                        String filePath = SiliCompressor.with(MainActivity.this).compressVideo(inputDir, outputDir, 1280, 720, 2500000);
                        Log.d("TAGTAG", filePath);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
