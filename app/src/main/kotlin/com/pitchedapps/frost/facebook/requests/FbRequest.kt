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
import com.pitchedapps.frost.facebook.FB_JSON_URL_MATCHER
import com.pitchedapps.frost.facebook.USER_AGENT
import com.pitchedapps.frost.facebook.get
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.text.StringEscapeUtils

/**
 * Request container with the execution call
 */
class FrostRequest<out T : Any?>(val call: Call, private val invoke: (Call) -> T) {
    fun invoke() = invoke(call)
}

val httpClient: OkHttpClient by lazy {
    val builder = OkHttpClient.Builder()
    if (BuildConfig.DEBUG)
        builder.addInterceptor(
            HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BASIC)
        )
    builder.build()
}

internal fun List<Pair<String, Any?>>.toForm(): FormBody {
    val builder = FormBody.Builder()
    forEach { (key, value) ->
        val v = value?.toString() ?: ""
        builder.add(key, v)
    }
    return builder.build()
}

internal fun List<Pair<String, Any?>>.withEmptyData(vararg key: String): List<Pair<String, Any?>> {
    val newList = toMutableList()
    newList.addAll(key.map { it to null })
    return newList
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

/**
 * Execute the call and attempt to check validity
 * Valid = not blank & no "error" instance
 */
fun executeForNoError(call: Call): Boolean {
    val body = call.execute().body() ?: return false
    var empty = true
    body.charStream().useLines { lines ->
        lines.forEach {
            if (it.contains("error")) return false
            if (empty && it.isNotEmpty()) empty = false
        }
    }
    return !empty
}

fun getJsonUrl(call: Call): String? {
    val body = call.execute().body() ?: return null
    val url = FB_JSON_URL_MATCHER.find(body.string())[1] ?: return null
    return StringEscapeUtils.unescapeEcmaScript(url)
}
