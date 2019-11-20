package com.renyu.androidimagelibrary

import androidx.multidex.MultiDexApplication
import com.blankj.utilcode.util.Utils
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.fresco.FrescoImageLoader
import me.jessyan.autosize.AutoSizeConfig

/**
 * Created by renyu on 2017/12/11.
 */
class MyApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)

        BigImageViewer.initialize(FrescoImageLoader.with(this))

        AutoSizeConfig.getInstance().isCustomFragment = true
    }
}