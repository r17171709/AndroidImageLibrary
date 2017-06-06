package com.renyu.androidimagelibrary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.renyu.imagelibrary.commonutils.Utils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        com.blankj.utilcode.util.Utils.init(this);

        Utils.takePicture(this, 1000);
    }
}
