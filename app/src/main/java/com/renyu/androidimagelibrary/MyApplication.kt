package com.renyu.androidimagelibrary

import android.app.Application
import com.blankj.utilcode.util.Utils
import com.facebook.drawee.backends.pipeline.Fresco

/**
 * Created by renyu on 2017/12/11.
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        Fresco.initialize(this)
    }
}