package com.renyu.imagelibrary.photopicker;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.commonlibrary.commonutils.BarUtils;
import com.renyu.imagelibrary.R;
import com.renyu.imagelibrary.bean.Photo;
import com.renyu.imagelibrary.bean.PhotoDirectory;
import com.renyu.imagelibrary.camera.CameraActivity;
import com.renyu.imagelibrary.commonutils.PhotoDirectoryLoader;
import com.renyu.imagelibrary.commonutils.Utils;
import com.renyu.imagelibrary.params.CommonParams;
import com.renyu.imagelibrary.preview.ImagePreviewActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;

/**
 * Created by Clevo on 2016/8/31.
 */
public class PhotoPickerActivity extends BaseActivity {

    ImageView ib_nav_left;
    TextView tv_nav_title;
    TextView tv_nav_right;
    RecyclerView photopicker_rv;
    PhotoPickerAdapter adapter;
    TextView photopicker_dict;
    TextView photopicker_preview;
    ListPopupWindow popupWindow;
    DictAdapter dictAdapter;

    //全部文件
    LinkedHashMap<String, PhotoDirectory> allHashMap;
    //列表加载图片
    ArrayList<Photo> models;
    //列表加载文件夹
    ArrayList<PhotoDirectory> dictModels;
    ArrayList<String> bucketIds;
    ObservableEmitter<LinkedHashMap<String, PhotoDirectory>> observableEmitter;
    //最大可选图片数量
    int maxNum=0;
    //选中的图片
    public ArrayList<String> imagePaths;
    //最大显示文件夹数量
    int COUNT_MAX=4;
    //当前文件夹key
    String currentKey="0";

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
    public void initParams() {
        ib_nav_left = findViewById(R.id.ib_nav_left);
        tv_nav_title = findViewById(R.id.tv_nav_title);
        tv_nav_right = findViewById(R.id.tv_nav_right);
        photopicker_rv = findViewById(R.id.photopicker_rv);
        photopicker_dict = findViewById(R.id.photopicker_dict);
        photopicker_preview = findViewById(R.id.photopicker_preview);

        allHashMap=new LinkedHashMap<>();
        models=new ArrayList<>();
        dictModels=new ArrayList<>();
        bucketIds=new ArrayList<>();
        imagePaths=new ArrayList<>();

        maxNum=getIntent().getExtras().getInt("maxNum");

        ib_nav_left.setImageResource(R.mipmap.icon_back_black);
        ib_nav_left.setOnClickListener(v -> finish());
        tv_nav_title.setTextColor(Color.parseColor("#333333"));
        tv_nav_title.setText("图片");
        tv_nav_right.setText("完成");
        tv_nav_right.setTextColor(Color.parseColor("#999999"));
        tv_nav_right.setEnabled(false);
        tv_nav_right.setOnClickListener(v -> {
            Intent intent=new Intent();
            Bundle bundle=new Bundle();
            bundle.putStringArrayList("choiceImages", imagePaths);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
        });
        photopicker_rv.setHasFixedSize(true);
        photopicker_rv.setLayoutManager(new GridLayoutManager(this, 3));
        photopicker_rv.addItemDecoration(new SpaceItemDecoration(1, 3));
        adapter=new PhotoPickerAdapter(this, models, new PhotoPickerAdapter.OperImageListener() {
            @Override
            public void add(String path) {
                if (!imagePaths.contains(path)) {
                    imagePaths.add(path);

                    tv_nav_right.setText("完成("+imagePaths.size()+"/"+maxNum+")");
                    tv_nav_right.setTextColor(ContextCompat.getColor(PhotoPickerActivity.this, R.color.colorAccent));
                    tv_nav_right.setEnabled(true);
                    photopicker_preview.setText("预览("+imagePaths.size()+")");
                }
            }

            @Override
            public void remove(String path) {
                imagePaths.remove(path);

                if (imagePaths.size()==0) {
                    tv_nav_right.setText("完成");
                    tv_nav_right.setTextColor(Color.parseColor("#999999"));
                    tv_nav_right.setEnabled(false);
                    photopicker_preview.setText("预览");
                }
                else {
                    tv_nav_right.setText("完成("+imagePaths.size()+"/"+maxNum+")");
                    tv_nav_right.setTextColor(ContextCompat.getColor(PhotoPickerActivity.this, R.color.colorAccent));
                    tv_nav_right.setEnabled(true);
                    photopicker_preview.setText("预览("+imagePaths.size()+")");
                }
            }

            @Override
            public void show(String path) {
                Intent intent=new Intent(PhotoPickerActivity.this, ImagePreviewActivity.class);
                Bundle bundle=new Bundle();
                bundle.putBoolean("canDownload", false);
                bundle.putInt("position", 0);
                bundle.putBoolean("canEdit", false);
                ArrayList<String> urls=new ArrayList<>();
                urls.add(path);
                bundle.putStringArrayList("urls", urls);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void takePic() {
                Intent intent=new Intent(PhotoPickerActivity.this, CameraActivity.class);
                startActivityForResult(intent, CommonParams.RESULT_TAKECAMERA);
            }
        });
        photopicker_rv.setAdapter(adapter);
        photopicker_dict.setOnClickListener(v -> {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            else if (!isFinishing()){
                adjustHeight();
                popupWindow.show();
            }
        });
        photopicker_preview.setOnClickListener(v -> {
            if (imagePaths.size()>0) {
                Intent intent=new Intent(PhotoPickerActivity.this, ImagePreviewActivity.class);
                Bundle bundle=new Bundle();
                bundle.putBoolean("canDownload", false);
                bundle.putInt("position", 0);
                bundle.putBoolean("canEdit", true);
                bundle.putStringArrayList("urls", imagePaths);
                intent.putExtras(bundle);
                startActivityForResult(intent, CommonParams.RESULT_PREVIEW);
            }
        });

        popupWindow=new ListPopupWindow(this);
        popupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        popupWindow.setAnchorView(findViewById(R.id.photopicker_toollayout));
        popupWindow.setModal(true);
        popupWindow.setDropDownGravity(Gravity.BOTTOM);
        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            popupWindow.dismiss();
            PhotoDirectory directory = dictModels.get(position);
            photopicker_dict.setText(directory.getName());
            currentKey=bucketIds.get(position);
            updateData(currentKey);
        });
    }

    @Override
    public int initViews() {
        return R.layout.activity_photopicker;
    }

    @Override
    public void loadData() {
        loadImages();
    }

    private void adjustHeight() {
        if (dictModels.size()>0) {
            int count=dictModels.size()<COUNT_MAX?dictModels.size():COUNT_MAX;
            if (popupWindow!=null) {
                popupWindow.setHeight(count* SizeUtils.dp2px(90));
            }
        }
    }

    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private int space;
        private int column;

        SpaceItemDecoration(int space, int column) {
            this.space = space;
            this.column = column;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = SizeUtils.dp2px(space);
            outRect.bottom = space;
            if (parent.getChildLayoutPosition(view)%column==0) {
                outRect.left = 0;
            }
        }
    }

    private void loadImages() {
        Observable.create((ObservableOnSubscribe<LinkedHashMap<String, PhotoDirectory>>) e -> PhotoPickerActivity.this.observableEmitter=e)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringPhotoDirectoryLinkedHashMap -> {
                    PhotoPickerActivity.this.allHashMap=stringPhotoDirectoryLinkedHashMap;
                    if (stringPhotoDirectoryLinkedHashMap.containsKey("0")) {
                        updateData(currentKey);

                        dictModels.clear();
                        bucketIds.clear();
                        Iterator iterator=stringPhotoDirectoryLinkedHashMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry entry= (Map.Entry) iterator.next();
                            if (entry.getKey().toString().equals("0")) {
                                continue;
                            }
                            dictModels.add((PhotoDirectory) entry.getValue());
                            bucketIds.add((String) entry.getKey());
                        }
                        dictModels.add(0, stringPhotoDirectoryLinkedHashMap.get("0"));
                        bucketIds.add(0, "0");
                        dictAdapter=new DictAdapter(PhotoPickerActivity.this, dictModels);
                        popupWindow.setAdapter(dictAdapter);
                    }
                });

        Bundle bundle=new Bundle();
        bundle.putBoolean(CommonParams.EXTRA_SHOW_GIF, false);
        getSupportLoaderManager().initLoader(0, bundle, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new PhotoDirectoryLoader(PhotoPickerActivity.this, args.getBoolean(CommonParams.EXTRA_SHOW_GIF, false));
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (data == null)  {
                    getSupportLoaderManager().destroyLoader(0);
                    return;
                }
                LinkedHashMap<String, PhotoDirectory> hashMap=new LinkedHashMap<>();
                PhotoDirectory photoDirectoryAll = new PhotoDirectory();
                photoDirectoryAll.setName("所有图片");
                photoDirectoryAll.setId("ALL");

                while (data.moveToNext()) {
                    int imageId  = data.getInt(data.getColumnIndexOrThrow(_ID));
                    String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
                    String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
                    String path = data.getString(data.getColumnIndexOrThrow(DATA));
                    File file=new File(path);
                    if (file.exists() && file.length()>0) {

                    }
                    else {
                        continue;
                    }
//                    Log.d("PhotoPickerActivity", bucketId+" "+name+" "+path);
                    if (!hashMap.containsKey(bucketId)) {
                        PhotoDirectory photoDirectory = new PhotoDirectory();
                        photoDirectory.setId(bucketId);
                        photoDirectory.setName(name);
                        photoDirectory.setCoverPath(path);
                        photoDirectory.addPhoto(imageId, path);
                        photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
                        hashMap.put(bucketId, photoDirectory);
                    }
                    else {
                        hashMap.get(bucketId).addPhoto(imageId, path);
                    }
                    photoDirectoryAll.addPhoto(imageId, path);
                }
                if (photoDirectoryAll.getPhotos().size() > 0) {
                    photoDirectoryAll.setCoverPath(photoDirectoryAll.getPhotos().get(0).getPath());
                }
                hashMap.put("0", photoDirectoryAll);
                observableEmitter.onNext(hashMap);

                getSupportLoaderManager().destroyLoader(0);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });
    }

    private void updateData(String key) {
        ((GridLayoutManager) photopicker_rv.getLayoutManager()).scrollToPositionWithOffset(0, 0);
        models.clear();
        List<Photo> temp=allHashMap.get(key).getPhotos();
        List<Photo> photos=new ArrayList<>();
        for (Photo photo : temp) {
            File file=new File(photo.getPath());
            if (file.exists() & file.length()>0) {
                photos.add(photo);
            }
        }
        for (Photo photo : photos) {
            if (imagePaths.contains(photo.getPath())) {
                photo.setSelect(true);
            }
        }
        models.addAll(photos);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== CommonParams.RESULT_TAKECAMERA && resultCode==RESULT_OK) {
            String filePath=data.getExtras().getString("path");
            Utils.cropImage(filePath, PhotoPickerActivity.this, CommonParams.RESULT_CROP, 0);
        }
        else if (requestCode== CommonParams.RESULT_CROP && resultCode==RESULT_OK) {
            String filePath=data.getExtras().getString("path");
            Intent intent=new Intent();
            Bundle bundle=new Bundle();
            ArrayList<String> strings=new ArrayList<>();
            strings.add(filePath);
            bundle.putStringArrayList("choiceImages", strings);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
        }
        else if (requestCode== CommonParams.RESULT_PREVIEW && resultCode==RESULT_OK) {
            imagePaths.clear();
            imagePaths.addAll(data.getStringArrayListExtra("urls"));
            loadImages();
        }
    }
}
