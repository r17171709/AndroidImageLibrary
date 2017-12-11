package com.renyu.androidimagelibrary.view

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.renyu.androidimagelibrary.R

/**
 * Created by renyu on 2017/12/11.
 */
class UploadView : RelativeLayout {

    var view_mask: View? = null
    var iv_releasehousepic: SimpleDraweeView? = null
    var tv_releasehousepic: TextView? = null
    var iv_releasehousepic_delete: ImageView? = null
    var iv_releasehousepic_cover: TextView? = null

    interface OnUIControllListener {
        fun deletePic()
        fun clickPic()
    }
    var listener: OnUIControllListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr : Int) : super(context, attrs, defStyleAttr)

    override fun onFinishInflate() {
        super.onFinishInflate()

        view_mask = findViewById(R.id.view_mask)
        iv_releasehousepic = findViewById(R.id.iv_releasehousepic)
        tv_releasehousepic = findViewById(R.id.tv_releasehousepic)
        iv_releasehousepic_delete = findViewById(R.id.iv_releasehousepic_delete)
        iv_releasehousepic_cover = findViewById(R.id.iv_releasehousepic_cover)

        iv_releasehousepic_delete?.setOnClickListener {
            listener?.deletePic()
        }
        iv_releasehousepic?.setOnClickListener {
            listener?.clickPic()
        }
    }

    fun loadPic(path: String) {
        if (TextUtils.isEmpty(path)) {
            view_mask?.visibility = View.GONE
            return
        }
        val requestBuilder: ImageRequestBuilder? = if (path.indexOf("http")!=-1) {
            ImageRequestBuilder.newBuilderWithSource(Uri.parse(path))
        }
        else {
            ImageRequestBuilder.newBuilderWithSource(Uri.parse("file://"+path))
        }
        val imageRequest: ImageRequest? = requestBuilder?.setResizeOptions(ResizeOptions(SizeUtils.dp2px(90f), SizeUtils.dp2px(90f)))?.build()
        val draweeController: DraweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest).setAutoPlayAnimations(true).build()
        view_mask?.visibility = View.VISIBLE
        iv_releasehousepic?.controller = draweeController
        tv_releasehousepic?.visibility = View.GONE
        iv_releasehousepic_delete?.visibility = View.VISIBLE
        // 设置标签
        tag = "$path"
    }

    fun updateMaskPercent(percent: Int) {
        view_mask?.layoutParams?.height = (measuredHeight * ((100-percent)*1.0f/100)).toInt()
        view_mask?.requestLayout()
    }
}