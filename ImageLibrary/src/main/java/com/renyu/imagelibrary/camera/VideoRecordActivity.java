package com.renyu.imagelibrary.camera;

import android.content.Intent;
import android.os.Handler;
import android.view.ViewGroup;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.imagelibrary.R;
import com.renyu.imagelibrary.view.ProgressCircleView;
import com.renyu.imagelibrary.view.RecordView;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class VideoRecordActivity extends BaseActivity {
    private RecordView recordView;
    private ProgressCircleView pc_record;

    private Disposable disposable;

    // 最大时间
    private int maxTime = 30;
    private int tmpTime = 0;

    // 是否已经结束
    private boolean isEnd = false;

    @Override
    public void initParams() {
        recordView = findViewById(R.id.recordView);
        recordView.setOnGestureListener(new RecordView.OnGestureListener() {
            @Override
            public void onDown() {
                down();
            }

            @Override
            public void onUp() {
                up();
            }
        });
        pc_record = findViewById(R.id.pc_record);
    }

    @Override
    public int initViews() {
        return R.layout.activity_videorecord;
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

    private void down() {
        ((ViedoRecordView) findViewById(R.id.video_record)).startRecord();
        ToastUtils.showShort("开始录制");

        ViewGroup.LayoutParams paramsPcRecord = pc_record.getLayoutParams();
        paramsPcRecord.height = SizeUtils.dp2px(100f);
        paramsPcRecord.width = SizeUtils.dp2px(100f);
        pc_record.setLayoutParams(paramsPcRecord);
        pc_record.requestLayout();
        ViewGroup.LayoutParams paramsRecordView = recordView.getLayoutParams();
        paramsRecordView.height = SizeUtils.dp2px(35f);
        paramsRecordView.width = SizeUtils.dp2px(35f);
        recordView.setLayoutParams(paramsRecordView);
        recordView.requestLayout();

        disposable = Observable.interval(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            tmpTime++;
            pc_record.setText("", tmpTime * 100 / maxTime);
            if (tmpTime == maxTime) {
                up();
            }
        });
    }

    private void up() {
        if (isEnd) {
            return;
        }

        isEnd = true;

        ((ViedoRecordView) findViewById(R.id.video_record)).finishRecord();
        ToastUtils.showShort("录制成功");

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        recordView.initState();
        pc_record.setText("", 0);

        ViewGroup.LayoutParams paramsPcRecord = pc_record.getLayoutParams();
        paramsPcRecord.height = SizeUtils.dp2px(68f);
        paramsPcRecord.width = SizeUtils.dp2px(68f);
        pc_record.setLayoutParams(paramsPcRecord);
        pc_record.requestLayout();
        ViewGroup.LayoutParams paramsRecordView = recordView.getLayoutParams();
        paramsRecordView.height = SizeUtils.dp2px(48f);
        paramsRecordView.width = SizeUtils.dp2px(48f);
        recordView.setLayoutParams(paramsRecordView);
        recordView.requestLayout();

        new Handler().postDelayed(() -> {
            File file = ((ViedoRecordView) findViewById(R.id.video_record)).getVecordFile();
            Intent intent = new Intent();
            intent.putExtra("path", file.getPath());
            setResult(RESULT_OK, intent);
            finish();
        }, 2000);
    }
}
