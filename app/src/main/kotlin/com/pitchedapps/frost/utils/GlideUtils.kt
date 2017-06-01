package com.pitchedapps.frost.utils

import android.content.Context
import com.bumptech.glide.Glide

/**
 * Created by Allan Wang on 2017-05-31.
 */
object GlideUtils {

    lateinit var applicationContext: Context

    operator fun invoke(applicationContext: Context) {
        this.applicationContext = applicationContext
    }

    fun downloadForLater(url: String) {
        Glide.with(applicationContext).download(url)
    }

    fun downloadProfile(id: Long) {
        L.d("Downloading profile photo")
        downloadForLater("http://graph.facebook.com/$id/picture?type=large")
    }

}