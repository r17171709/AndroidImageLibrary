package com.stfalcon.imageviewer.common.extensions

import me.relex.photodraweeview.PhotoDraweeView

internal fun PhotoDraweeView.resetScale(animate: Boolean) {
    setScale(minimumScale, animate)
}