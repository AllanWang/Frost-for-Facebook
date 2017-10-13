package com.pitchedapps.frost.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import ca.allanwang.kau.utils.*
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.formattedFbUrl
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
        fun showVideo(container: ViewGroup, url: String): FrostVideoViewer {
            val videoViewer = FrostVideoViewer(container.context)
            container.addView(videoViewer)
            videoViewer.setVideo(url)
            return videoViewer
        }
    }

    init {
        inflate(R.layout.view_video, true)
        gone()
        background.setBackgroundColor(Prefs.bgColor.withMinAlpha(200))
        video.backgroundView = background
    }

    fun setVideo(url: String) {
        fadeIn()
        video.setVideoURI(Uri.parse(url.formattedFbUrl))
    }

    fun pause() = video.pause()

}