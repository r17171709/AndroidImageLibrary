package com.renyu.imagelibrary.photopicker;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;
import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.commonlibrary.commonutils.BarUtils;
import com.renyu.imagelibrary.R;
import com.renyu.imagelibrary.SpaceItemDecoration;
import com.renyu.imagelibrary.bean.Video;
import com.renyu.imagelibrary.bean.VideoDirectory;
import com.renyu.imagelibrary.commonutils.Utils;
import com.renyu.imagelibrary.commonutils.VideoDirectoryLoader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2020/6/11.
 */
public class VideoPickerActivity extends BaseActivity {
    private TextView tv_nav_right;
    private TextView photopicker_dict;
    private RecyclerView photopicker_rv = null;
    private VideoPickerAdapter videoPickerAdapter;
    private ListPopupWindow popupWindow;
    private DictVideoAdapter dictAdapter;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int corePoolSize = Math.max(2, Math.min(CPU_COUNT - 1, 4));

    private ExecutorService executorService;
    // 已经添加进入线程池
    private ArrayList<String> servicesList;

    //全部文件
    private LinkedHashMap<String, VideoDirectory> allHashMap;
    private ArrayList<Video> models;
    //列表加载文件夹
    private ArrayList<VideoDirectory> dictModels;
    private ArrayList<String> bucketIds;

    Disposable disposable = null;
    ObservableEmitter<LinkedHashMap<String, VideoDirectory>> observableEmitter;

    //最大可选视频数量
    int maxNum = 0;
    //选中的视频
    public ArrayList<String> videoPaths;
    //最大显示文件夹数量
    private int COUNT_MAX = 4;
    //当前文件夹key
    private String currentKey = "0";

    @Override
    public void initParams() {
        allHashMap = new LinkedHashMap<>();
        models = new ArrayList<>();
        dictModels = new ArrayList<>();
        bucketIds = new ArrayList<>();
        videoPaths = new ArrayList<>();

        maxNum = getIntent().getExtras().getInt("maxNum");

        executorService = Executors.newFixedThreadPool(corePoolSize);
        servicesList = new ArrayList<>();

        ImageView ib_nav_left = findViewById(R.id.ib_nav_left);
        ib_nav_left.setImageResource(R.mipmap.icon_back_black);
        ib_nav_left.setOnClickListener(v -> finish());
        TextView tv_nav_title = findViewById(R.id.tv_nav_title);
        tv_nav_title.setTextColor(Color.parseColor("#333333"));
        tv_nav_title.setText("视频");
        tv_nav_right = findViewById(R.id.tv_nav_right);
        tv_nav_right.setText("完成");
        tv_nav_right.setTextColor(Color.parseColor("#999999"));
        tv_nav_right.setEnabled(false);
        tv_nav_right.setOnClickListener(v -> {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("choiceVideos", videoPaths);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
        });
        photopicker_dict = findViewById(R.id.photopicker_dict);
        photopicker_dict.setOnClickListener(v -> {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            } else if (!isFinishing()) {
                adjustHeight();
                popupWindow.show();
            }
        });
        photopicker_rv = findViewById(R.id.photopicker_rv);
        photopicker_rv.setHasFixedSize(true);
        photopicker_rv.setLayoutManager(new GridLayoutManager(this, 3));
        photopicker_rv.addItemDecoration(new SpaceItemDecoration(1, 3));
        videoPickerAdapter = new VideoPickerAdapter(models, new VideoPickerAdapter.OperVideoListener() {
            @Override
            public void add(String path) {
                if (!videoPaths.contains(path)) {
                    videoPaths.add(path);

                    tv_nav_right.setText("完成(" + videoPaths.size() + "/" + maxNum + ")");
                    tv_nav_right.setTextColor(ContextCompat.getColor(VideoPickerActivity.this, R.color.colorAccent));
                    tv_nav_right.setEnabled(true);
                }
            }

            @Override
            public void remove(String path) {
                videoPaths.remove(path);

                if (videoPaths.size() == 0) {
                    tv_nav_right.setText("完成");
                    tv_nav_right.setTextColor(Color.parseColor("#999999"));
                    tv_nav_right.setEnabled(false);
                } else {
                    tv_nav_right.setText("完成(" + videoPaths.size() + "/" + maxNum + ")");
                    tv_nav_right.setTextColor(ContextCompat.getColor(VideoPickerActivity.this, R.color.colorAccent));
                    tv_nav_right.setEnabled(true);
                }
            }
        });
        photopicker_rv.setAdapter(videoPickerAdapter);

        popupWindow = new ListPopupWindow(this);
        popupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        popupWindow.setAnchorView(findViewById(R.id.photopicker_toollayout));
        popupWindow.setModal(true);
        popupWindow.setDropDownGravity(Gravity.BOTTOM);
        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            popupWindow.dismiss();
            VideoDirectory directory = dictModels.get(position);
            photopicker_dict.setText(directory.getBucket_display_name());
            currentKey = bucketIds.get(position);
            updateData(currentKey);
        });

        disposable = Observable.create((ObservableOnSubscribe<LinkedHashMap<String, VideoDirectory>>) e -> VideoPickerActivity.this.observableEmitter = e)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoDirectoryLinkedHashMap -> {
                    VideoPickerActivity.this.allHashMap = videoDirectoryLinkedHashMap;

                    // 加载第一页全部图片
                    if (videoDirectoryLinkedHashMap.containsKey("0")) {
                        models.addAll(videoDirectoryLinkedHashMap.get("0").getVideos());
                        videoPickerAdapter.notifyDataSetChanged();
                    }

                    // 加载视频目录菜单
                    dictModels.clear();
                    bucketIds.clear();
                    Iterator<Map.Entry<String, VideoDirectory>> iterator = videoDirectoryLinkedHashMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, VideoDirectory> entry = iterator.next();
                        if (entry.getKey().equals("0")) {
                            continue;
                        }
                        dictModels.add(entry.getValue());
                        bucketIds.add(entry.getKey());
                    }
                    dictModels.add(0, videoDirectoryLinkedHashMap.get("0"));
                    bucketIds.add(0, "0");
                    dictAdapter = new DictVideoAdapter(dictModels);
                    popupWindow.setAdapter(dictAdapter);
                });
    }

    @Override
    public int initViews() {
        return R.layout.activity_photopicker;
    }

    @Override
    public void loadData() {
        loadVideos();
    }

    @Override
    public int setStatusBarColor() {
        return Color.WHITE;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 0;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        BarUtils.setDark(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
        executorService.shutdownNow();
    }

    private void loadVideos() {
        LoaderManager.getInstance(this).initLoader(0, new Bundle(), new LoaderManager.LoaderCallbacks<Cursor>() {
            @NonNull
            @Override
            public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
                return new VideoDirectoryLoader(VideoPickerActivity.this);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
                if (data == null) {
                    LoaderManager.getInstance(VideoPickerActivity.this).destroyLoader(0);
                    return;
                }
                LinkedHashMap<String, VideoDirectory> hashMapVideoDirectory = new LinkedHashMap<>();
                // 所有视频
                VideoDirectory allVideoDirectory = new VideoDirectory();
                allVideoDirectory.setBucket_id("0");
                allVideoDirectory.setBucket_display_name("全部视频");
                while (data.moveToNext()) {
                    Video video = new Video();
                    int _id = data.getInt(data.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    video.setId(_id);
                    video.setDuration(data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
                    video.setPath(data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
                    String videoPath = data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));

                    // 生成缩略图
                    Utils.getVideoThumb(videoPath, "" + _id);

                    if (hashMapVideoDirectory.containsKey(data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)))) {
                        VideoDirectory videoDirectory = hashMapVideoDirectory.get(data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)));
                        videoDirectory.addVideo(video);
                    } else {
                        VideoDirectory videoDirectory = new VideoDirectory();
                        videoDirectory.setBucket_display_name(data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)));
                        videoDirectory.setBucket_id(data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)));
                        videoDirectory.addVideo(video);
                        hashMapVideoDirectory.put(data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)), videoDirectory);
                    }
                }
                LoaderManager.getInstance(VideoPickerActivity.this).destroyLoader(0);

                Iterator<Map.Entry<String, VideoDirectory>> it = hashMapVideoDirectory.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, VideoDirectory> entry = it.next();
                    // 补充全部音频
                    allVideoDirectory.addVideos(entry.getValue().getVideos());
                }
                hashMapVideoDirectory.put("0", allVideoDirectory);

                observableEmitter.onNext(hashMapVideoDirectory);
            }

            @Override
            public void onLoaderReset(@NonNull Loader<Cursor> loader) {

            }
        });
    }

    private void updateData(String key) {
        ((GridLayoutManager) photopicker_rv.getLayoutManager()).scrollToPositionWithOffset(0, 0);
        models.clear();
        List<Video> temp = allHashMap.get(key).getVideos();
        for (Video photo : temp) {
            if (videoPaths.contains(photo.getPath())) {
                photo.setSelect(true);
            }
        }
        models.addAll(temp);
        videoPickerAdapter.notifyDataSetChanged();
    }

    private void adjustHeight() {
        if (dictModels.size() > 0) {
            int count = dictModels.size() < COUNT_MAX ? dictModels.size() : COUNT_MAX;
            if (popupWindow != null) {
                popupWindow.setHeight(count * SizeUtils.dp2px(90));
            }
        }
    }
}
