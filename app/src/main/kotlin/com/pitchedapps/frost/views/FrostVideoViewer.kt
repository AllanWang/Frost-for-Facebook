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

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.fadeOut
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.goneIf
import ca.allanwang.kau.utils.isColorDark
import ca.allanwang.kau.utils.isGone
import ca.allanwang.kau.utils.isVisible
import ca.allanwang.kau.utils.launchMain
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.setMenuIcons
import ca.allanwang.kau.utils.visible
import ca.allanwang.kau.utils.withMinAlpha
import com.devbrackets.android.exomedia.listener.VideoControlsVisibilityListener
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.databinding.ViewVideoBinding
import com.pitchedapps.frost.db.CookieDao
import com.pitchedapps.frost.db.currentCookie
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.ctxCoroutine
import com.pitchedapps.frost.utils.frostDownload
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Allan Wang on 2017-10-13.
 */
class FrostVideoViewer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), FrostVideoViewerContract, KoinComponent {

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
        fun showVideo(
            url: String,
            repeat: Boolean,
            contract: FrostVideoContainerContract
        ): FrostVideoViewer {
            val container = contract.videoContainer
            val videoViewer = FrostVideoViewer(container.context)
            container.addView(videoViewer)
            videoViewer.bringToFront()
            videoViewer.setVideo(url, repeat)
            videoViewer.binding.video.containerContract = contract
            videoViewer.binding.video.onFinishedListener =
                { container.removeView(videoViewer); contract.onVideoFinished() }
            return videoViewer
        }
    }

    private val prefs: Prefs by inject()
    private val cookieDao: CookieDao by inject()

    private val binding: ViewVideoBinding =
        ViewVideoBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.init()
    }

    fun ViewVideoBinding.init() {
        alpha = 0f
        videoBackground.setBackgroundColor(
            if (!prefs.blackMediaBg && prefs.bgColor.isColorDark)
                prefs.bgColor.withMinAlpha(200)
            else
                Color.BLACK
        )
        video.setViewerContract(this@FrostVideoViewer)
        video.pause()
        videoToolbar.inflateMenu(R.menu.menu_video)
        context.setMenuIcons(
            videoToolbar.menu, prefs.iconColor,
            R.id.action_pip to GoogleMaterial.Icon.gmd_picture_in_picture_alt,
            R.id.action_download to GoogleMaterial.Icon.gmd_file_download
        )
        videoToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_pip -> video.isExpanded = false
                R.id.action_download -> context.ctxCoroutine.launchMain {
                    val cookie = cookieDao.currentCookie(prefs) ?: return@launchMain
                    context.frostDownload(cookie.cookie, video.videoUri)
                }
            }
            true
        }
        videoRestart.gone().setIcon(GoogleMaterial.Icon.gmd_replay, 64)
        videoRestart.setOnClickListener {
            video.restart()
            videoRestart.fadeOut { videoRestart.gone() }
        }
    }

    fun setVideo(url: String, repeat: Boolean = false) {
        with(binding) {
            L.d { "Load video; repeat: $repeat" }
            L._d { "Video Url: $url" }
            animate().alpha(1f).setDuration(FrostVideoView.ANIMATION_DURATION).start()
            video.setVideoURI(Uri.parse(url))
            video.repeat = repeat
        }
    }

    /**
     * Handle back presses
     * returns true if consumed, false otherwise
     */
    fun onBackPressed(): Boolean {
        with(binding) {
            parent ?: return false
            if (video.isExpanded)
                video.isExpanded = false
            else
                video.destroy()
            return true
        }
    }

    fun pause() = binding.video.pause()

    /*
     * -------------------------------------------------------------
     * FrostVideoViewerContract
     * -------------------------------------------------------------
     */

    override fun onExpand(progress: Float) {
        with(binding) {
            videoToolbar.goneIf(progress == 0f).alpha = progress
            videoBackground.alpha = progress
        }
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        with(binding) {
            if (videoRestart.isVisible) {
                videoRestart.performClick()
                return true
            }
            return false
        }
    }

    override fun onVideoComplete() {
        with(binding) {
            video.jumpToStart()
            videoRestart.fadeIn()
        }
    }

    fun updateLocation() {
        with(binding) {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    video.updateLocation()
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    override fun onControlsShown() {
        with(binding) {
            if (video.isExpanded)
                videoToolbar.fadeIn(
                    duration = CONTROL_ANIMATION_DURATION,
                    onStart = { videoToolbar.visible() })
        }
    }

    override fun onControlsHidden() {
        with(binding) {
            if (!videoToolbar.isGone)
                videoToolbar.fadeOut(duration = CONTROL_ANIMATION_DURATION) { videoToolbar.gone() }
        }
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
