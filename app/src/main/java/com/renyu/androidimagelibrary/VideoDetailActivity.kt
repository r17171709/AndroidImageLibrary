package com.renyu.androidimagelibrary

import android.graphics.Color
import com.renyu.commonlibrary.baseact.BaseActivity
import kotlinx.android.synthetic.main.activity_videodetail.*
import xyz.doikki.videocontroller.StandardVideoController

class VideoDetailActivity : BaseActivity() {
    override fun initParams() {
        videoplayer.setUrl("http://vfx.mtime.cn/Video/2019/03/14/mp4/190314223540373995.mp4")
        val controller = StandardVideoController(this)
        controller.addDefaultControlComponent("标题", false)
        videoplayer.setVideoController(controller)
        videoplayer.start()
    }

    override fun initViews() = R.layout.activity_videodetail

    override fun loadData() {

    }

    override fun setStatusBarColor() = Color.BLACK

    override fun setStatusBarTranslucent() = 0

    override fun onPause() {
        super.onPause()
        videoplayer.pause()
    }

    override fun onResume() {
        super.onResume()
        videoplayer.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoplayer.release()
    }


    override fun onBackPressed() {
        if (!videoplayer.onBackPressed()) {
            super.onBackPressed()
        }
    }
}