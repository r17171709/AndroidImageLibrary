package com.renyu.imagelibrary.preview;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.fresco.FrescoImageLoader;
import com.github.rubensousa.gravitysnaphelper.GravityPagerSnapHelper;
import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.imagelibrary.R;

import java.util.ArrayList;

public class PreviewActivity extends BaseActivity {
    // 图片路径
    ArrayList<String> urls;

    RecyclerView rv_preview;

    @Override
    public void initParams() {
        BigImageViewer.initialize(FrescoImageLoader.with(getApplicationContext()));
        urls = getIntent().getExtras().getStringArrayList("urls");
        int choicePosition = getIntent().getExtras().getInt("position");

        rv_preview = findViewById(R.id.rv_preview);
        rv_preview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int currentPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        rv_preview.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv_preview.setItemViewCacheSize(1);
        rv_preview.setAdapter(new PreviewAdapter(urls));
        GravityPagerSnapHelper gravityPagerSnapHelper = new GravityPagerSnapHelper(Gravity.START,
                true);
        gravityPagerSnapHelper.attachToRecyclerView(rv_preview);
    }

    @Override
    public int initViews() {
        return R.layout.activity_preview;
    }

    @Override
    public void loadData() {

    }

    @Override
    public int setStatusBarColor() {
        return 0;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 1;
    }
}
