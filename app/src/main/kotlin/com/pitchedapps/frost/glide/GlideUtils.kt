package com.pitchedapps.frost.glide

import ca.allanwang.kau.kotlin.lazyResettable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions

/**
 * Created by Allan Wang on 28/12/17.
 *
 * Collection of transformations
 * Each caller will generate a new one upon request
 */
object FrostGlide {
    val roundCorner
        get() = RoundCornerTransformation()
    val circleCrop
        get() = CircleCrop()
}

fun <T> RequestBuilder<T>.transform(vararg transformation: BitmapTransformation): RequestBuilder<T> =
        when (transformation.size) {
            0 -> this
            1 -> apply(RequestOptions.bitmapTransform(transformation[0]))
            else -> apply(RequestOptions.bitmapTransform(MultiTransformation(*transformation)))
        }
