package com.renyu.androidimagelibrary.preview;

import android.net.Uri;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.rubensousa.gravitysnaphelper.GravityPagerSnapHelper;
import com.renyu.androidimagelibrary.R;
import com.renyu.commonlibrary.baseact.BaseActivity;

import java.util.ArrayList;

public class PreviewActivity extends BaseActivity {
    // 图片路径
    ArrayList<Uri> urls;

    RecyclerView rv_preview;

    @Override
    public void initParams() {
        urls = getIntent().getExtras().getParcelableArrayList("urls");
        int choicePosition = getIntent().getExtras().getInt("position");

        rv_preview = findViewById(R.id.rv_preview);
        rv_preview.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv_preview.setItemViewCacheSize(1);
        GravityPagerSnapHelper gravityPagerSnapHelper = new GravityPagerSnapHelper(Gravity.START,
                true);
        gravityPagerSnapHelper.attachToRecyclerView(rv_preview);
        rv_preview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        rv_preview.setAdapter(new PreviewAdapter(urls));
        rv_preview.scrollToPosition(choicePosition);
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
