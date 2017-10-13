package com.pitchedapps.frost.views

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.parentViewGroup
import ca.allanwang.kau.utils.scaleXY
import com.devbrackets.android.exomedia.ui.widget.VideoView
import com.pitchedapps.frost.facebook.formattedFbUrl

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

    val parent: ViewGroup by lazy { parentViewGroup }
    /**
     * Padding between minimized video and the parent borders
     * Note that this is double the actual padding
     * as we are calculating then dividing by 2
     */
    private val MINIMIZED_PADDING = 10.dpToPx

    private var upperMinimizedX = 0f
    private var upperMinimizedY = 0f

    var isExpanded: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            if (field) {
                animate().scaleXY(1f).translationX(0f).translationY(0f).withStartAction {
                    backgroundView?.animate()?.alpha(1f)
                }
            } else {
                hideControls()
                val height = parent.height
                val width = parent.width
                val scale = Math.min(height / 4f / v.height, width / 2.3f / v.width)
                val desiredHeight = scale * v.height
                val desiredWidth = scale * v.width
                val translationX = (width - MINIMIZED_PADDING - desiredWidth) / 2
                val translationY = (height - MINIMIZED_PADDING - desiredHeight) / 2
                upperMinimizedX = width - desiredWidth - MINIMIZED_PADDING
                upperMinimizedY = height - desiredHeight - MINIMIZED_PADDING
                animate().scaleXY(scale).translationX(translationX).translationY(translationY).withStartAction {
                    backgroundView?.animate()?.alpha(0f)
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
    }

    fun setVideo(url: String)
            = setVideoURI(Uri.parse(url.formattedFbUrl))

    fun hideControls() {
        if (videoControls?.isVisible == true)
            videoControls?.hide()
    }

    fun toggleControls() {
        if (videoControls?.isVisible == true)
            hideControls()
        else
            showControls()
    }

    fun shouldParentAcceptTouch(ev: MotionEvent): Boolean {
        if (isExpanded) return true
        return ev.x >= upperMinimizedX && ev.y >= upperMinimizedY
    }

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

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            gestureDetector.onTouchEvent(event)
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