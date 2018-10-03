package com.renyu.androidimagelibrary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.renyu.imagelibrary.preview.PreviewActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Utils.takePicture(this, 100);

//        ArrayList<String> strings=new ArrayList<>();
//        strings.add(Environment.getExternalStorageDirectory().getPath()+"/1.jpg");
//        Intent intent=new Intent(this, ImagePreviewActivity.class);
//        Bundle bundle=new Bundle();
//        bundle.putBoolean("canDownload", false);
//        bundle.putInt("position", 0);
//        bundle.putBoolean("canEdit", true);
//        bundle.putStringArrayList("urls", strings);
//        bundle.putString("rightNav", "查看");
//        bundle.putParcelable("rightNavClick", new RightNavClick());
//        intent.putExtras(bundle);
//        startActivity(intent);

        ArrayList<String> strings=new ArrayList<>();
        strings.add(
                "http://a.hiphotos.baidu.com/image/pic/item/377adab44aed2e73c3dc082b8a01a18b87d6fa84.jpg");
        strings.add(
                "https://mathiasbynens.be/demo/animated-webp-supported.webp");
        strings.add(
                "http://d.hiphotos.baidu.com/image/h%3D300/sign=b9cd963b6663f624035d3f03b745eb32/203fb80e7bec54e77d03071cb4389b504ec26ac0.jpg");
        strings.add(
                "https://upload.wikimedia.org/wikipedia/commons/2/2c/Rotating_earth_%28large%29.gif");
        strings.add(
                "http://d.hiphotos.baidu.com/image/pic/item/b64543a98226cffc254c32acb4014a90f603ea4e.jpg");
        strings.add(
                "http://b.hiphotos.baidu.com/image/pic/item/b8014a90f603738d30915925be1bb051f919ecda.jpg");
        strings.add(
                "http://f.hiphotos.baidu.com/image/pic/item/b03533fa828ba61e75af3a314c34970a304e596f.jpg");
        strings.add(
                "http://c.hiphotos.baidu.com/image/pic/item/ac4bd11373f08202e85f943a46fbfbedaa641be1.jpg");
        strings.add(
                "https://ww1.sinaimg.cn/mw690/005Fj2RDgw1f9mvl4pivvj30c82ougw3.jpg");
        strings.add(
                "http://a.hiphotos.baidu.com/image/pic/item/b2de9c82d158ccbfc471743b14d8bc3eb03541e6.jpg");
        Intent intent=new Intent(this, PreviewActivity.class);
        Bundle bundle=new Bundle();
        bundle.putStringArrayList("urls", strings);
        bundle.putInt("position", 0);
        intent.putExtras(bundle);
        startActivity(intent);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {

        }
    }
}
