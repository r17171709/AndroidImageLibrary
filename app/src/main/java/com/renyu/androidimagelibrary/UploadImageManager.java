package com.renyu.androidimagelibrary;

import android.text.TextUtils;
import android.util.Log;

import com.renyu.commonlibrary.network.OKHttpHelper;
import com.renyu.commonlibrary.network.OKHttpUtils;
import com.renyu.commonlibrary.params.InitParams;
import com.renyu.imagelibrary.bean.UploadTaskBean;
import com.renyu.imagelibrary.commonutils.Utils;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by renyu on 2017/12/7.
 */

public class UploadImageManager {
    private ExecutorService uploadService;
    private OKHttpUtils okHttpUtils;

    // 上传状态回调
    public interface UpdateCallBack {
        void updateMap(UploadTaskBean bean);
    }

    private UpdateCallBack callBack;

    // 任务线程组
    private ConcurrentHashMap<String, Future> tasks;
    // 任务状态组
    private ConcurrentHashMap<String, UploadTaskBean> beans;

    public UploadImageManager() {
        tasks = new ConcurrentHashMap<>();
        beans = new ConcurrentHashMap<>();

        okHttpUtils = OKHttpHelper.getInstance().getOkHttpUtils();

        uploadService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * 添加任务
     *
     * @param filePath
     * @param url
     * @param tag
     */
    public synchronized void addTask(String filePath, String url, String tag) {
        Runnable runnable = () -> {
            // 开始上传
            UploadTaskBean bean = beans.get(tag);
            bean.setUrl("");
            bean.setProgress(0);
            bean.setStatue(UploadTaskBean.UploadState.UPLOADING);

            // 剪裁图片
            File cropFile = Utils.compressPic(com.blankj.utilcode.util.Utils.getApp(), filePath, InitParams.CACHE_PATH);

            HashMap<String, File> fileHashMap = new HashMap<>();
            fileHashMap.put("fileData", cropFile);
            String uploadValue = okHttpUtils.syncUpload(url, null, fileHashMap, null, (l, l1) -> {
                Log.d("UploadImageManager", "UploadImageManager " + l + " " + l1);
                // 上传每20%进度刷新一次，上传完成不进行修改以防止与后续成功的回调不一致
                if ((l * 100 / l1 - bean.getProgress() >= 20) && l != l1) {
                    bean.setUrl("");
                    bean.setProgress((int) (l * 100 / l1));
                    bean.setStatue(UploadTaskBean.UploadState.UPLOADING);
                    if (callBack != null) {
                        callBack.updateMap(bean);
                    }
                }
            });
            if (uploadValue == null) {
                Log.d("UploadImageManager", filePath + "发布失败");
            } else {
                JSONObject jsonObject;
                try {
                    // 上传成功
                    jsonObject = new JSONObject(uploadValue);
                    String picUrl = jsonObject.getJSONObject("data").getString("picUrl");
                    if (TextUtils.isEmpty(picUrl)) {
                        Log.d("UploadImageManager", filePath + "发布失败");
                    } else {
                        Log.d("UploadImageManager", filePath + "发布成功:" + picUrl);

                        bean.setProgress(100);
                        bean.setUrl(picUrl);
                        bean.setStatue(UploadTaskBean.UploadState.UPLOADSUCCESS);
                        if (callBack != null) {
                            callBack.updateMap(bean);
                        }
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("UploadImageManager", filePath + "发布失败");
                }
            }

            // 上传失败
            bean.setProgress(0);
            bean.setUrl("");
            bean.setStatue(UploadTaskBean.UploadState.UPLOADFAIL);
            if (callBack != null) {
                callBack.updateMap(bean);
            }
        };

        // 存在相同任务
        if (beans.containsKey(tag) && tasks.containsKey(tag)) {
            // 如果上传完成，则直接刷新
            if (beans.get(tag).getStatue() == UploadTaskBean.UploadState.UPLOADSUCCESS) {
                if (callBack != null) {
                    callBack.updateMap(beans.get(tag));
                }
                return;
            }
            // 如果上传失败，则重新上传
            else if (beans.get(tag).getStatue() == UploadTaskBean.UploadState.UPLOADFAIL) {

            }
            // 如果正在上传中或者处于队列中，则不进行添加
            else {
                return;
            }
        }
        // 防止出现错误情况
        cancelTask(tag);

        UploadTaskBean bean = new UploadTaskBean();
        bean.setFilePath(filePath);
        bean.setProgress(0);
        bean.setStatue(UploadTaskBean.UploadState.UPLOADPREPARE);
        // 添加上传状态Map中
        beans.put(tag, bean);
        // 添加上传线程池中
        tasks.put(tag, uploadService.submit(runnable));
    }

    /**
     * 取消一个任务
     *
     * @param tag
     */
    public void cancelTask(String tag) {
        if (tasks.containsKey(tag)) {
            tasks.remove(tag).cancel(true);
        }
        beans.remove(tag);
    }

    /**
     * 关闭全部任务
     */
    public synchronized void stopAllTask() {
        uploadService.shutdownNow();
        tasks.clear();
        beans.clear();
    }

    public void addListener(UpdateCallBack callBack) {
        this.callBack = callBack;
    }
}