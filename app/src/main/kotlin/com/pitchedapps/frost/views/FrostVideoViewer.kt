package com.pitchedapps.frost.views

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.inflate
import ca.allanwang.kau.utils.isColorDark
import ca.allanwang.kau.utils.withMinAlpha
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-10-13.
 */
class FrostVideoViewer @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val container: ViewGroup by bindView(R.id.video_container)
    val background: View by bindView(R.id.video_background)
    val video: FrostVideoView by bindView(R.id.video)

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