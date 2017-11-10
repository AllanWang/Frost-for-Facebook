package com.pitchedapps.frost.contracts

import android.app.Activity
import android.widget.FrameLayout
import ca.allanwang.kau.utils.inflate
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.views.FrostVideoContainerContract
import com.pitchedapps.frost.views.FrostVideoViewer

/**
 * Created by Allan Wang on 2017-11-10.
 */
interface VideoViewerContract : FrameWrapper, FrostVideoContainerContract {

    var videoViewer: FrostVideoViewer?

    /**
     * Create new viewer and reuse existing one
     * The url will be formatted upon loading
     */
    fun showVideo(url: String) {
        if (videoViewer != null)
            videoViewer?.setVideo(url)
        else
            videoViewer = FrostVideoViewer.showVideo(url, this)
    }

    fun videoOnStop() = videoViewer?.pause()

    fun videoOnBackPress() = videoViewer?.onBackPressed() ?: false

    override val videoContainer: FrameLayout
        get() = frameWrapper

    override fun onVideoFinished() {
        L.d("Video view released")
        videoViewer = null
    }
}

interface FrameWrapper {

    val frameWrapper: FrameLayout

    fun Activity.setFrameContentView(layoutRes: Int) {
        setContentView(R.layout.activity_frame_wrapper)
        frameWrapper.inflate(layoutRes, true)
    }

}
