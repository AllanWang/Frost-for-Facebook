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
package com.pitchedapps.frost.activities

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.customview.widget.ViewDragHelper
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.logging.KauLoggerExtension
import ca.allanwang.kau.utils.adjustAlpha
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.copyFromInputStream
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.fadeOut
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.invisible
import ca.allanwang.kau.utils.isHidden
import ca.allanwang.kau.utils.isVisible
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.scaleXY
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.toast
import ca.allanwang.kau.utils.withAlpha
import ca.allanwang.kau.utils.withMinAlpha
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.databinding.ActivityImageBinding
import com.pitchedapps.frost.facebook.FB_IMAGE_ID_MATCHER
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.facebook.requests.call
import com.pitchedapps.frost.facebook.requests.getFullSizedImageUrl
import com.pitchedapps.frost.facebook.requests.requestBuilder
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.services.LocalService
import com.pitchedapps.frost.utils.ARG_COOKIE
import com.pitchedapps.frost.utils.ARG_IMAGE_URL
import com.pitchedapps.frost.utils.ARG_TEXT
import com.pitchedapps.frost.utils.frostDownload
import com.pitchedapps.frost.utils.frostSnackbar
import com.pitchedapps.frost.utils.frostUriFromFile
import com.pitchedapps.frost.utils.isIndirectImageUrl
import com.pitchedapps.frost.utils.logFrostEvent
import com.pitchedapps.frost.utils.sendFrostEmail
import com.pitchedapps.frost.utils.setFrostColors
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.math.abs
import kotlin.math.max
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/**
 * Created by Allan Wang on 2017-07-15.
 */
class ImageActivity : KauBaseActivity() {

    private val prefs: Prefs by inject()

    @Volatile
    internal var errorRef: Throwable? = null

    /**
     * Reference to the temporary file path
     */
    internal var tempFile: File? = null

    private lateinit var dragHelper: ViewDragHelper

    companion object {
        /**
         * Cache folder to store images
         * Linked to the uri provider
         */
        private const val IMAGE_FOLDER = "images"
        private const val TIME_FORMAT = "yyyyMMdd_HHmmss"
        private const val IMG_TAG = "Frost"
        const val PURGE_TIME: Long = 10 * 60 * 1000 // 10 min block
        private val L = KauLoggerExtension("Image", com.pitchedapps.frost.utils.L)

        fun cacheDir(context: Context): File =
            File(context.cacheDir, IMAGE_FOLDER)
    }

    private val cookie: String? by lazy { intent.getStringExtra(ARG_COOKIE) }

    val imageUrl: String by lazy { intent.getStringExtra(ARG_IMAGE_URL)?.trim('"') ?: "" }

    private lateinit var trueImageUrl: Deferred<String>

    private val imageText: String? by lazy { intent.getStringExtra(ARG_TEXT) }

    // a unique image identifier based on the id (if it exists), and its hash
    private val imageHash: String by lazy {
        "${abs(FB_IMAGE_ID_MATCHER.find(imageUrl)[1]?.hashCode() ?: 0)}_${abs(imageUrl.hashCode())}"
    }

    lateinit var binding: ActivityImageBinding
    private var bottomBehavior: BottomSheetBehavior<View>? = null

    private val baseBackgroundColor = if (prefs.blackMediaBg) Color.BLACK
    else prefs.bgColor.withMinAlpha(235)

    private fun loadError(e: Throwable) {
        if (e.message?.contains("<!DOCTYPE html>") == true) {
            applicationContext.toast(R.string.image_not_found)
            finish()
            return
        }
        errorRef = e
        e.logFrostEvent("Image load error")
        with(binding) {
            if (imageProgress.isVisible)
                imageProgress.fadeOut()
        }
        tempFile?.delete()
        binding.error.fadeIn()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (imageUrl.isEmpty()) {
            return finish()
        }
        L.i { "Displaying image" }
        trueImageUrl = async(Dispatchers.IO) {
            val result = if (!imageUrl.isIndirectImageUrl) imageUrl
            else cookie?.getFullSizedImageUrl(imageUrl) ?: imageUrl
            if (result != imageUrl)
                L.v { "Launching with true url $result" }
            result
        }
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.init()
        launch(CoroutineExceptionHandler { _, throwable -> loadError(throwable) }) {
            val tempFile = downloadTempImage()
            this@ImageActivity.tempFile = tempFile
            binding.imageProgress.fadeOut()
            binding.imagePhoto.setImage(ImageSource.uri(frostUriFromFile(tempFile)))
            binding.imagePhoto.animate().alpha(1f).scaleXY(1f).start()
        }
    }

    private fun ActivityImageBinding.init() {
        imageContainer.setBackgroundColor(baseBackgroundColor)
        toolbar.setBackgroundColor(baseBackgroundColor)
        this@ImageActivity.imageText.also { text ->
            if (text.isNullOrBlank()) {
                imageText.gone()
            } else {
                imageText.setTextColor(if (prefs.blackMediaBg) Color.WHITE else prefs.textColor)
                imageText.setBackgroundColor(
                    baseBackgroundColor.colorToForeground(0.2f).withAlpha(255)
                )
                imageText.text = text
                bottomBehavior = BottomSheetBehavior.from<View>(imageText).apply {
                    addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                            imageText.alpha = slideOffset / 2 + 0.5f
                        }

                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            // No op
                        }
                    })
                }
                imageText.bringToFront()
            }
        }
        val foregroundTint = if (prefs.blackMediaBg) Color.WHITE else prefs.accentColor

        fun ImageView.setState(state: FabStates) {
            setIcon(state.iicon, color = foregroundTint, sizeDp = 24)
            setOnClickListener { state.onClick(this@ImageActivity) }
        }

        imageProgress.tint(foregroundTint)
        error.apply {
            invisible()
            setState(FabStates.ERROR)
        }
        download.apply {
            setState(FabStates.DOWNLOAD)
        }
        share.apply {
            setState(FabStates.SHARE)
        }
        imagePhoto.setOnImageEventListener(object :
            SubsamplingScaleImageView.DefaultOnImageEventListener() {
            override fun onImageLoadError(e: Exception) {
                loadError(e)
            }
        })
        setFrostColors(prefs) {
            themeWindow = false
        }
        dragHelper = ViewDragHelper.create(imageDrag, ViewDragCallback()).apply {
            setEdgeTrackingEnabled(ViewDragHelper.EDGE_TOP or ViewDragHelper.EDGE_BOTTOM)
        }
        imageDrag.dragHelper = dragHelper
        imageDrag.viewToIgnore = imageText
    }

    private inner class ViewDragCallback : ViewDragHelper.Callback() {
        private var scrollPercent: Float = 0f
        private var scrollThreshold = 0.5f
        private var scrollToTop = false

        override fun tryCaptureView(view: View, i: Int): Boolean {
            L.d { "Try capture ${view.id} $i ${binding.imagePhoto.id} ${binding.imageText.id}" }
            return view === binding.imagePhoto
        }

        override fun getViewHorizontalDragRange(child: View): Int = 0

        override fun getViewVerticalDragRange(child: View): Int = child.height

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            with(binding) {
                // make sure that we are using the proper axis
                scrollPercent = abs(top.toFloat() / imageContainer.height)
                scrollToTop = top < 0
                val multiplier = max(1f - scrollPercent, 0f)

                toolbar.alpha = multiplier
                bottomBehavior?.also {
                    imageText.alpha =
                        multiplier * (if (it.state == BottomSheetBehavior.STATE_COLLAPSED) 0.5f else 1f)
                }
                imageContainer.setBackgroundColor(baseBackgroundColor.adjustAlpha(multiplier))

                if (scrollPercent >= 1) {
                    if (!isFinishing) {
                        finish()
                        overridePendingTransition(0, 0)
                    }
                }
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val overScrolled = scrollPercent > scrollThreshold
            val maxOffset = releasedChild.height + 10
            val finalTop = when {
                scrollToTop && (overScrolled || yvel < -dragHelper.minVelocity) -> -maxOffset
                !scrollToTop && (overScrolled || yvel > dragHelper.minVelocity) -> maxOffset
                else -> 0
            }
            dragHelper.settleCapturedViewAt(0, finalTop)
            binding.imageDrag.invalidate()
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int = 0

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int = top
    }

    private fun getImageExtension(type: String?): String? {
        if (type?.startsWith("image/") != true) {
            return null
        }
        return when (type.substring(6)) {
            "jpeg" -> "jpg"
            "png" -> "png"
            "gif" -> "gif"
            else -> null
        }
    }

    @Throws(IOException::class)
    private suspend fun downloadTempImage(): File = withContext(Dispatchers.IO) {

        // We assume all images are jpg
        // Activity launcher may be able to provide specifics, but this beats sending a request
        // just to get the content header
        val file = File(cacheDir(this@ImageActivity), "$imageHash.jpg")

        if (!file.isFile) {
            file.parentFile?.mkdirs()
            file.createNewFile()
        } else {
            file.setLastModified(System.currentTimeMillis())
        }

        // Forbid overwrites
        if (file.isFile && file.length() > 0) {
            L.i { "Forbid image overwrite" }
            return@withContext file
        }

        val response = cookie.requestBuilder()
            .url(trueImageUrl.await())
            .get()
            .call()
            .execute()

        if (!response.isSuccessful) {
            throw IOException("Unsuccessful response for image: ${response.peekBody(128).string()}")
        }

        val body = response.body ?: throw IOException("Failed to retrieve image body")
        file.copyFromInputStream(body.byteStream())
        file
    }

    internal suspend fun saveImage() {
        frostDownload(cookie = cookie, url = trueImageUrl.await())
    }

    override fun onDestroy() {
        LocalService.schedule(this, LocalService.Flag.PURGE_IMAGE)
        super.onDestroy()
    }
}

internal enum class FabStates(
    val iicon: IIcon,
    val iconColorProvider: (Prefs) -> Int = { it.iconColor },
    val backgroundTint: Int = Int.MAX_VALUE
) {
    ERROR(GoogleMaterial.Icon.gmd_error, { Color.WHITE }, Color.RED) {
        override fun onClick(activity: ImageActivity) {
            val err =
                activity.errorRef?.takeIf { it !is FileNotFoundException && it.message != "Image failed to decode using JPEG decoder" }
                    ?: return
            activity.materialDialog {
                title(R.string.kau_error)
                message(R.string.bad_image_overlay)
                positiveButton(R.string.kau_yes) {
                    activity.sendFrostEmail(R.string.debug_image_link_subject) {
                        addItem("Url", activity.imageUrl)
                        addItem("Type", err.javaClass.name)
                        addItem("Message", err.message ?: "Null")
                    }
                }
                negativeButton(R.string.kau_no)
            }
        }
    },
    NOTHING(GoogleMaterial.Icon.gmd_adjust) {
        override fun onClick(activity: ImageActivity) {}
    },
    DOWNLOAD(GoogleMaterial.Icon.gmd_file_download) {
        override fun onClick(activity: ImageActivity) {
            activity.launch {
                activity.binding.download.fadeOut()
                activity.saveImage()
            }
        }
    },
    SHARE(GoogleMaterial.Icon.gmd_share) {
        override fun onClick(activity: ImageActivity) {
            val file = activity.tempFile ?: return
            try {
                val photoURI = activity.frostUriFromFile(file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(Intent.EXTRA_STREAM, photoURI)
                    type = "image/png"
                }
                activity.startActivity(intent)
            } catch (e: Exception) {
                activity.errorRef = e
                e.logFrostEvent("Image share failed")
                activity.frostSnackbar(R.string.image_share_failed)
            }
        }
    };

    /**
     * Change the fab look
     * If it's in view, give it some animations
     *
     * TODO investigate what is wrong with fadeScaleTransition
     *
     * https://github.com/AllanWang/KAU/issues/184
     *
     */
    fun update(fab: FloatingActionButton, prefs: Prefs) {
        val tint = if (backgroundTint != Int.MAX_VALUE) backgroundTint else prefs.accentColor
        val iconColor = iconColorProvider(prefs)
        if (fab.isHidden) {
            fab.setIcon(iicon, color = iconColor)
            fab.backgroundTintList = ColorStateList.valueOf(tint)
            fab.show()
        } else {
            fab.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
                override fun onHidden(fab: FloatingActionButton) {
                    fab.setIcon(iicon, color = iconColor)
                    fab.show()
                }
            })
        }
    }

    abstract fun onClick(activity: ImageActivity)
}
