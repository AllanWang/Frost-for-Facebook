package com.pitchedapps.frost.views

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import ca.allanwang.kau.utils.*
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostDownload

/**
 * Created by Allan Wang on 2017-10-13.
 */
class FrostVideoViewer @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), FrostVideoViewerContract {

    override fun onFade(alpha: Float, duration: Long) {
        toolbar.animate().alpha(alpha).setDuration(duration)
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        if (restarter.isVisible) {
            restarter.performClick()
            return true
        }
        return false
    }

    override fun onVideoComplete() {
        video.jumpToStart()
        restarter.fadeIn()
    }

    val container: ViewGroup by bindView(R.id.video_container)
    val toolbar: Toolbar by bindView(R.id.video_toolbar)
    val background: View by bindView(R.id.video_background)
    val video: FrostVideoView by bindView(R.id.video)
    val restarter: ImageView by bindView(R.id.video_restart)


    companion object {
        /**
         * Simplified binding to add video to layout, and remove it when finished
         * This is under the assumption that the container allows for overlays,
         * such as a FrameLayout
         */
        inline fun showVideo(container: ViewGroup, url: String, crossinline onFinish: () -> Unit): FrostVideoViewer {
            val videoViewer = FrostVideoViewer(container.context)
            container.addView(videoViewer)
            videoViewer.bringToFront()
            L.d("Create video view", url)
            videoViewer.setVideo(url)
            videoViewer.video.onFinishedListener = { container.removeView(videoViewer); onFinish() }
            return videoViewer
        }
    }

    init {
        inflate(R.layout.view_video, true)
        alpha = 0f
        background.setBackgroundColor(if (Prefs.bgColor.isColorDark) Prefs.bgColor.withMinAlpha(200) else Color.BLACK)
        video.backgroundView = background
        video.viewerContract = this
        video.pause()
        toolbar.inflateMenu(R.menu.menu_video)
        toolbar.setBackgroundColor(Prefs.headerColor)
        context.setMenuIcons(toolbar.menu, Prefs.iconColor,
                R.id.action_pip to GoogleMaterial.Icon.gmd_picture_in_picture_alt,
                R.id.action_download to GoogleMaterial.Icon.gmd_file_download
        )
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_pip -> video.isExpanded = false
                R.id.action_download -> context.frostDownload(video.videoUri)
            }
            true
        }
        restarter.gone().setIcon(GoogleMaterial.Icon.gmd_replay, 64)
        restarter.setOnClickListener {
            video.restart()
            restarter.fadeOut { restarter.gone() }
        }
    }

    fun setVideo(url: String) {
        animate().alpha(1f).setDuration(FrostVideoView.ANIMATION_DURATION).start()
        video.setVideoURI(Uri.parse(url.formattedFbUrl))
    }

    /**
     * Handle back presses
     * returns true if consumed, false otherwise
     */
    fun onBackPressed(): Boolean {
        if (video.isExpanded) {
            video.isExpanded = false
            return true
        }
        return false
    }

    fun pause() = video.pause()

}

interface FrostVideoViewerContract {
    fun onSingleTapConfirmed(event: MotionEvent): Boolean
    fun onFade(alpha: Float, duration: Long)
    fun onVideoComplete()
}