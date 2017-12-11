package com.renyu.androidimagelibrary

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.GridLayout
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.renyu.androidimagelibrary.view.UploadView
import com.renyu.commonlibrary.baseact.BaseActivity
import com.renyu.commonlibrary.views.actionsheet.ActionSheetFragment
import com.renyu.imagelibrary.commonutils.Utils
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

    val picPath = ArrayList<String>()

    val upload: UploadImageManager by lazy {
        UploadImageManager()
    }

    override fun setStatusBarColor() = Color.BLACK

    override fun setStatusBarTranslucent() = 0

    override fun loadData() {
        grid_pic.post {
            addImage("", -1)
        }
    }

    // 刷新gridlayout
    var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)

            if (msg != null && msg.data != null) {
                val path = msg.data.getString("path")
                val percent = msg.data.getInt("percent")
                refreshPic(path, percent)
            }
        }
    }

    override fun initParams() {
        upload.addListener {
            println("${it.url}   ${it.statue}   ${it.progress}")
            val message = Message()
            val bundle = Bundle()
            bundle.putString("path", it.filePath)
            bundle.putInt("percent", it.progress)
            message.data = bundle
            handler.sendMessage(message)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CommonParams.RESULT_ALUMNI -> {
                    val temp=data?.extras?.getStringArrayList("choiceImages")
                    val filePaths=ArrayList<String>()
                    if (temp != null) {
                        for (i in 0 until temp.size) {
                            val file=File(temp[i])
                            if (file.exists() && file.length()>0) {
                                filePaths.add(temp[i])
                            }
                        }
                        if (filePaths==null) {
                            return
                        }
                        grid_pic.removeView(grid_pic.getChildAt(grid_pic.childCount -1))
                        picPath.addAll(filePaths)
                        for (i in 0 until filePaths.size) {
                            addImage(filePaths[i], -1)
                        }
                        if (picPath.size < 10) {
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
            override fun deletePic() {
                upload.cancelTask("aizuna_"+File(path).name.substring(0, File(path).name.indexOf(".")))
                picPath.remove(path)
                grid_pic.removeView(view)
                if (picPath.size==9) {
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
                upload.addTask(path, "http://www.zksell.com/index.php?s=Api/Base/uploadpic")
            }
        }

        val params = GridLayout.LayoutParams()
        params.width = grid_pic.measuredWidth / 4 - SizeUtils.dp2px(8f)
        params.height = SizeUtils.dp2px(80f)
        params.leftMargin = SizeUtils.dp2px(4f)
        params.topMargin = SizeUtils.dp2px(4f)
        params.rightMargin = SizeUtils.dp2px(4f)
        params.bottomMargin = SizeUtils.dp2px(4f)
        if (position != -1) {
            grid_pic.addView(view, 0, params)
        }
        else {
            grid_pic.addView(view, params)
        }
    }

    fun choicePic() {
        val view_clearmessage: View = LayoutInflater.from(this)
                .inflate(R.layout.view_actionsheet_button_3, null, false)
        val actionSheetFragment: ActionSheetFragment = ActionSheetFragment.build(supportFragmentManager)
                .setChoice(ActionSheetFragment.CHOICE.CUSTOMER)
                .setTitle("设置图片")
                .setCustomerView(view_clearmessage)
                .show()
        val pop_three_choice1: TextView = view_clearmessage.findViewById(R.id.pop_three_choice1)
        pop_three_choice1.text = "拍照"
        pop_three_choice1.setOnClickListener {
            Utils.takePicture(this, CommonParams.RESULT_TAKEPHOTO)
            actionSheetFragment.dismiss()
        }
        val pop_three_choice2: TextView= view_clearmessage.findViewById(R.id.pop_three_choice2)
        pop_three_choice2.text = "从相册获取"
        pop_three_choice2.setOnClickListener {
            Utils.choicePic(this, 10-picPath.size, CommonParams.RESULT_ALUMNI)
            actionSheetFragment.dismiss()
        }
        val pop_three_cancel: TextView = view_clearmessage.findViewById(R.id.pop_three_cancel)
        pop_three_cancel.text = "取消"
        pop_three_cancel.setOnClickListener {
            actionSheetFragment.dismiss()
        }
    }

    fun refreshPic(path: String, percent: Int) {
        for (i in 0 until grid_pic.childCount) {
            val tag = grid_pic.getChildAt(i).tag
            if (tag != null && grid_pic.getChildAt(i).tag == path) {
                (grid_pic.getChildAt(i) as UploadView).updateMaskPercent(percent)
            }
        }
    }
}