package com.pitchedapps.frost.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.FileProvider
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import ca.allanwang.kau.email.sendEmail
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.mediapicker.scanMedia
import ca.allanwang.kau.permissions.PERMISSION_WRITE_EXTERNAL_STORAGE
import ca.allanwang.kau.permissions.kauRequestPermissions
import ca.allanwang.kau.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BaseTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.*
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.IOException

/**
 * Created by Allan Wang on 2017-07-15.
 */
class ImageActivity : KauBaseActivity() {

    val progress: ProgressBar by bindView(R.id.image_progress)
    val container: ViewGroup by bindView(R.id.image_container)
    val panel: SlidingUpPanelLayout? by bindOptionalView(R.id.image_panel)
    val photo: SubsamplingScaleImageView by bindView(R.id.image_photo)
    val caption: TextView? by bindOptionalView(R.id.image_text)
    val fab: FloatingActionButton by bindView(R.id.image_fab)
    var errorRef: Throwable? = null

    /**
     * Reference to the temporary file path
     * Should be nonnull if the image is successfully loaded
     * As this is temporary, the image is deleted upon exit
     */
    internal var tempFilePath: String? = null
    /**
     * Reference to path for downloaded image
     * Nonnull once the image is downloaded by the user
     */
    internal var downloadPath: String? = null
    /**
     * Indicator for fab's click result
     */
    internal var fabAction: FabStates = FabStates.NOTHING
        set(value) {
            if (field == value) return
            field = value
            value.update(fab)
        }

    val imageUrl: String
        get() = intent.extras.getString(ARG_IMAGE_URL)

    val text: String?
        get() = intent.extras.getString(ARG_TEXT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = if (!text.isNullOrBlank()) R.layout.activity_image else R.layout.activity_image_textless
        setContentView(layout)
        container.setBackgroundColor(Prefs.bgColor.withMinAlpha(222))
        caption?.setTextColor(Prefs.textColor)
        caption?.setBackgroundColor(Prefs.bgColor.colorToForeground(0.2f).withAlpha(255))
        caption?.text = text
        progress.tint(Prefs.accentColor)
        panel?.addPanelSlideListener(object : SlidingUpPanelLayout.SimplePanelSlideListener() {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                if (slideOffset == 0f && !fab.isShown) fab.show()
                else if (slideOffset != 0f && fab.isShown) fab.hide()
                caption?.alpha = slideOffset / 2 + 0.5f
            }
        })
        fab.setOnClickListener { fabAction.onClick(this) }
        photo.setOnImageEventListener(object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
            override fun onImageLoadError(e: Exception?) {
                errorRef = e
                e.logFrostAnswers("Image load error")
                imageCallback(null, false)
            }
        })
        Glide.with(this).asBitmap().load(imageUrl).into(PhotoTarget(this::imageCallback))
        setFrostColors(themeWindow = false)
    }

    /**
     * Callback to add image to view
     * [resource] is guaranteed to be nonnull when [success] is true
     * and null when it is false
     */
    private fun imageCallback(resource: Bitmap?, success: Boolean) {
        if (progress.isVisible) progress.fadeOut()
        if (success) {
            saveTempImage(resource!!, {
                if (it == null) {
                    imageCallback(null, false)
                } else {
                    photo.setImage(ImageSource.uri(it))
                    fabAction = FabStates.DOWNLOAD
                    photo.animate().alpha(1f).scaleXY(1f).withEndAction { fab.show() }.start()
                }
            })
        } else {
            fabAction = FabStates.ERROR
            fab.show()
        }
    }

    /**
     * Bitmap load handler
     */
    class PhotoTarget(val callback: (resource: Bitmap?, success: Boolean) -> Unit) : BaseTarget<Bitmap>() {

        override fun removeCallback(cb: SizeReadyCallback?) {}

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) = callback(resource, true)

        override fun onLoadFailed(errorDrawable: Drawable?) = callback(null, false)

        override fun getSize(cb: SizeReadyCallback) = cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)

    }

    private fun saveTempImage(resource: Bitmap, callback: (uri: Uri?) -> Unit) {
        var photoFile: File? = null
        try {
            photoFile = createPrivateMediaFile(".png")
        } catch (e: IOException) {
            errorRef = e
        } finally {
            if (photoFile == null) {
                callback(null)
            } else {
                tempFilePath = photoFile.absolutePath
                L.d("Temp image path $tempFilePath")
                // File created; proceed with request
                val photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile)
                photoFile.outputStream().use { resource.compress(Bitmap.CompressFormat.PNG, 100, it) }
                callback(photoURI)
            }
        }
    }

    internal fun downloadImage() {
        kauRequestPermissions(PERMISSION_WRITE_EXTERNAL_STORAGE) {
            granted, _ ->
            L.d("Download image callback granted: $granted")
            if (granted) {
                doAsync {
                    val destination = createMediaFile(".png")
                    downloadPath = destination.absolutePath
                    var success = true
                    try {
                        File(tempFilePath).copyTo(destination, true)
                        scanMedia(destination)
                    } catch (e: Exception) {
                        errorRef = e
                        success = false
                    } finally {
                        L.d("Download image async finished: $success")
                        uiThread {
                            val text = if (success) R.string.image_download_success else R.string.image_download_fail
                            frostSnackbar(text)
                            if (success) fabAction = FabStates.SHARE
                        }
                    }
                }
            }
        }
    }

    internal fun deleteTempFile() {
        if (tempFilePath != null) {
            File(tempFilePath!!).delete()
            tempFilePath = null
        }
    }

    override fun onDestroy() {
        deleteTempFile()
        super.onDestroy()
    }
}

internal enum class FabStates(val iicon: IIcon, val iconColor: Int = Prefs.iconColor, val backgroundTint: Int = Int.MAX_VALUE) {
    ERROR(GoogleMaterial.Icon.gmd_error, Color.WHITE, Color.RED) {
        override fun onClick(activity: ImageActivity) {
            activity.materialDialogThemed {
                title(R.string.kau_error)
                content(R.string.bad_image_overlay)
                positiveText(R.string.kau_yes)
                onPositive { _, _ ->
                    if (activity.errorRef != null)
                        L.e(activity.errorRef, "ImageActivity error report")
                    activity.sendEmail(R.string.dev_email, R.string.debug_image_link_subject) {
                        addItem("Url", activity.imageUrl)
                        addItem("Message", activity.errorRef?.message ?: "Null")
                    }
                }
                negativeText(R.string.kau_no)
            }
        }
    },
    NOTHING(GoogleMaterial.Icon.gmd_adjust) {
        override fun onClick(activity: ImageActivity) {}
    },
    DOWNLOAD(GoogleMaterial.Icon.gmd_file_download) {
        override fun onClick(activity: ImageActivity) = activity.downloadImage()
    },
    SHARE(GoogleMaterial.Icon.gmd_share) {
        override fun onClick(activity: ImageActivity) {
            try {
                val photoURI = FileProvider.getUriForFile(activity,
                        BuildConfig.APPLICATION_ID + ".provider",
                        File(activity.downloadPath))
                val intent = Intent(Intent.ACTION_SEND).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(Intent.EXTRA_STREAM, photoURI)
                    type = "image/png"
                }
                activity.startActivity(intent)
            } catch (e: Exception) {
                activity.errorRef = e
                e.logFrostAnswers("Image share failed")
                activity.frostSnackbar(R.string.image_share_failed)
            }
        }
    };

    /**
     * Change the fab look
     * If it's in view, give it some animations
     */
    fun update(fab: FloatingActionButton) {
        val tint = if (backgroundTint != Int.MAX_VALUE) backgroundTint else Prefs.accentColor
        if (fab.isHidden) {
            fab.setIcon(iicon, color = iconColor)
            fab.backgroundTintList = ColorStateList.valueOf(tint)
            fab.show()
        } else {
            fab.fadeScaleTransition {
                setIcon(iicon, color = iconColor)
                backgroundTintList = ColorStateList.valueOf(tint)
            }
        }
    }

    abstract fun onClick(activity: ImageActivity)

}