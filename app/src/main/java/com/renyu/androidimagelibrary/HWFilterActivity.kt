package com.renyu.androidimagelibrary

import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.huawei.hms.image.vision.ImageVision
import com.renyu.commonlibrary.baseact.BaseActivity
import kotlinx.android.synthetic.main.activity_hwfilter.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Random

class HWFilterActivity : BaseActivity() {
    private val allShowMap by lazy {
        HashMap<Int, String>().apply {
            put(1, "黑白")
            put(2, "棕调")
            put(3, "慵懒")
            put(4, "小苍兰")
            put(5, "富士")
            put(6, "桃粉")
            put(7, "海盐")
            put(8, "薄荷")
            put(9, "蒹葭")
            put(10, "复古")
            put(11, "棉花糖")
            put(12, "青苔")
            put(13, "日光")
            put(14, "时光")
            put(15, "雾霾蓝")
            put(16, "向日葵")
            put(17, "硬朗")
            put(18, "古铜黄")
            put(19, "黑白调")
            put(20, "黄绿调")
            put(21, "黄调")
            put(22, "绿调")
            put(23, "青调")
            put(24, "紫调")
        }
    }

    private val imageVisionFilterAPI by lazy { ImageVision.getInstance(this) }

    private val authJsonObject by lazy {
        JSONObject().apply {
            put("projectId", "890086000102058683")
            put("appId", "104543085")
            put(
                "authApiKey",
                "CgB6e3x9j5CXVJdb5b9LSQBr1o06+TaYsgVU9t/+WUSL+7Vep5hGOcW0egZEtusTpCoUAYF+DiyDGY3Q36PcRzyC"
            )
            put("clientSecret", "0E21A01B95BD10823535D3B8A5581CBDD206DAE039B974AD2E46C68F9BB5679C")
            put("clientId", "675748510703830208")
        }
    }

    private val taskJson by lazy {
        JSONObject().apply {
            put("filterType", randomInt)
            put("intensity", 1)
            put("compressRate", 1)
        }
    }

    private val requestJsonObject by lazy {
        JSONObject().apply {
            put("requestId", "1")
            put("taskJson", taskJson)
            put("authJson", authJsonObject)
        }
    }

    private val randomInt by lazy { Random().nextInt(24) + 1 }

    override fun initParams() {
        imageVisionFilterAPI.setVisionCallBack(object : ImageVision.VisionCallBack {
            override fun onSuccess(p0: Int) {
                val initCode = imageVisionFilterAPI.init(this@HWFilterActivity, authJsonObject)
                if (initCode == 0) {
                    ToastUtils.showShort("可以开始玩了")
                }
            }

            override fun onFailure(p0: Int) {

            }
        })

        btn_hwfilter.setOnClickListener {
            doFilter()
        }

        tv_hwfilter.text = allShowMap[randomInt]
    }

    override fun initViews() = R.layout.activity_hwfilter

    override fun loadData() {

    }

    override fun setStatusBarColor() = Color.BLACK

    override fun setStatusBarTranslucent() = 0

    private fun doFilter() {
        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        lifecycleScope.launch(Dispatchers.IO) {
            val visionResult = imageVisionFilterAPI.getColorFilter(requestJsonObject, bitmap)
            withContext(Dispatchers.Main) {
                if (visionResult.resultCode == 0) {
                    val resultBitmap = visionResult.image
                    iv_hwfilter.setImageBitmap(resultBitmap)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageVisionFilterAPI.stop()
    }
}