package com.pitchedapps.frost.views

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import ca.allanwang.kau.utils.*
import com.devbrackets.android.exomedia.listener.VideoControlsVisibilityListener
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostDownload
import kotlinx.android.synthetic.main.view_video.view.*

/**
 * Created by Allan Wang on 2017-10-13.
 */
class FrostVideoViewer @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), FrostVideoViewerContract {

    companion object {
        /**
         * Matches VideoControls.CONTROL_VISIBILITY_ANIMATION_LENGTH
         */
        private const val CONTROL_ANIMATION_DURATION = 300L

        /**
         * Simplified binding to add video to layout, and remove it when finished
         * This is under the assumption that the container allows for overlays,
         * such as a FrameLayout
         */
        fun showVideo(url: String, repeat: Boolean, contract: FrostVideoContainerContract): FrostVideoViewer {
            val container = contract.videoContainer
            val videoViewer = FrostVideoViewer(container.context)
            container.addView(videoViewer)
            videoViewer.bringToFront()
            videoViewer.setVideo(url, repeat)
            videoViewer.video.containerContract = contract
            videoViewer.video.onFinishedListener = { container.removeView(videoViewer); contract.onVideoFinished() }
            return videoViewer
        }
    }

    init {
        inflate(R.layout.view_video, true)
        alpha = 0f
        video_background.setBackgroundColor(
                if (!Prefs.blackMediaBg && Prefs.bgColor.isColorDark)
                    Prefs.bgColor.withMinAlpha(200)
                else
                    Color.BLACK)
        video.setViewerContract(this)
        video.pause()
        video_toolbar.inflateMenu(R.menu.menu_video)
        context.setMenuIcons(video_toolbar.menu, Prefs.iconColor,
                R.id.action_pip to GoogleMaterial.Icon.gmd_picture_in_picture_alt,
                R.id.action_download to GoogleMaterial.Icon.gmd_file_download
        )
        video_toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_pip -> video.isExpanded = false
                R.id.action_download -> context.frostDownload(video.videoUri)
            }
            true
        }
        video_restart.gone().setIcon(GoogleMaterial.Icon.gmd_replay, 64)
        video_restart.setOnClickListener {
            video.restart()
            video_restart.fadeOut { video_restart.gone() }
        }
    }

    fun setVideo(url: String, repeat: Boolean = false) {
        L.d { "Load video; repeat: $repeat" }
        L._d { "Video Url: $url" }
        animate().alpha(1f).setDuration(FrostVideoView.ANIMATION_DURATION).start()
        video.setVideoURI(Uri.parse(url))
        video.repeat = repeat
    }

    /**
     * Handle back presses
     * returns true if consumed, false otherwise
     */
    fun onBackPressed(): Boolean {
        parent ?: return false
        if (video.isExpanded)
            video.isExpanded = false
        else
            video.destroy()
        return true
    }

    fun pause() = video.pause()

    /*
     * -------------------------------------------------------------
     * FrostVideoViewerContract
     * -------------------------------------------------------------
     */

    override fun onExpand(progress: Float) {
        video_toolbar.goneIf(progress == 0f).alpha = progress
        video_background.alpha = progress
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        if (video_restart.isVisible) {
            video_restart.performClick()
            return true
        }
        return false
    }

    override fun onVideoComplete() {
        video.jumpToStart()
        video_restart.fadeIn()
    }

    fun updateLocation() {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                video.updateLocation()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    override fun onControlsShown() {
        if (video.isExpanded)
            video_toolbar.fadeIn(duration = CONTROL_ANIMATION_DURATION, onStart = { video_toolbar.visible() })
    }

    override fun onControlsHidden() {
        if (!video_toolbar.isGone)
            video_toolbar.fadeOut(duration = CONTROL_ANIMATION_DURATION) { video_toolbar.gone() }
    }

}

interface FrostVideoViewerContract : VideoControlsVisibilityListener {
    fun onSingleTapConfirmed(event: MotionEvent): Boolean
    /**
     * Process of expansion
     * 1f represents an expanded view, 0f represents a minimized view
     */
    fun onExpand(progress: Float)

    fun onVideoComplete()
}

interface FrostVideoContainerContract {
    /**
     * Returns extra padding to be added
     * from the right and from the bottom respectively
     */
    val lowerVideoPadding: PointF

    /**
     * Get the container which will hold the video viewer
     */
    val videoContainer: FrameLayout

    /**
     * Called once the video has stopped & should be removed
     */
    fun onVideoFinished()
}