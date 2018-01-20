package com.pitchedapps.frost.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
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
import com.pitchedapps.frost.utils.*
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import okhttp3.Request
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

    val progress: ProgressBar by bindView(R.id.image_progress)
    val container: ViewGroup by bindView(R.id.image_container)
    val panel: SlidingUpPanelLayout? by bindOptionalView(R.id.image_panel)
    val photo: SubsamplingScaleImageView by bindView(R.id.image_photo)
    val caption: TextView? by bindOptionalView(R.id.image_text)
    val fab: FloatingActionButton by bindView(R.id.image_fab)
    var errorRef: Throwable? = null

    private val tempDir: File by lazy { File(cacheDir, IMAGE_FOLDER) }

    /**
     * Reference to the temporary file path
     * Should be nonnull if the image is successfully loaded
     * As this is temporary, the image is deleted upon exit
     */
    internal var tempFile: File? = null
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
            runOnUiThread { value.update(fab) }
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

    val IMAGE_URL: String by lazy { intent.getStringExtra(ARG_IMAGE_URL).trim('"') }

    private val TEXT: String? by lazy { intent.getStringExtra(ARG_TEXT) }

    // a unique image identifier based on the id (if it exists), and its hash
    private val IMAGE_HASH: String by lazy {
        "${Math.abs(FB_IMAGE_ID_MATCHER.find(IMAGE_URL)[1]?.hashCode() ?: 0)}_${Math.abs(IMAGE_URL.hashCode())}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.extras ?: return finish()
        L.i { "Displaying image" }
        L.v { "Displaying image $IMAGE_URL" }
        val layout = if (!TEXT.isNullOrBlank()) R.layout.activity_image else R.layout.activity_image_textless
        setContentView(layout)
        container.setBackgroundColor(Prefs.bgColor.withMinAlpha(222))
        caption?.setTextColor(Prefs.textColor)
        caption?.setBackgroundColor(Prefs.bgColor.colorToForeground(0.2f).withAlpha(255))
        caption?.text = TEXT
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
                L.e { "Failed to load image $IMAGE_URL" }
                tempFile?.delete()
                fabAction = FabStates.ERROR
            }
        })
        setFrostColors {
            themeWindow = false
        }
        doAsync({
            L.e(it) { "Failed to load image $IMAGE_HASH" }
            errorRef = it
            runOnUiThread { progress.fadeOut() }
            tempFile?.delete()
            fabAction = FabStates.ERROR
        }) {
            loadImage { file ->
                uiThread { progress.fadeOut() }
                if (file == null) {
                    fabAction = FabStates.ERROR
                    return@loadImage
                }
                tempFile = file
                L.d { "Temp image path ${file.absolutePath}" }
                uiThread {
                    photo.setImage(ImageSource.uri(frostUriFromFile(file)))
                    fabAction = FabStates.DOWNLOAD
                    photo.animate().alpha(1f).scaleXY(1f).start()
                }
            }
        }
    }

    /**
     * Returns a file pointing to the image, or null if something goes wrong
     */
    private inline fun loadImage(callback: (file: File?) -> Unit) {
        val local = File(tempDir, IMAGE_HASH)
        if (local.exists() && local.length() > 1) {
            local.setLastModified(System.currentTimeMillis())
            L.d { "Loading from local cache ${local.absolutePath}" }
            return callback(local)
        }
        val response = getImageResponse()

        if (!response.isSuccessful) {
            L.e { "Unsuccessful response for image" }
            errorRef = Throwable("Unsuccessful response for image")
            return callback(null)
        }

        if (!local.createFreshFile()) {
            L.e { "Could not create temp file" }
            return callback(null)
        }

        var valid = false

        response.body()?.byteStream()?.use { input ->
            local.outputStream().use { output ->
                input.copyTo(output)
                valid = true
            }
        }

        if (!valid) {
            L.e { "Failed to copy file" }
            local.delete()
            return callback(null)
        }

        callback(local)
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

    private fun getImageResponse() = Request.Builder()
            .url(IMAGE_URL)
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
        tempFile = null
        val purge = System.currentTimeMillis() - PURGE_TIME
        tempDir.listFiles(FileFilter { it.isFile && it.lastModified() < purge }).forEach {
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
                        addItem("Url", activity.IMAGE_URL)
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