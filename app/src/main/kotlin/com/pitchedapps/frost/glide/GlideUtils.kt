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
package com.pitchedapps.frost.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.pitchedapps.frost.facebook.FbCookie
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * Created by Allan Wang on 28/12/17.
 *
 * Collection of transformations
 * Each caller will generate a new one upon request
 */
object FrostGlide {
    val circleCrop
        get() = CircleCrop()
}

fun <T> RequestBuilder<T>.transform(vararg transformation: BitmapTransformation): RequestBuilder<T> =
    when (transformation.size) {
        0 -> this
        1 -> apply(RequestOptions.bitmapTransform(transformation[0]))
        else -> apply(RequestOptions.bitmapTransform(MultiTransformation(*transformation)))
    }

@GlideModule
class FrostGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
//        registry.replace(GlideUrl::class.java,
//                InputStream::class.java,
//                OkHttpUrlLoader.Factory(getFrostHttpClient()))
//        registry.prepend(HdImageMaybe::class.java, InputStream::class.java, HdImageLoadingFactory())
    }
}

private fun getFrostHttpClient(): OkHttpClient =
    OkHttpClient.Builder().addInterceptor(FrostCookieInterceptor()).build()

class FrostCookieInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val origRequest = chain.request()
        val cookie = FbCookie.webCookie ?: return chain.proceed(origRequest)
        val request = origRequest.newBuilder().addHeader("Cookie", cookie).build()
        return chain.proceed(request)
    }
}
