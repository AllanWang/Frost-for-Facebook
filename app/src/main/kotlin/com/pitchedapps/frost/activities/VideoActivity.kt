package com.pitchedapps.frost.activities

import android.os.Bundle
import android.view.ViewGroup
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.utils.bindView
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.views.FrostVideoView

/**
 * Created by Allan Wang on 2017-06-01.
 */
class VideoActivity : KauBaseActivity() {

    val container: ViewGroup by bindView(R.id.video_container)
    val video: FrostVideoView by bindView(R.id.video)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_video)
        container.setOnTouchListener { _, event ->
            val y = video.shouldParentAcceptTouch(event)
            L.d("Video SPAT $y")
            y
        }
    }

    override fun onStop() {
        video.pause()
        super.onStop()
    }
}