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
interface VideoViewHolder : FrameWrapper, FrostVideoContainerContract {

    var videoViewer: FrostVideoViewer?

    fun showVideo(url: String) = showVideo(url, false)

    /**
     * Create new viewer and reuse existing one
     * The url will be formatted upon loading
     */
    fun showVideo(url: String, repeat: Boolean) {
        if (videoViewer != null)
            videoViewer?.setVideo(url, repeat)
        else
            videoViewer = FrostVideoViewer.showVideo(url, repeat, this)
    }

    fun videoOnStop() = videoViewer?.pause()

    fun videoOnBackPress() = videoViewer?.onBackPressed() ?: false

    override val videoContainer: FrameLayout
        get() = frameWrapper

    override fun onVideoFinished() {
        L.d { "Video view released" }
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
