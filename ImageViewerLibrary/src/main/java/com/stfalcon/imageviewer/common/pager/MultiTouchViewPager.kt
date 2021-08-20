package com.stfalcon.imageviewer.common.pager

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import com.stfalcon.imageviewer.common.extensions.addOnPageChangeListener

internal class MultiTouchViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    internal var isIdle = true
        private set

    private var isInterceptionDisallowed: Boolean = false
    private var pageChangeListener: ViewPager.OnPageChangeListener? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        pageChangeListener = addOnPageChangeListener(
            onPageScrollStateChanged = ::onPageScrollStateChanged
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pageChangeListener?.let { removeOnPageChangeListener(it) }
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        isInterceptionDisallowed = disallowIntercept
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (ev.pointerCount > 1 && isInterceptionDisallowed) {
            requestDisallowInterceptTouchEvent(false)
            val handled = super.dispatchTouchEvent(ev)
            requestDisallowInterceptTouchEvent(true)
            handled
        } else {
            super.dispatchTouchEvent(ev)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (ev.pointerCount > 1) {
            false
        } else {
            try {
                super.onInterceptTouchEvent(ev)
            } catch (ex: IllegalArgumentException) {
                false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return try {
            super.onTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            false
        }
    }

    private fun onPageScrollStateChanged(state: Int) {
        isIdle = state == ViewPager.SCROLL_STATE_IDLE
    }
}