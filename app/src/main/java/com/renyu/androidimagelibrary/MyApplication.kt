package com.renyu.androidimagelibrary

import androidx.multidex.MultiDexApplication
import com.blankj.utilcode.util.Utils
import com.facebook.drawee.backends.pipeline.Fresco
import me.jessyan.autosize.AutoSizeConfig

/**
 * Created by renyu on 2017/12/11.
 */
class MyApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        Fresco.initialize(this)

        AutoSizeConfig.getInstance().isCustomFragment = true
    }
}