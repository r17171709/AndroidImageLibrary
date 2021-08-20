package com.renyu.imagelibrary.photopicker;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import com.facebook.drawee.view.SimpleDraweeView;
import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.commonlibrary.commonutils.BarUtils;
import com.renyu.commonlibrary.permission.annotation.NeedPermission;
import com.renyu.commonlibrary.permission.annotation.PermissionDenied;
import com.renyu.imagelibrary.R;
import com.renyu.imagelibrary.SpaceItemDecoration;
import com.renyu.imagelibrary.bean.Photo;
import com.renyu.imagelibrary.bean.PhotoDirectory;
import com.renyu.imagelibrary.commonutils.PhotoDirectoryLoader;
import com.renyu.imagelibrary.params.CommonParams;
import com.stfalcon.imageviewer.common.util.ImagePreviewUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Created by Clevo on 2016/8/31.
 */
public class PhotoPickerActivity extends BaseActivity {
    private TextView tv_nav_right;
    private RecyclerView photopicker_rv;
    private PhotoPickerAdapter adapter;
    private TextView photopicker_dict;
    private TextView photopicker_preview;
    private ListPopupWindow popupWindow;
    private DictAdapter dictAdapter;

    //全部文件
    private LinkedHashMap<String, PhotoDirectory> allHashMap;
    //列表加载图片
    private ArrayList<Photo> models;
    //列表加载文件夹
    private ArrayList<PhotoDirectory> dictModels;
    private ArrayList<String> bucketIds;
    private Disposable disposable = null;
    private ObservableEmitter<LinkedHashMap<String, PhotoDirectory>> observableEmitter;
    //最大可选图片数量
    int maxNum = 0;
    //选中的图片
    public ArrayList<Uri> imagePaths;
    //最大显示文件夹数量
    private int COUNT_MAX = 4;
    //当前文件夹key
    private String currentKey = "0";

    private ImagePreviewUtils imagePreviewUtils = new ImagePreviewUtils();

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
        imagePreviewUtils.closePreview();
    }

    @Override
    public void initParams() {
        allHashMap = new LinkedHashMap<>();
        models = new ArrayList<>();
        dictModels = new ArrayList<>();
        bucketIds = new ArrayList<>();
        imagePaths = new ArrayList<>();

        maxNum = getIntent().getExtras().getInt("maxNum");

        ImageView ib_nav_left = findViewById(R.id.ib_nav_left);
        ib_nav_left.setImageResource(R.mipmap.icon_back_black);
        ib_nav_left.setOnClickListener(v -> finish());
        TextView tv_nav_title = findViewById(R.id.tv_nav_title);
        tv_nav_title.setTextColor(Color.parseColor("#333333"));
        tv_nav_title.setText("图片");
        tv_nav_right = findViewById(R.id.tv_nav_right);
        tv_nav_right.setText("完成");
        tv_nav_right.setTextColor(Color.parseColor("#999999"));
        tv_nav_right.setEnabled(false);
        tv_nav_right.setOnClickListener(v -> {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("choiceImages", imagePaths);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
        });
        photopicker_rv = findViewById(R.id.photopicker_rv);
        photopicker_rv.setHasFixedSize(true);
        photopicker_rv.setLayoutManager(new GridLayoutManager(this, 3));
        photopicker_rv.addItemDecoration(new SpaceItemDecoration(1, 3));
        adapter = new PhotoPickerAdapter(this, models, new PhotoPickerAdapter.OperImageListener() {
            @Override
            public void add(Uri path) {
                if (!imagePaths.contains(path)) {
                    imagePaths.add(path);

                    tv_nav_right.setText("完成(" + imagePaths.size() + "/" + maxNum + ")");
                    tv_nav_right.setTextColor(ContextCompat.getColor(PhotoPickerActivity.this, R.color.colorAccent));
                    tv_nav_right.setEnabled(true);
                    photopicker_preview.setText("预览(" + imagePaths.size() + ")");
                }
            }

            @Override
            public void remove(Uri path) {
                imagePaths.remove(path);

                if (imagePaths.size() == 0) {
                    tv_nav_right.setText("完成");
                    tv_nav_right.setTextColor(Color.parseColor("#999999"));
                    tv_nav_right.setEnabled(false);
                    photopicker_preview.setText("预览");
                } else {
                    tv_nav_right.setText("完成(" + imagePaths.size() + "/" + maxNum + ")");
                    tv_nav_right.setTextColor(ContextCompat.getColor(PhotoPickerActivity.this, R.color.colorAccent));
                    tv_nav_right.setEnabled(true);
                    photopicker_preview.setText("预览(" + imagePaths.size() + ")");
                }
            }

            @Override
            public void showPreview(Uri path, SimpleDraweeView simpleDraweeView) {
                ArrayList<Uri> tmp = new ArrayList<>();
                tmp.add(path);
                imagePreviewUtils.showPreview(PhotoPickerActivity.this, simpleDraweeView, 0, tmp);
            }
        });
        photopicker_rv.setAdapter(adapter);
        photopicker_dict = findViewById(R.id.photopicker_dict);
        photopicker_dict.setOnClickListener(v -> {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            } else if (!isFinishing()) {
                adjustHeight();
                popupWindow.show();
            }
        });
        photopicker_preview = findViewById(R.id.photopicker_preview);
        photopicker_preview.setOnClickListener(v -> {
            if (imagePaths.size() > 0) {
                imagePreviewUtils.showPreview(this, 0, imagePaths);
            }
        });

        popupWindow = new ListPopupWindow(this);
        popupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        popupWindow.setAnchorView(findViewById(R.id.photopicker_toollayout));
        popupWindow.setModal(true);
        popupWindow.setDropDownGravity(Gravity.BOTTOM);
        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            popupWindow.dismiss();
            PhotoDirectory directory = dictModels.get(position);
            photopicker_dict.setText(directory.getName());
            currentKey = bucketIds.get(position);
            updateData(currentKey);
        });

        disposable = Observable.create((ObservableOnSubscribe<LinkedHashMap<String, PhotoDirectory>>) e -> PhotoPickerActivity.this.observableEmitter = e)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringPhotoDirectoryLinkedHashMap -> {
                    PhotoPickerActivity.this.allHashMap = stringPhotoDirectoryLinkedHashMap;
                    if (stringPhotoDirectoryLinkedHashMap.containsKey("0")) {
                        updateData(currentKey);

                        dictModels.clear();
                        bucketIds.clear();
                        Iterator iterator = stringPhotoDirectoryLinkedHashMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry entry = (Map.Entry) iterator.next();
                            if (entry.getKey().toString().equals("0")) {
                                continue;
                            }
                            dictModels.add((PhotoDirectory) entry.getValue());
                            bucketIds.add((String) entry.getKey());
                        }
                        dictModels.add(0, stringPhotoDirectoryLinkedHashMap.get("0"));
                        bucketIds.add(0, "0");
                        dictAdapter = new DictAdapter(PhotoPickerActivity.this, dictModels);
                        popupWindow.setAdapter(dictAdapter);
                    }
                });
    }

    @Override
    public int initViews() {
        return R.layout.activity_photopicker;
    }

    @Override
    public void loadData() {
        permissionApply();
    }

    private void adjustHeight() {
        if (dictModels.size() > 0) {
            int count = dictModels.size() < COUNT_MAX ? dictModels.size() : COUNT_MAX;
            if (popupWindow != null) {
                popupWindow.setHeight(count * SizeUtils.dp2px(90));
            }
        }
    }

    private void loadImages() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(CommonParams.EXTRA_SHOW_GIF, false);
        LoaderManager.getInstance(this).initLoader(0, bundle, new LoaderManager.LoaderCallbacks<Cursor>() {
            @NonNull
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new PhotoDirectoryLoader(PhotoPickerActivity.this, args.getBoolean(CommonParams.EXTRA_SHOW_GIF, false));
            }


            @Override
            public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
                if (data == null) {
                    LoaderManager.getInstance(PhotoPickerActivity.this).destroyLoader(0);
                    return;
                }
                LinkedHashMap<String, PhotoDirectory> hashMap = new LinkedHashMap<>();
                PhotoDirectory photoDirectoryAll = new PhotoDirectory();
                photoDirectoryAll.setName("所有图片");
                photoDirectoryAll.setId("ALL");

                while (data.moveToNext()) {
                    int imageId = data.getInt(data.getColumnIndexOrThrow(_ID));
                    String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
                    String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
                    String path = data.getString(data.getColumnIndexOrThrow(DATA));
                    Uri uri = Uri.parse("file://" + path);
//                    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                            data.getLong(data.getColumnIndex(_ID)));
//                    Log.d("PhotoPickerActivity", bucketId+" "+name+" "+path + " " + uri.getPath());
                    if (!hashMap.containsKey(bucketId)) {
                        PhotoDirectory photoDirectory = new PhotoDirectory();
                        photoDirectory.setId(bucketId);
                        photoDirectory.setName(name);
                        photoDirectory.setCoverPath(uri);
                        photoDirectory.addPhoto(imageId, uri);
                        photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
                        hashMap.put(bucketId, photoDirectory);
                    } else {
                        hashMap.get(bucketId).addPhoto(imageId, uri);
                    }
                    photoDirectoryAll.addPhoto(imageId, uri);
                }
                if (photoDirectoryAll.getPhotos().size() > 0) {
                    photoDirectoryAll.setCoverPath(photoDirectoryAll.getPhotos().get(0).getPath());
                }
                hashMap.put("0", photoDirectoryAll);
                observableEmitter.onNext(hashMap);

                LoaderManager.getInstance(PhotoPickerActivity.this).destroyLoader(0);
            }

            @Override
            public void onLoaderReset(@NonNull Loader<Cursor> loader) {

            }
        });
    }

    private void updateData(String key) {
        ((GridLayoutManager) photopicker_rv.getLayoutManager()).scrollToPositionWithOffset(0, 0);
        models.clear();
        List<Photo> temp = allHashMap.get(key).getPhotos();
        for (Photo photo : temp) {
            if (imagePaths.contains(photo.getPath())) {
                photo.setSelect(true);
            }
        }
        models.addAll(temp);
        adapter.notifyDataSetChanged();
    }

    @NeedPermission(permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            deniedDesp = "为了您可以正常访问相册，\n请点击\"设置\"-\"权限\"-打开 \"存储空间\" 权限。\n最后点击两次后退按钮，即可返回。")
    public void permissionApply() {
        loadImages();
    }

    @PermissionDenied(permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void permissionDenied() {
        finish();
    }
}