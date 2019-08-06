/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import ca.allanwang.kau.ui.ProgressAnimator
import ca.allanwang.kau.utils.AnimHolder
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.scaleXY
import ca.allanwang.kau.utils.toast
import com.devbrackets.android.exomedia.ui.widget.VideoControls
import com.devbrackets.android.exomedia.ui.widget.VideoView
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.utils.L
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Allan Wang on 2017-10-13.
 *
 * VideoView with scalability
 * Parent must have layout with both height & width as match_parent
 */
class FrostVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : VideoView(context, attrs, defStyleAttr) {

    /**
     * Shortcut for actual video view
     */
    private inline val v
        get() = videoViewImpl

    var onFinishedListener: () -> Unit = {}
    private lateinit var viewerContract: FrostVideoViewerContract
    lateinit var containerContract: FrostVideoContainerContract
    var repeat: Boolean = false

    private val videoDimensions = PointF(0f, 0f)

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
        const val ANIMATION_DURATION = 200L
        private const val FAST_ANIMATION_DURATION = 100L
    }

    private var videoBounds = RectF()

    var isExpanded: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            val origX = translationX
            val origY = translationY
            val origScale = scaleX
            if (field) {
                ProgressAnimator.ofFloat {
                    duration = ANIMATION_DURATION
                    interpolator = AnimHolder.fastOutSlowInInterpolator(context)
                    withAnimator { viewerContract.onExpand(it) }
                    withAnimator(origScale, 1f) { scaleXY = it }
                    withAnimator(origX, 0f) { translationX = it }
                    withAnimator(origY, 0f) { translationY = it }
                    withEndAction {
                        if (!isPlaying) showControls()
                        else viewerContract.onControlsHidden()
                    }
                }.start()
            } else {
                hideControls()
                val (scale, tX, tY) = mapBounds()
                ProgressAnimator.ofFloat {
                    duration = ANIMATION_DURATION
                    interpolator = AnimHolder.fastOutSlowInInterpolator(context)
                    withAnimator { viewerContract.onExpand(1f - it) }
                    withAnimator(origScale, scale) { scaleXY = it }
                    withAnimator(origX, tX) { translationX = it }
                    withAnimator(origY, tY) { translationY = it }
                }.start()
            }
        }

    /**
     * Store the boundaries of the minimized video,
     * and return the necessary transitions to get there
     */
    private fun mapBounds(): Triple<Float, Float, Float> {
        if (videoDimensions.x <= 0f || videoDimensions.y <= 0f) {
            L.d { "Attempted to toggle video expansion when points have not been finalized" }
            val dimen = min(height, width).toFloat()
            videoDimensions.set(dimen, dimen)
        }
        val portrait = height > width
        val scale = min(
            height / (if (portrait) 4f else 2.3f) / videoDimensions.y,
            width / (if (portrait) 2.3f else 4f) / videoDimensions.x
        )
        val desiredHeight = scale * videoDimensions.y
        val desiredWidth = scale * videoDimensions.x
        val padding = containerContract.lowerVideoPadding
        val offsetX = width - MINIMIZED_PADDING - desiredWidth
        val offsetY = height - MINIMIZED_PADDING - desiredHeight
        val tX = offsetX / 2 - padding.x
        val tY = offsetY / 2 - padding.y
        videoBounds.set(offsetX, offsetY, width.toFloat(), height.toFloat())
        videoBounds.offset(padding.x, padding.y)
        L.v { "Video bounds: fullwidth $width, fullheight $height, scale $scale, tX $tX, tY $tY" }
        return Triple(scale, tX, tY)
    }

    fun updateLocation() {
        L.d { "Update video location" }
        val (scale, tX, tY) = if (isExpanded) Triple(1f, 0f, 0f) else mapBounds()
        scaleXY = scale
        translationX = tX
        translationY = tY
    }

    init {
        setOnPreparedListener {
            start()
            if (isExpanded) showControls()
        }
        setOnErrorListener {
            L.e(it) { "Failed to load video ${videoUri?.toString()?.formattedFbUrl}" }
            toast(R.string.video_load_failed, Toast.LENGTH_SHORT)
            destroy()
            true
        }
        setOnCompletionListener {
            if (repeat) restart()
            else viewerContract.onVideoComplete()
        }
        setOnTouchListener(FrameTouchListener(context))
        v.setOnTouchListener(VideoTouchListener(context))
        setOnVideoSizedChangedListener { intrinsicWidth, intrinsicHeight, pixelWidthHeightRatio ->
            // todo use provided ratio?
            val ratio =
                min(width.toFloat() / intrinsicWidth, height.toFloat() / intrinsicHeight.toFloat())
            /**
             * Only remap if not expanded and if dimensions have changed
             */
            val shouldRemap = !isExpanded &&
                (videoDimensions.x != ratio * intrinsicWidth || videoDimensions.y != ratio * intrinsicHeight)
            videoDimensions.set(ratio * intrinsicWidth, ratio * intrinsicHeight)
            if (shouldRemap) updateLocation()
        }
    }

    fun setViewerContract(contract: FrostVideoViewerContract) {
        this.viewerContract = contract
        (videoControls as? VideoControls)?.setVisibilityListener(viewerContract)
    }

    fun jumpToStart() {
        pause()
        v.seekTo(0)
        videoControls?.finishLoading()
    }

    override fun pause() {
        audioFocusHelper.abandonFocus()
        videoViewImpl.pause()
        keepScreenOn = false
        if (isExpanded)
            videoControls?.updatePlaybackState(false)
    }

    override fun restart(): Boolean {
        videoUri ?: return false
        if (videoViewImpl.restart() && isExpanded && !repeat) {
            videoControls?.showLoading(true)
            return true
        }
        return false
    }

    private fun hideControls() {
        if (videoControls?.isVisible == true)
            videoControls?.hide(false)
    }

    private fun toggleControls() {
        if (videoControls?.isVisible == true)
            hideControls()
        else
            showControls()
    }

    fun shouldParentAcceptTouch(ev: MotionEvent): Boolean {
        if (isExpanded) return true
        return !videoBounds.contains(ev.x, ev.y)
    }

    fun destroy() {
        stopPlayback()
        if (alpha > 0f)
            ProgressAnimator.ofFloat {
                duration = FAST_ANIMATION_DURATION
                withAnimator(alpha, 0f) { alpha = it }
                withEndAction { onFinishedListener() }
            }.start()
        else
            onFinishedListener()
    }

    private fun onHorizontalSwipe(offset: Float) {
        val alpha =
            max((1f - abs(offset / SWIPE_TO_CLOSE_OFFSET_THRESHOLD)) * 0.5f + 0.5f, 0f)
        this.alpha = alpha
    }

    /*
     * -------------------------------------------------------------------
     * Touch Listeners
     * -------------------------------------------------------------------
     */

    private inner class FrameTouchListener(context: Context) :
        GestureDetector.SimpleOnGestureListener(),
        View.OnTouchListener {

        private val gestureDetector: GestureDetector = GestureDetector(context, this)

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (!isExpanded) return false
            gestureDetector.onTouchEvent(event)
            return true
        }

        override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
            if (!viewerContract.onSingleTapConfirmed(event))
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
    private inner class VideoTouchListener(context: Context) :
        GestureDetector.SimpleOnGestureListener(),
        View.OnTouchListener {

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
                        if (abs(event.rawY - downLoc.y) > SWIPE_TO_CLOSE_VERTICAL_THRESHOLD)
                            checkForDismiss = false
                        else if (abs(event.rawX - downLoc.x) > SWIPE_TO_CLOSE_HORIZONTAL_THRESHOLD) {
                            onSwipe = true
                            baseSwipeX = event.rawX
                            baseTranslateX = translationX
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (onSwipe) {
                        if (abs(baseSwipeX - event.rawX) > SWIPE_TO_CLOSE_OFFSET_THRESHOLD)
                            destroy()
                        else
                            animate().translationX(baseTranslateX).setDuration(
                                FAST_ANIMATION_DURATION
                            ).withStartAction {
                                animate().alpha(1f)
                            }
                    }
                }
            }
            return true
        }

        override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
            if (viewerContract.onSingleTapConfirmed(event)) return true
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
