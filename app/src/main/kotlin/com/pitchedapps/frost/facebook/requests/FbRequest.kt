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
package com.pitchedapps.frost.facebook.requests

import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.facebook.USER_AGENT
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

val httpClient: OkHttpClient by lazy {
    val builder = OkHttpClient.Builder()
    if (BuildConfig.DEBUG)
        builder.addInterceptor(
            HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BASIC)
        )
    builder.build()
}

internal fun String?.requestBuilder(): Request.Builder {
    val builder = Request.Builder()
        .header("User-Agent", USER_AGENT)
    if (this != null)
        builder.header("Cookie", this)
//        .cacheControl(CacheControl.FORCE_NETWORK)
    return builder
}

fun Request.Builder.call(): Call = httpClient.newCall(build())
