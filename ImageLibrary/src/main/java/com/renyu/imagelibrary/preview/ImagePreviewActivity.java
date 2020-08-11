package com.renyu.imagelibrary.preview;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.facebook.imagepipeline.image.ImageInfo;
import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.imagelibrary.R;
import com.renyu.imagelibrary.preview.impl.RightNavClickImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import me.relex.circleindicator.CircleIndicator;

/**
 * Created by renyu on 16/1/31.
 */
public class ImagePreviewActivity extends BaseActivity {
    MultiTouchViewPager imagepreview_viewpager;
    ViewPagerFragmentAdapter adapter;
    CircleIndicator imagepreview_indicator;
    TextView tv_nav_title;
    TextView tv_nav_right;
    ImageButton ib_nav_right;
    ImageButton ib_nav_left;

    // 图片路径
    ArrayList<Uri> urls;
    ArrayList<Fragment> fragments;

    // 图片当前位置
    int currentPosition = 0;

    // 记录图片原始尺寸，比便于复原
    HashMap<String, ImageInfo> point;

    @Override
    public void initParams() {

    }

    @Override
    public int initViews() {
        return R.layout.activity_imagepreview;
    }

    @Override
    public void loadData() {

    }

    @Override
    public int setStatusBarColor() {
        return Color.BLACK;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 0;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        point = new HashMap<>();

        imagepreview_viewpager = findViewById(R.id.imagepreview_viewpager);
        imagepreview_indicator = findViewById(R.id.imagepreview_indicator);
        tv_nav_title = findViewById(R.id.tv_nav_title);
        tv_nav_title.setTextColor(Color.WHITE);
        ib_nav_left = findViewById(R.id.ib_nav_left);
        ib_nav_left.setImageResource(R.mipmap.ic_arrow_write_left);
        ib_nav_left.setOnClickListener(view -> onBackPressed());
        // 右侧文字点击事件
        tv_nav_right = findViewById(R.id.tv_nav_right);
        tv_nav_right.setTextColor(Color.WHITE);
        if (!TextUtils.isEmpty(getIntent().getExtras().getString("rightNavText"))) {
            tv_nav_right.setText(getIntent().getExtras().getString("rightNavText"));
            tv_nav_right.setOnClickListener(view -> {
                if (getIntent().getExtras().getParcelable("rightNavClick") != null) {
                    ((RightNavClickImpl) getIntent().getExtras().getParcelable("rightNavClick")).click(ImagePreviewActivity.this);
                }
            });
        }
        // 右侧图标点击事件
        ib_nav_right = findViewById(R.id.ib_nav_right);
        if (getIntent().getExtras().getInt("rightNavImage", -1) != -1) {
            ib_nav_right.setImageResource(getIntent().getExtras().getInt("rightNavImage", -1));
            ib_nav_right.setOnClickListener(view -> {
                if (getIntent().getExtras().getParcelable("rightNavClick") != null) {
                    ((RightNavClickImpl) getIntent().getExtras().getParcelable("rightNavClick")).click(ImagePreviewActivity.this);
                }
            });
        }

        int choicePosition = getIntent().getExtras().getInt("position");
        urls = getIntent().getExtras().getParcelableArrayList("urls");
        fragments = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            ImagePreviewFragment fragment = ImagePreviewFragment.newInstance(urls.get(i), i);
            fragment.setOnPicChangedListener((position, imageInfo) -> {
                // 添加图片尺寸信息
                point.put("" + position, imageInfo);
            });
            fragments.add(fragment);
        }
        adapter = new ViewPagerFragmentAdapter(fragments, getSupportFragmentManager());
        imagepreview_viewpager.setAdapter(adapter);
        imagepreview_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                currentPosition = position;

                Observable.timer(300, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            // 图片尺寸复原
                            if (position - 1 >= 0 && point.containsKey("" + (position - 1))) {
                                ((ImagePreviewFragment) fragments.get(position - 1)).update(point.get("" + (position - 1)).getWidth(), point.get("" + (position - 1)).getHeight());
                            }
                            if (position + 1 <= urls.size() && point.containsKey("" + (position + 1))) {
                                ((ImagePreviewFragment) fragments.get(position + 1)).update(point.get("" + (position + 1)).getWidth(), point.get("" + (position + 1)).getHeight());
                            }
                        });
                tv_nav_title.setText((position + 1) + "/" + urls.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        imagepreview_indicator.setViewPager(imagepreview_viewpager);
        imagepreview_viewpager.setCurrentItem(choicePosition);
        tv_nav_title.setText((choicePosition + 1) + "/" + urls.size());
    }

    /**
     * 获取当前选中的Position
     *
     * @return
     */
    public int getCurrentPosition() {
        return currentPosition;
    }
}
