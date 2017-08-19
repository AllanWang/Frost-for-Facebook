package com.pitchedapps.frost.activities

import android.content.Context
import android.net.Uri
import android.support.v4.content.FileProvider
import ca.allanwang.kau.mediapicker.*
import ca.allanwang.kau.utils.colorToBackground
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.utils.Prefs
import java.io.File

/**
 * Created by Allan Wang on 2017-07-23.
 */
private fun actions(): List<MediaAction> {
    val color = Prefs.accentColorForWhite
    return listOf(object : MediaActionCamera(color) {

        override fun createFile(context: Context): File
                = createMediaFile("Frost", ".jpg")

        override fun createUri(context: Context, file: File): Uri
                = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)

    }, MediaActionGallery(color = color.colorToBackground(0.1f)))
}

class ImagePickerActivity : MediaPickerActivityOverlayBase(MediaType.IMAGE, actions())

class VideoPickerActivity : MediaPickerActivityOverlayBase(MediaType.VIDEO, actions())