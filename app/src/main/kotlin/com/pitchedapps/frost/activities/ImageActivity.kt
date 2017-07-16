package com.pitchedapps.frost.activities

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import ca.allanwang.kau.utils.*
import com.bumptech.glide.request.target.BaseTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.*
import com.sothree.slidinguppanel.SlidingUpPanelLayout

/**
 * Created by Allan Wang on 2017-07-15.
 */
class ImageActivity : AppCompatActivity() {

    val container: FrameLayout by bindView(R.id.image_container)
    val panel: SlidingUpPanelLayout? by bindOptionalView(R.id.image_panel)
    val photo: SubsamplingScaleImageView by bindView(R.id.image_photo)
    val caption: TextView? by bindOptionalView(R.id.image_text)
    val fab: FloatingActionButton by bindView(R.id.image_fab)

    val imageUrl: String
        get() = intent.extras.getString(ARG_IMAGE_URL)

    val text: String?
        get() = intent.extras.getString(ARG_TEXT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(if (!text.isNullOrBlank()) R.layout.activity_image else R.layout.activity_image_textless)
        container.setBackgroundColor(Prefs.bgColor.withMinAlpha(200))
        caption?.setTextColor(Prefs.textColor)
        caption?.text = text
        panel?.addPanelSlideListener(object : SlidingUpPanelLayout.SimplePanelSlideListener() {

            override fun onPanelSlide(panel: View, slideOffset: Float) {
                if (slideOffset == 0f && !fab.isShown) fab.show()
                else if (slideOffset != 0f && fab.isShown) fab.hide()
            }

        })
        GlideApp.with(this).asBitmap().load(imageUrl).into(PhotoTarget(this::imageCallback))
    }

    /**
     * Callback to add image to view
     * [resource] is guaranteed to be nonnull when [success] is true
     * and null when it is false
     */
    private fun imageCallback(resource: Bitmap?, success: Boolean) {
        if (success) {
            photo.setImage(ImageSource.cachedBitmap(resource!!))
            fab.setIcon(GoogleMaterial.Icon.gmd_file_download)
            fab.backgroundTintList = ColorStateList.valueOf(Prefs.accentBackgroundColor.withAlpha(255))
            photo.animate().alpha(1f).scaleX(1f).scaleY(1f).withEndAction { fab.show() }.start()
        } else {
            fab.setIcon(GoogleMaterial.Icon.gmd_error)
            fab.backgroundTintList = ColorStateList.valueOf(Color.RED)
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

}