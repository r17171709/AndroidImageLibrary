package com.renyu.androidimagelibrary

import android.graphics.Color
import com.renyu.commonlibrary.baseact.BaseActivity
import kotlinx.android.synthetic.main.activity_upload.*
import java.io.File

/**
 * Created by renyu on 2017/12/7.
 */
class UploadActivity: BaseActivity() {

    val pics = arrayOf("/storage/emulated/0/Pictures/dongqiudi/1512634878367.jpg",
            "/storage/emulated/0/Pictures/dongqiudi/1512635214549.jpg",
            "/storage/emulated/0/Pictures/dongqiudi/1512635218258.jpg",
            "/storage/emulated/0/Pictures/dongqiudi/1512635223683.jpg",
            "/storage/emulated/0/Pictures/dongqiudi/1512635228373.jpg")

    var count = 0

    val upload: UploadImageManager by lazy {
        UploadImageManager()
    }

    override fun setStatusBarColor() = Color.BLACK

    override fun setStatusBarTranslucent() = 0

    override fun loadData() {

    }

    override fun initParams() {
        upload.addListener {
            println("${it.url}   ${it.statue}")
        }
        button_start.setOnClickListener {
            upload.addTask(pics[count%5], "http://www.zksell.com/index.php?s=Api/Base/uploadpic")
            count++
        }
        button_cancel.setOnClickListener {
            upload.cancelTask("aizuna_"+File(pics[(count-1)%5]).name.substring(0, File(pics[(count-1)%5]).name.indexOf(".")))
        }
    }

    override fun initViews() = R.layout.activity_upload

    override fun onDestroy() {
        super.onDestroy()
        upload.stopAllTask()
    }
}