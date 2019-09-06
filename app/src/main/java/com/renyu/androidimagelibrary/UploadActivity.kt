package com.renyu.androidimagelibrary

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.gridlayout.widget.GridLayout
import com.blankj.utilcode.util.SizeUtils
import com.renyu.androidimagelibrary.view.UploadView
import com.renyu.commonlibrary.baseact.BaseActivity
import com.renyu.commonlibrary.permission.annotation.NeedPermission
import com.renyu.commonlibrary.permission.annotation.PermissionDenied
import com.renyu.commonlibrary.views.actionsheet.ActionSheetFactory
import com.renyu.imagelibrary.bean.UploadTaskBean
import com.renyu.imagelibrary.camera.CameraFragment
import com.renyu.imagelibrary.camera.CameraPreviewActivity
import com.renyu.imagelibrary.commonutils.Utils
import kotlinx.android.synthetic.main.activity_upload.*
import java.io.File


/**
 * Created by renyu on 2017/12/7.
 */
class UploadActivity : BaseActivity() {
    // 选择的本地图片文件路径集合
    val picPath = ArrayList<String>()
    // 远程上传完成的图片文件集合
    private val urlMaps = HashMap<String, String>()

    val upload: UploadImageManager by lazy {
        UploadImageManager()
    }

    // 相机小功能
    private val lists: ArrayList<CameraFragment.CameraFunction> by lazy {
        ArrayList<CameraFragment.CameraFunction>()
    }

    override fun setStatusBarColor() = Color.BLACK

    override fun setStatusBarTranslucent() = 0

    override fun loadData() {
        grid_pic.post {
            addImage("", -1)
        }
    }

    // 刷新gridlayout
    private var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)

            if (msg != null && msg.data != null) {
                val path = msg.data.getString("path")
                val percent = msg.data.getInt("percent")
                val statue = msg.data.getSerializable("statue") as UploadTaskBean.UploadState
                val url = msg.data.getString("url")
                refreshPic(path, percent, statue, url)
            }
        }
    }

    override fun initParams() {
        lists.add(CameraFragment.CameraFunction.PhotoPicker)
        lists.add(CameraFragment.CameraFunction.ChangeCamera)

        upload.addListener {
            println("${it.url}   ${it.statue}   ${it.progress}")
            val message = Message()
            val bundle = Bundle()
            bundle.putString("path", it.filePath)
            bundle.putInt("percent", it.progress)
            bundle.putSerializable("statue", it.statue)
            bundle.putString("url", it.url)
            message.data = bundle
            handler.sendMessage(message)
        }
        button_commit.setOnClickListener {
            picPath.filter {
                urlMaps.containsKey(it)
            }.forEach {
                println(urlMaps[it])
            }
        }
    }

    override fun initViews() = R.layout.activity_upload

    override fun onDestroy() {
        super.onDestroy()
        upload.stopAllTask()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED && requestCode == CommonParams.RESULT_CAMERAPREVIEW) {
            Utils.takePicture2(this, CommonParams.RESULT_TAKEPHOTO, lists, false)
        }
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CommonParams.RESULT_TAKEPHOTO -> {
                    val path = data?.extras?.getString("path")!!
                    val intent = Intent(this@UploadActivity, CameraPreviewActivity::class.java)
                    intent.putExtra("path", path)
                    startActivityForResult(intent, CommonParams.RESULT_CAMERAPREVIEW)
                }
                CommonParams.RESULT_CAMERAPREVIEW -> {
                    val path = data?.extras?.getString("path")!!
                    grid_pic.removeView(grid_pic.getChildAt(grid_pic.childCount - 1))
                    picPath.add(path)
                    addImage(path, -1)
                    if (picPath.size < 9) {
                        addImage("", -1)
                    }
                }
                CommonParams.RESULT_ALUMNI -> {
                    val temp = data?.extras?.getStringArrayList("choiceImages")
                    val filePaths = ArrayList<String>()
                    if (temp != null) {
                        for (i in 0 until temp.size) {
                            val file = File(temp[i])
                            // 避免重复且出错的图片进行上传
                            if (file.exists() && file.length() > 0) {
                                var find = false
                                for (filePath in filePaths) {
                                    // 文件名称相同则跳过
                                    if (File(filePath).name == file.name) {
                                        find = true
                                        break
                                    }
                                }
                                if (!find) {
                                    filePaths.add(temp[i])
                                }
                            }
                        }
                        if (filePaths.size == 0) {
                            return
                        }
                        grid_pic.removeView(grid_pic.getChildAt(grid_pic.childCount - 1))
                        picPath.addAll(filePaths)
                        for (i in 0 until filePaths.size) {
                            addImage(filePaths[i], -1)
                        }
                        if (picPath.size < 9) {
                            addImage("", -1)
                        }
                    }
                }
            }
        }
    }

    fun addImage(path: String, position: Int) {
        val view: UploadView = LayoutInflater.from(this).inflate(R.layout.view_upload, null, false) as UploadView
        view.listener = object : UploadView.OnUIControllListener {
            override fun retryUploadPic() {
                retryPic(path)
            }

            override fun deletePic() {
                upload.cancelTask(File(path).name.substring(0, File(path).name.indexOf(".")))
                picPath.remove(path)
                urlMaps.remove(path)
                grid_pic.removeView(view)
                if (picPath.size == 8) {
                    addImage("", -1)
                }
            }

            override fun clickPic() {
                if (TextUtils.isEmpty(path)) {
                    choicePic()
                }
            }
        }
        view.loadPic(path)
        if (!TextUtils.isEmpty(path)) {
            view.post {
                upload.addTask(
                    path,
                    "http://www.zksell.com/index.php?s=Api/Base/uploadpic",
                    File(path).name.substring(0, File(path).name.lastIndexOf("."))
                )
            }
        }

        val params = GridLayout.LayoutParams()
        params.width = (grid_pic.measuredWidth - SizeUtils.dp2px(8f) * 3) / 3
        params.height = params.width
        params.leftMargin = SizeUtils.dp2px(4f)
        params.topMargin = SizeUtils.dp2px(4f)
        params.rightMargin = SizeUtils.dp2px(4f)
        params.bottomMargin = SizeUtils.dp2px(4f)
        if (position != -1) {
            grid_pic.addView(view, position, params)
        } else {
            grid_pic.addView(view, params)
        }
    }

    fun retryPic(path: String) {
        upload.addTask(
            path,
            "http://www.zksell.com/index.php?s=Api/Base/uploadpic",
            File(path).name.substring(0, File(path).name.lastIndexOf("."))
        )
    }

    @NeedPermission(
        permissions = [(Manifest.permission.READ_EXTERNAL_STORAGE), (Manifest.permission.WRITE_EXTERNAL_STORAGE)],
        deniedDesp = "请授予存储卡读取权限"
    )
    fun choicePic() {
        val view_clearmessage: View = LayoutInflater.from(this)
            .inflate(R.layout.view_actionsheet_button_3, null, false)
        val actionSheetFragment = ActionSheetFactory.createCustomActionSheetFragment(
            this,
            "",
            "设置图片",
            -1,
            "",
            -1,
            "",
            -1,
            true,
            view_clearmessage,
            null,
            null
        )
        val pop_three_choice1: TextView = view_clearmessage.findViewById(R.id.pop_three_choice1)
        pop_three_choice1.text = "拍照"
        pop_three_choice1.setOnClickListener {
            Utils.takePicture2(this, CommonParams.RESULT_TAKEPHOTO, lists, false)
            actionSheetFragment.dismiss()
        }
        val pop_three_choice2: TextView = view_clearmessage.findViewById(R.id.pop_three_choice2)
        pop_three_choice2.text = "从相册获取"
        pop_three_choice2.setOnClickListener {
            Utils.choicePic(this, 9 - picPath.size, CommonParams.RESULT_ALUMNI)
            actionSheetFragment.dismiss()
        }
        val pop_three_cancel: TextView = view_clearmessage.findViewById(R.id.pop_three_cancel)
        pop_three_cancel.text = "取消"
        pop_three_cancel.setOnClickListener {
            actionSheetFragment.dismiss()
        }
    }

    fun refreshPic(path: String, percent: Int, statue: UploadTaskBean.UploadState, url: String) {
        for (i in 0 until grid_pic.childCount) {
            val tag = grid_pic.getChildAt(i).tag
            if (tag != null && grid_pic.getChildAt(i).tag == path) {
                when (statue) {
                    UploadTaskBean.UploadState.UPLOADFAIL -> {
                        (grid_pic.getChildAt(i) as UploadView).uploadError()
                    }
                    UploadTaskBean.UploadState.UPLOADSUCCESS -> {
                        (grid_pic.getChildAt(i) as UploadView).uploadSuccess()
                        urlMaps[path] = url
                    }
                    else -> (grid_pic.getChildAt(i) as UploadView).uploadMaskPercent(percent)
                }
            }
        }
    }

    @PermissionDenied(permissions = [(Manifest.permission.READ_EXTERNAL_STORAGE), (Manifest.permission.WRITE_EXTERNAL_STORAGE)])
    fun denied() {

    }
}