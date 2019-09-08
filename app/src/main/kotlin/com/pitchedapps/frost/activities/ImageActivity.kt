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
import android.os.Environment
import android.view.View
import androidx.customview.widget.ViewDragHelper
import androidx.databinding.DataBindingUtil
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.logging.KauLoggerExtension
import ca.allanwang.kau.mediapicker.scanMedia
import ca.allanwang.kau.permissions.PERMISSION_WRITE_EXTERNAL_STORAGE
import ca.allanwang.kau.permissions.kauRequestPermissions
import ca.allanwang.kau.utils.adjustAlpha
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.copyFromInputStream
import ca.allanwang.kau.utils.fadeOut
import ca.allanwang.kau.utils.gone
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
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.R
import com.pitchedapps.frost.databinding.ActivityImageBinding
import com.pitchedapps.frost.facebook.FB_IMAGE_ID_MATCHER
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.facebook.requests.call
import com.pitchedapps.frost.facebook.requests.getFullSizedImageUrl
import com.pitchedapps.frost.facebook.requests.requestBuilder
import com.pitchedapps.frost.services.LocalService
import com.pitchedapps.frost.utils.ARG_COOKIE
import com.pitchedapps.frost.utils.ARG_IMAGE_URL
import com.pitchedapps.frost.utils.ARG_TEXT
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostSnackbar
import com.pitchedapps.frost.utils.frostUriFromFile
import com.pitchedapps.frost.utils.isIndirectImageUrl
import com.pitchedapps.frost.utils.logFrostEvent
import com.pitchedapps.frost.utils.sendFrostEmail
import com.pitchedapps.frost.utils.setFrostColors
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

/**
 * Created by Allan Wang on 2017-07-15.
 */
class ImageActivity : KauBaseActivity() {

    @Volatile
    internal var errorRef: Throwable? = null

    /**
     * Reference to the temporary file path
     */
    internal lateinit var tempFile: File
    /**
     * Reference to path for downloaded image
     * Nonnull once the image is downloaded by the user
     */
    internal var savedFile: File? = null
    /**
     * Indicator for fab's click result
     */
    internal var fabAction: FabStates = FabStates.NOTHING
        set(value) {
            if (field == value) return
            field = value
            value.update(binding.imageFab)
        }

    private lateinit var dragHelper: ViewDragHelper

    private var imgExtension: String = ".jpg"

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

    private lateinit var binding: ActivityImageBinding
    private var bottomBehavior: BottomSheetBehavior<View>? = null

    private val baseBackgroundColor = if (Prefs.blackMediaBg) Color.BLACK
    else Prefs.bgColor.withMinAlpha(235)

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
        tempFile.delete()
        fabAction = FabStates.ERROR
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image)
        binding.onCreate()
        tempFile = File(cacheDir(this), imageHash)
        launch(CoroutineExceptionHandler { _, throwable -> loadError(throwable) }) {
            downloadImageTo(tempFile)
            binding.imageProgress.fadeOut()
            binding.imagePhoto.setImage(ImageSource.uri(frostUriFromFile(tempFile)))
            fabAction = FabStates.DOWNLOAD
            binding.imagePhoto.animate().alpha(1f).scaleXY(1f).start()
        }
    }

    private fun ActivityImageBinding.onCreate() {
        imageContainer.setBackgroundColor(baseBackgroundColor)
        this@ImageActivity.imageText.also { text ->
            if (text.isNullOrBlank()) {
                imageText.gone()
            } else {
                imageText.setTextColor(if (Prefs.blackMediaBg) Color.WHITE else Prefs.textColor)
                imageText.setBackgroundColor(
                    (if (Prefs.blackMediaBg) Color.BLACK else Prefs.bgColor)
                        .colorToForeground(0.2f).withAlpha(255)
                )
                imageText.text = text
                bottomBehavior = BottomSheetBehavior.from<View>(imageText).apply {
                    setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                            if (slideOffset == 0f && !imageFab.isShown) imageFab.show()
                            else if (slideOffset != 0f && imageFab.isShown) imageFab.hide()
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
        imageProgress.tint(if (Prefs.blackMediaBg) Color.WHITE else Prefs.accentColor)
        imageFab.setOnClickListener { fabAction.onClick(this@ImageActivity) }
        imagePhoto.setOnImageEventListener(object :
            SubsamplingScaleImageView.DefaultOnImageEventListener() {
            override fun onImageLoadError(e: Exception) {
                loadError(e)
            }
        })
        setFrostColors {
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
                //make sure that we are using the proper axis
                scrollPercent = abs(top.toFloat() / imageContainer.height)
                scrollToTop = top < 0
                val multiplier = max(1f - scrollPercent, 0f)

                imageFab.alpha = multiplier
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
            "jpeg" -> ".jpg"
            "png" -> ".png"
            "gif" -> ".gif"
            else -> null
        }
    }

    @Throws(IOException::class)
    private fun createPublicMediaFile(): File {
        val timeStamp = SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).format(Date())
        val imageFileName = "${IMG_TAG}_${timeStamp}_"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val frostDir = File(storageDir, IMG_TAG)
        if (!frostDir.exists()) frostDir.mkdirs()
        return File.createTempFile(imageFileName, imgExtension, frostDir)
    }

    /**
     * Saves the image to the specified file, creating it if it doesn't exist.
     * Returns true if a change is made, false otherwise.
     * Throws an error if something goes wrong.
     */
    @Throws(IOException::class)
    private suspend fun downloadImageTo(file: File): Boolean {
        val exceptionHandler = CoroutineExceptionHandler { _, err ->
            if (file.isFile && file.length() == 0L) {
                file.delete()
            }
            throw err
        }
        return withContext(Dispatchers.IO + exceptionHandler) {
            if (!file.isFile) {
                file.parentFile?.mkdirs()
                file.createNewFile()
            } else {
                file.setLastModified(System.currentTimeMillis())
            }

            // Forbid overwrites
            if (file.isFile && file.length() > 0) {
                L.i { "Forbid image overwrite" }
                return@withContext false
            }

            // Fast route, image is already downloaded
            if (tempFile.isFile && tempFile.length() > 0) {
                if (tempFile == file) {
                    return@withContext false
                }
                tempFile.copyTo(file, true)
                return@withContext true
            }

            // No temp file, download ourselves
            val response = cookie.requestBuilder()
                .url(trueImageUrl.await())
                .get()
                .call()
                .execute()

            if (!response.isSuccessful) {
                throw IOException("Unsuccessful response for image: ${response.peekBody(128).string()}")
            }

            imgExtension = getImageExtension(response.header("Content-Type")) ?: ".jpg"

            val body = response.body() ?: throw IOException("Failed to retrieve image body")

            file.copyFromInputStream(body.byteStream())

            return@withContext true
        }
    }

    internal fun saveImage() {
        kauRequestPermissions(PERMISSION_WRITE_EXTERNAL_STORAGE) { granted, _ ->
            L.d { "Download image callback granted: $granted" }
            if (granted) {
                val errorHandler = CoroutineExceptionHandler { _, throwable ->
                    loadError(throwable)
                    frostSnackbar(R.string.image_download_fail)
                }
                launch(errorHandler) {
                    val destination = createPublicMediaFile()
                    downloadImageTo(destination)
                    L.d { "Download image async finished" }
                    scanMedia(destination)
                    savedFile = destination
                    frostSnackbar(R.string.image_download_success)
                    fabAction = FabStates.SHARE
                }
            }
        }
    }

    override fun onDestroy() {
        LocalService.schedule(this, LocalService.Flag.PURGE_IMAGE)
        super.onDestroy()
    }
}

internal enum class FabStates(
    val iicon: IIcon,
    val iconColor: Int = Prefs.iconColor,
    val backgroundTint: Int = Int.MAX_VALUE
) {
    ERROR(GoogleMaterial.Icon.gmd_error, Color.WHITE, Color.RED) {
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
        override fun onClick(activity: ImageActivity) = activity.saveImage()
    },
    SHARE(GoogleMaterial.Icon.gmd_share) {
        override fun onClick(activity: ImageActivity) {
            try {
                val photoURI = activity.frostUriFromFile(activity.savedFile!!)
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
    fun update(fab: FloatingActionButton) {
        val tint = if (backgroundTint != Int.MAX_VALUE) backgroundTint else Prefs.accentColor
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
