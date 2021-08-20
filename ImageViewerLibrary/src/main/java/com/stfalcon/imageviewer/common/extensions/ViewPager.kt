package com.stfalcon.imageviewer.common.extensions

import androidx.viewpager.widget.ViewPager

internal fun ViewPager.addOnPageChangeListener(
    onPageScrolled: ((position: Int, offset: Float, offsetPixels: Int) -> Unit)? = null,
    onPageSelected: ((position: Int) -> Unit)? = null,
    onPageScrollStateChanged: ((state: Int) -> Unit)? = null
) = object : ViewPager.OnPageChangeListener {
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        onPageScrolled?.invoke(position, positionOffset, positionOffsetPixels)
    }

    override fun onPageSelected(position: Int) {
        onPageSelected?.invoke(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
        onPageScrollStateChanged?.invoke(state)
    }
}.also { addOnPageChangeListener(it) }