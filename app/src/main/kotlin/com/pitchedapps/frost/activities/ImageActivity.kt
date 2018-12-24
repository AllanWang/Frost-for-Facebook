package com.pitchedapps.frost.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.View
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.logging.KauLoggerExtension
import ca.allanwang.kau.mediapicker.scanMedia
import ca.allanwang.kau.permissions.PERMISSION_WRITE_EXTERNAL_STORAGE
import ca.allanwang.kau.permissions.kauRequestPermissions
import ca.allanwang.kau.utils.*
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FB_IMAGE_ID_MATCHER
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.facebook.requests.call
import com.pitchedapps.frost.facebook.requests.getFullSizedImageUrl
import com.pitchedapps.frost.facebook.requests.requestBuilder
import com.pitchedapps.frost.utils.*
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_image.*
import okhttp3.Response
import org.jetbrains.anko.activityUiThreadWithContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Allan Wang on 2017-07-15.
 */
class ImageActivity : KauBaseActivity() {

    internal var errorRef: Throwable? = null

    private lateinit var tempDir: File

    /**
     * Reference to the temporary file path
     */
    private lateinit var tempFile: File
    /**
     * Reference to path for downloaded image
     * Nonnull once the image is downloaded by the user
     */
    internal var savedFile: File? = null
    /**
     * Indicator for fab's click result
     * Can be called from any thread
     */
    internal var fabAction: FabStates = FabStates.NOTHING
        set(value) {
            if (field == value) return
            field = value
            runOnUiThread { value.update(image_fab) }
        }

    companion object {
        /**
         * Cache folder to store images
         * Linked to the uri provider
         */
        private const val IMAGE_FOLDER = "images"
        private const val TIME_FORMAT = "yyyyMMdd_HHmmss"
        private const val IMG_TAG = "Frost"
        private const val IMG_EXTENSION = ".png"
        private const val PURGE_TIME: Long = 10 * 60 * 1000 // 10 min block
        private val L = KauLoggerExtension("Image", com.pitchedapps.frost.utils.L)
    }

    private val cookie: String? by lazy { intent.getStringExtra(ARG_COOKIE) }

    val imageUrl: String by lazy { intent.getStringExtra(ARG_IMAGE_URL).trim('"') }

    private val trueImageUrl: String by lazy {
        val result = if (!imageUrl.isIndirectImageUrl) imageUrl
        else cookie?.getFullSizedImageUrl(imageUrl)?.blockingGet() ?: imageUrl
        if (result != imageUrl)
            L.v { "Launching with true url $result" }
        result
    }

    private val imageText: String? by lazy { intent.getStringExtra(ARG_TEXT) }

    // a unique image identifier based on the id (if it exists), and its hash
    private val imageHash: String by lazy {
        "${Math.abs(FB_IMAGE_ID_MATCHER.find(imageUrl)[1]?.hashCode()
                ?: 0)}_${Math.abs(imageUrl.hashCode())}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.extras ?: return finish()
        L.i { "Displaying image" }
        L.v { "Displaying image $imageUrl" }
        val layout = if (!imageText.isNullOrBlank()) R.layout.activity_image else R.layout.activity_image_textless
        setContentView(layout)
        image_container.setBackgroundColor(if (Prefs.blackMediaBg) Color.BLACK
        else Prefs.bgColor.withMinAlpha(222))
        image_text?.setTextColor(if (Prefs.blackMediaBg) Color.WHITE else Prefs.textColor)
        image_text?.setBackgroundColor((if (Prefs.blackMediaBg) Color.BLACK else Prefs.bgColor)
                .colorToForeground(0.2f).withAlpha(255))
        image_text?.text = imageText
        image_progress.tint(if (Prefs.blackMediaBg) Color.WHITE else Prefs.accentColor)
        image_panel?.addPanelSlideListener(object : SlidingUpPanelLayout.SimplePanelSlideListener() {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                if (slideOffset == 0f && !image_fab.isShown) image_fab.show()
                else if (slideOffset != 0f && image_fab.isShown) image_fab.hide()
                image_text?.alpha = slideOffset / 2 + 0.5f
            }
        })
        image_fab.setOnClickListener { fabAction.onClick(this) }
        image_photo.setOnImageEventListener(object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
            override fun onImageLoadError(e: Exception?) {
                errorRef = e
                e.logFrostEvent("Image load error")
                L.e { "Failed to load image $imageUrl" }
                tempFile?.delete()
                fabAction = FabStates.ERROR
            }
        })
        setFrostColors {
            themeWindow = false
        }
        tempDir = File(cacheDir, IMAGE_FOLDER)
        tempFile = File(tempDir, imageHash)
        doAsync({
            L.e(it) { "Failed to load image $imageHash" }
            errorRef = it
            runOnUiThread { image_progress.fadeOut() }
            tempFile.delete()
            fabAction = FabStates.ERROR
        }) {
            val loaded = loadImage(tempFile)
            uiThread {
                image_progress.fadeOut()
                if (!loaded) {
                    fabAction = FabStates.ERROR
                } else {
                    image_photo.setImage(ImageSource.uri(frostUriFromFile(tempFile)))
                    fabAction = FabStates.DOWNLOAD
                    image_photo.animate().alpha(1f).scaleXY(1f).start()
                }
            }
        }
    }

    /**
     * Attempts to load the image to [file]
     * Returns true if successful
     * Note that this is a long execution and should not be done on the UI thread
     */
    private fun loadImage(file: File): Boolean {
        if (file.exists() && file.length() > 1) {
            file.setLastModified(System.currentTimeMillis())
            L.d { "Loading from local cache ${file.absolutePath}" }
            return true
        }
        val response = getImageResponse()

        if (!response.isSuccessful) {
            L.e { "Unsuccessful response for image" }
            errorRef = Throwable("Unsuccessful response for image")
            return false
        }

        if (!file.createFreshFile()) {
            L.e { "Could not create temp file" }
            return false
        }

        var valid = false

        response.body()?.byteStream()?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
                valid = true
            }
        }

        if (!valid) {
            L.e { "Failed to copy file" }
            file.delete()
            return false
        }

        return true
    }

    @Throws(IOException::class)
    private fun createPublicMediaFile(): File {
        val timeStamp = SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).format(Date())
        val imageFileName = "${IMG_TAG}_${timeStamp}_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val frostDir = File(storageDir, IMG_TAG)
        if (!frostDir.exists()) frostDir.mkdirs()
        return File.createTempFile(imageFileName, IMG_EXTENSION, frostDir)
    }

    private fun getImageResponse(): Response = cookie.requestBuilder()
            .url(trueImageUrl)
            .get()
            .call()
            .execute()


    @Throws(IOException::class)
    private fun downloadImageTo(file: File) {
        val body = getImageResponse().body()
                ?: throw IOException("Failed to retrieve image body")
        body.byteStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    internal fun saveImage() {
        kauRequestPermissions(PERMISSION_WRITE_EXTERNAL_STORAGE) { granted, _ ->
            L.d { "Download image callback granted: $granted" }
            if (granted) {
                doAsync {
                    val destination = createPublicMediaFile()
                    var success = true
                    try {
                        val temp = tempFile
                        if (temp != null)
                            temp.copyTo(destination, true)
                        else
                            downloadImageTo(destination)
                    } catch (e: Exception) {
                        errorRef = e
                        success = false
                    } finally {
                        L.d { "Download image async finished: $success" }
                        if (success) {
                            scanMedia(destination)
                            savedFile = destination
                        } else {
                            try {
                                destination.delete()
                            } catch (ignore: Exception) {
                            }
                        }
                        activityUiThreadWithContext {
                            val text = if (success) R.string.image_download_success else R.string.image_download_fail
                            frostSnackbar(text)
                            if (success) fabAction = FabStates.SHARE
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        val purge = System.currentTimeMillis() - PURGE_TIME
        tempDir.listFiles(FileFilter { it.isFile && it.lastModified() < purge })?.forEach {
            it.delete()
        }
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
                        L.e(activity.errorRef) { "ImageActivity error report" }
                    activity.sendFrostEmail(R.string.debug_image_link_subject) {
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