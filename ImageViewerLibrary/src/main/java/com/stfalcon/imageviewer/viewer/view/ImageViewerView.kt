package com.stfalcon.imageviewer.viewer.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.GestureDetectorCompat
import com.facebook.drawee.view.SimpleDraweeView
import com.stfalcon.imageviewer.R
import com.stfalcon.imageviewer.common.extensions.*
import com.stfalcon.imageviewer.common.gestures.detector.SimpleOnGestureListener
import com.stfalcon.imageviewer.common.gestures.direction.SwipeDirection
import com.stfalcon.imageviewer.common.gestures.direction.SwipeDirection.*
import com.stfalcon.imageviewer.common.gestures.direction.SwipeDirectionDetector
import com.stfalcon.imageviewer.common.gestures.dismiss.SwipeToDismissHandler
import com.stfalcon.imageviewer.common.pager.MultiTouchViewPager
import com.stfalcon.imageviewer.loader.ImageLoader
import com.stfalcon.imageviewer.viewer.adapter.ImagesPagerAdapter
import me.relex.circleindicator.CircleIndicator

internal class ImageViewerView<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    internal var isZoomingAllowed = true
    internal var isSwipeToDismissAllowed = true

    internal var currentPosition: Int
        get() = imagesPager.currentItem
        set(value) {
            imagesPager.currentItem = value
        }

    internal var onDismiss: (() -> Unit)? = null
    internal var onPageChange: ((position: Int) -> Unit)? = null

    internal val isScaled
        get() = imagesAdapter?.isScaled(currentPosition) ?: false

    internal var containerPadding = intArrayOf(0, 0, 0, 0)

    internal var imagesMargin
        get() = imagesPager.pageMargin
        set(value) {
            imagesPager.pageMargin = value
        }

    internal var overlayView: View? = null
        set(value) {
            field = value
            value?.let { rootContainer.addView(it) }
        }

    private var rootContainer: ViewGroup
    private var backgroundView: View
    private var dismissContainer: ViewGroup

    private val transitionImageContainer: FrameLayout
    private val transitionImageView: SimpleDraweeView
    private var externalTransitionImageView: SimpleDraweeView? = null

    private var imagesPager: MultiTouchViewPager
    private var imagesAdapter: ImagesPagerAdapter<T>? = null

    private var imagesIndicator: CircleIndicator

    private var directionDetector: SwipeDirectionDetector
    private var gestureDetector: GestureDetectorCompat
    private var scaleDetector: ScaleGestureDetector
    private lateinit var swipeDismissHandler: SwipeToDismissHandler

    private var wasScaled: Boolean = false
    private var wasDoubleTapped = false
    private var isOverlayWasClicked: Boolean = false
    private var swipeDirection: SwipeDirection? = null

    private var images: List<T> = listOf()
    private var imageLoader: ImageLoader<T>? = null
    private lateinit var transitionImageAnimator: TransitionImageAnimator

    private var startPosition: Int = 0
        set(value) {
            field = value
            currentPosition = value
        }

    private val shouldDismissToBottom: Boolean
        get() = externalTransitionImageView == null
                || !externalTransitionImageView.isRectVisible
                || !isAtStartPosition

    private val isAtStartPosition: Boolean
        get() = currentPosition == startPosition

    init {
        View.inflate(context, R.layout.view_image_viewer, this)

        rootContainer = findViewById(R.id.rootContainer)
        backgroundView = findViewById(R.id.backgroundView)
        dismissContainer = findViewById(R.id.dismissContainer)

        transitionImageContainer = findViewById(R.id.transitionImageContainer)
        transitionImageView = findViewById(R.id.transitionImageView)

        imagesPager = findViewById(R.id.imagesPager)
        imagesPager.addOnPageChangeListener(
            onPageSelected = {
                externalTransitionImageView?.apply {
                    if (isAtStartPosition) makeInvisible() else makeVisible()
                }
                onPageChange?.invoke(it)
            })

        imagesIndicator = findViewById(R.id.imagesIndicator)

        directionDetector = createSwipeDirectionDetector()
        gestureDetector = createGestureDetector()
        scaleDetector = createScaleGestureDetector()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (overlayView.isVisible && overlayView?.dispatchTouchEvent(event) == true) {
            return true
        }

        if (!this::transitionImageAnimator.isInitialized || transitionImageAnimator.isAnimating) {
            return true
        }

        //one more tiny kludge to prevent single tap a one-finger zoom which is broken by the SDK
        if (wasDoubleTapped &&
            event.action == MotionEvent.ACTION_MOVE &&
            event.pointerCount == 1
        ) {
            return true
        }

        handleUpDownEvent(event)

        if (swipeDirection == null && (scaleDetector.isInProgress || event.pointerCount > 1 || wasScaled)) {
            wasScaled = true
            return imagesPager.dispatchTouchEvent(event)
        }

        return if (isScaled) super.dispatchTouchEvent(event) else handleTouchIfNotScaled(event)
    }

    override fun setBackgroundColor(color: Int) {
        findViewById<View>(R.id.backgroundView).setBackgroundColor(color)
    }

    internal fun setImages(images: List<T>, startPosition: Int, imageLoader: ImageLoader<T>) {
        this.images = images
        this.imageLoader = imageLoader
        this.imagesAdapter = ImagesPagerAdapter(context, images, imageLoader, isZoomingAllowed)
        this.imagesPager.adapter = imagesAdapter
        this.startPosition = startPosition

        imagesIndicator.setViewPager(imagesPager)
    }

    internal fun open(transitionImageView: SimpleDraweeView?, animate: Boolean) {
        prepareViewsForTransition()

        externalTransitionImageView = transitionImageView

        imageLoader?.loadImagePreview(this.transitionImageView, images[startPosition])
        this.transitionImageView.copyBitmapFrom(transitionImageView)

        transitionImageAnimator = createTransitionImageAnimator(transitionImageView)
        swipeDismissHandler = createSwipeToDismissHandler()
        rootContainer.setOnTouchListener(swipeDismissHandler)

        if (animate) animateOpen() else prepareViewsForViewer()
    }

    internal fun close() {
        if (shouldDismissToBottom) {
            swipeDismissHandler.initiateDismissToBottom()
        } else {
            animateClose()
        }
    }

    internal fun updateImages(images: List<T>) {
        this.images = images
        imagesAdapter?.updateImages(images)
    }

    internal fun updateTransitionImage(imageView: SimpleDraweeView?) {
        externalTransitionImageView?.makeVisible()
        imageView?.makeInvisible()

        externalTransitionImageView = imageView
        startPosition = currentPosition
        transitionImageAnimator = createTransitionImageAnimator(imageView)
        imageLoader?.loadImagePreview(transitionImageView, images[startPosition])
    }

    internal fun resetScale() {
        imagesAdapter?.resetScale(currentPosition)
    }

    private fun animateOpen() {
        transitionImageAnimator.animateOpen(
            containerPadding = containerPadding,
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(0f, 1f, duration)
                overlayView?.animateAlpha(0f, 1f, duration)
            },
            onTransitionEnd = { prepareViewsForViewer() })
    }

    private fun animateClose() {
        prepareViewsForTransition()
        dismissContainer.applyMargin(0, 0, 0, 0)

        transitionImageAnimator.animateClose(
            shouldDismissToBottom = shouldDismissToBottom,
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(backgroundView.alpha, 0f, duration)
                overlayView?.animateAlpha(overlayView?.alpha, 0f, duration)
            },
            onTransitionEnd = { onDismiss?.invoke() })
    }

    private fun prepareViewsForTransition() {
        transitionImageContainer.makeVisible()
        imagesPager.makeGone()
    }

    private fun prepareViewsForViewer() {
        backgroundView.alpha = 1f
        transitionImageContainer.makeGone()
        imagesPager.makeVisible()
    }

    private fun handleTouchIfNotScaled(event: MotionEvent): Boolean {
        directionDetector.handleTouchEvent(event)

        return when (swipeDirection) {
            UP, DOWN -> {
                if (isSwipeToDismissAllowed && !wasScaled && imagesPager.isIdle) {
                    swipeDismissHandler.onTouch(rootContainer, event)
                } else true
            }
            LEFT, RIGHT -> {
                imagesPager.dispatchTouchEvent(event)
            }
            else -> true
        }
    }

    private fun handleUpDownEvent(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP) {
            handleEventActionUp(event)
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            handleEventActionDown(event)
        }

        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
    }

    private fun handleEventActionDown(event: MotionEvent) {
        swipeDirection = null
        wasScaled = false
        imagesPager.dispatchTouchEvent(event)

        swipeDismissHandler.onTouch(rootContainer, event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleEventActionUp(event: MotionEvent) {
        wasDoubleTapped = false
        swipeDismissHandler.onTouch(rootContainer, event)
        imagesPager.dispatchTouchEvent(event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleSingleTap(event: MotionEvent, isOverlayWasClicked: Boolean) {
        if (overlayView != null && !isOverlayWasClicked) {
            overlayView?.switchVisibilityWithAnimation()
            super.dispatchTouchEvent(event)
        }
    }

    private fun handleSwipeViewMove(translationY: Float, translationLimit: Int) {
        val alpha = calculateTranslationAlpha(translationY, translationLimit)
        backgroundView.alpha = alpha
        overlayView?.alpha = alpha
    }

    private fun dispatchOverlayTouch(event: MotionEvent): Boolean =
        overlayView
            ?.let { it.isVisible && it.dispatchTouchEvent(event) }
            ?: false

    private fun calculateTranslationAlpha(translationY: Float, translationLimit: Int): Float =
        1.0f - 1.0f / translationLimit.toFloat() / 4f * Math.abs(translationY)

    private fun createSwipeDirectionDetector() =
        SwipeDirectionDetector(context) { swipeDirection = it }

    private fun createGestureDetector() =
        GestureDetectorCompat(context, SimpleOnGestureListener(
            onSingleTap = {
                if (imagesPager.isIdle) {
                    handleSingleTap(it, isOverlayWasClicked)
                }
                false
            },
            onDoubleTap = {
                wasDoubleTapped = !isScaled
                false
            }
        ))

    private fun createScaleGestureDetector() =
        ScaleGestureDetector(context, ScaleGestureDetector.SimpleOnScaleGestureListener())

    private fun createSwipeToDismissHandler()
            : SwipeToDismissHandler = SwipeToDismissHandler(
        swipeView = dismissContainer,
        shouldAnimateDismiss = { shouldDismissToBottom },
        onDismiss = { animateClose() },
        onSwipeViewMove = ::handleSwipeViewMove
    )

    private fun createTransitionImageAnimator(transitionImageView: ImageView?) =
        TransitionImageAnimator(
            externalImage = transitionImageView,
            internalImage = this.transitionImageView,
            internalImageContainer = this.transitionImageContainer
        )
}