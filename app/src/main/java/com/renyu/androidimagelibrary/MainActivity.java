package com.renyu.androidimagelibrary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.renyu.imagelibrary.preview.ImagePreviewActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        com.blankj.utilcode.util.Utils.init(this);
        Fresco.initialize(this);

        ArrayList<String> strings=new ArrayList<>();
        strings.add("http://www.ccarting.com/img/opus/photograph/h000/h41/img201008181910520.jpg");
        strings.add("http://img.sj33.cn/uploads/allimg/201302/1-130201105055.jpg");
        Intent intent=new Intent(this, ImagePreviewActivity.class);
        Bundle bundle=new Bundle();
        bundle.putBoolean("canDownload", false);
        bundle.putInt("position", 0);
        bundle.putBoolean("canEdit", false);
        bundle.putStringArrayList("urls", strings);
        bundle.putString("rightNav", "查看");
        bundle.putParcelable("rightNavClick", new RightNavClick());
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
