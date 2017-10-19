package com.pitchedapps.frost.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.parentViewGroup
import ca.allanwang.kau.utils.scaleXY
import com.devbrackets.android.exomedia.ui.widget.VideoView
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-10-13.
 *
 * VideoView with scalability
 * Parent must have layout with both height & width as match_parent
 */
class FrostVideoView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : VideoView(context, attrs, defStyleAttr) {

    /**
     * Shortcut for actual video view
     */
    private inline val v
        get() = videoViewImpl

    var backgroundView: View? = null
    var onFinishedListener: () -> Unit = {}

    private val videoDimensions = PointF(0f, 0f)

    val parent: ViewGroup by lazy { parentViewGroup }

    companion object {

        /**
         * Padding between minimized video and the parent borders
         * Note that this is double the actual padding
         * as we are calculating then dividing by 2
         */
        private val MINIMIZED_PADDING = 10.dpToPx
        private val SWIPE_TO_CLOSE_HORIZONTAL_THRESHOLD = 2f.dpToPx
        private val SWIPE_TO_CLOSE_VERTICAL_THRESHOLD = 5f.dpToPx
        private val SWIPE_TO_CLOSE_OFFSET_THRESHOLD = 75f.dpToPx
        val ANIMATION_DURATION = 300L
        private val FAST_ANIMATION_DURATION = 100L
    }

    private var upperMinimizedX = 0f
    private var upperMinimizedY = 0f

    var isExpanded: Boolean = true
        set(value) {
            if (field == value) return
            if (videoDimensions.x <= 0f || videoDimensions.y <= 0f)
                return L.d("Attempted to toggle video expansion when points have not been finalized")
            field = value
            if (field) {
                animate().scaleXY(1f).translationX(0f).translationY(0f).setDuration(ANIMATION_DURATION).withStartAction {
                    backgroundView?.animate()?.alpha(1f)?.setDuration(ANIMATION_DURATION)
                }
            } else {
                hideControls()
                val height = parent.height
                val width = parent.width
                val scale = Math.min(height / 4f / videoDimensions.y, width / 2.3f / videoDimensions.x)
                val desiredHeight = scale * videoDimensions.y
                val desiredWidth = scale * videoDimensions.x
                val translationX = (width - MINIMIZED_PADDING - desiredWidth) / 2
                val translationY = (height - MINIMIZED_PADDING - desiredHeight) / 2
                upperMinimizedX = width - desiredWidth - MINIMIZED_PADDING
                upperMinimizedY = height - desiredHeight - MINIMIZED_PADDING
                animate().scaleXY(scale).translationX(translationX).translationY(translationY).setDuration(ANIMATION_DURATION).withStartAction {
                    backgroundView?.animate()?.alpha(0f)?.setDuration(ANIMATION_DURATION)
                }
            }
        }

    init {
        setOnPreparedListener {
            start()
            isExpanded = true
            showControls()
        }
        setOnCompletionListener {
            restart() //todo add restart button
        }
        setOnTouchListener(FrameTouchListener(context))
        v.setOnTouchListener(VideoTouchListener(context))
        setOnVideoSizedChangedListener { intrinsicWidth, intrinsicHeight ->
            //the textureview bases its dimensions from its parent. We have to calculate it manually
            val ratio = Math.min(parent.width.toFloat() / intrinsicWidth, parent.height.toFloat() / intrinsicHeight.toFloat())
            videoDimensions.set(ratio * intrinsicWidth, ratio * intrinsicHeight)
        }
    }

    private fun hideControls() {
        if (videoControls?.isVisible == true)
            videoControls?.hide()
    }

    private fun toggleControls() {
        if (videoControls?.isVisible == true)
            hideControls()
        else
            showControls()
    }

    fun shouldParentAcceptTouch(ev: MotionEvent): Boolean {
        if (isExpanded) return true
        return ev.x >= upperMinimizedX && ev.y >= upperMinimizedY
    }

    fun destroy() {
        stopPlayback()
        if (parent.alpha > 0f)
            parent.animate().alpha(0f).setDuration(FAST_ANIMATION_DURATION).withEndAction { onFinishedListener() }.start()
        else
            onFinishedListener()
    }

    private fun onHorizontalSwipe(offset: Float) {
        val alpha = Math.max((1f - Math.abs(offset / SWIPE_TO_CLOSE_OFFSET_THRESHOLD)) * 0.5f + 0.5f, 0f)
        parent.alpha = alpha
    }

    /*
     * -------------------------------------------------------------------
     * Touch Listeners
     * -------------------------------------------------------------------
     */

    private inner class FrameTouchListener(context: Context) : GestureDetector.SimpleOnGestureListener(), View.OnTouchListener {

        private val gestureDetector: GestureDetector = GestureDetector(context, this)

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (!isExpanded) return false
            gestureDetector.onTouchEvent(event)
            return true
        }

        override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
            toggleControls()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            isExpanded = !isExpanded
            return true
        }
    }

    /**
     * Monitors the view click events to show and hide the video controls if they have been specified.
     */
    private inner class VideoTouchListener(context: Context) : GestureDetector.SimpleOnGestureListener(), View.OnTouchListener {

        private val gestureDetector: GestureDetector = GestureDetector(context, this)
        private val downLoc = PointF()
        private var baseSwipeX = -1f
        private var baseTranslateX = -1f
        private var checkForDismiss = true
        private var onSwipe = false

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            gestureDetector.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    checkForDismiss = !isExpanded
                    onSwipe = false
                    downLoc.x = event.rawX
                    downLoc.y = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    if (onSwipe) {
                        val dx = baseSwipeX - event.rawX
                        translationX = baseTranslateX - dx
                        onHorizontalSwipe(dx)
                    } else if (checkForDismiss) {
                        if (Math.abs(event.rawY - downLoc.y) > SWIPE_TO_CLOSE_VERTICAL_THRESHOLD)
                            checkForDismiss = false
                        else if (Math.abs(event.rawX - downLoc.x) > SWIPE_TO_CLOSE_HORIZONTAL_THRESHOLD) {
                            onSwipe = true
                            baseSwipeX = event.rawX
                            baseTranslateX = translationX
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (onSwipe) {
                        if (Math.abs(baseSwipeX - event.rawX) > SWIPE_TO_CLOSE_OFFSET_THRESHOLD)
                            destroy()
                        else
                            animate().translationX(baseTranslateX).setDuration(FAST_ANIMATION_DURATION).withStartAction {
                                parent.animate().alpha(1f)
                            }
                    }
                }
            }
            return true
        }

        override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
            if (!isExpanded) {
                isExpanded = true
                return true
            }
            toggleControls()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            isExpanded = !isExpanded
            return true
        }
    }
}