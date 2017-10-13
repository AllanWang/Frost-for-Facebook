package com.pitchedapps.frost.activities

import android.net.Uri
import android.os.Bundle
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.utils.bindView
import com.devbrackets.android.exomedia.ui.widget.VideoView
import com.pitchedapps.frost.R

/**
 * Created by Allan Wang on 2017-06-01.
 */
class VideoActivity : KauBaseActivity() {

    val video: VideoView by bindView(R.id.video)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_video)
        video.apply {
            setOnPreparedListener {
                video.start()
            }
            showControls()
            setVideoURI(Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"))
            setOnCompletionListener {
                restart()
            }
        }
//        postDelayed(2000) {
//            video.animate().translationYBy(500f)
//        }
    }

}