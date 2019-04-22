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
import com.pitchedapps.frost.facebook.FB_DTSG_MATCHER
import com.pitchedapps.frost.facebook.FB_JSON_URL_MATCHER
import com.pitchedapps.frost.facebook.FB_REV_MATCHER
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.FB_USER_MATCHER
import com.pitchedapps.frost.facebook.USER_AGENT_DESKTOP
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.kotlin.Flyweight
import com.pitchedapps.frost.utils.L
import kotlinx.coroutines.GlobalScope
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.text.StringEscapeUtils

/**
 * Created by Allan Wang on 21/12/17.
 */
val fbAuth = Flyweight<String, RequestAuth>(GlobalScope, 3600000 /* an hour */) {
    it.getAuth()
}

/**
 * Underlying container for all fb requests
 */
data class RequestAuth(
    val userId: Long = -1,
    val cookie: String = "",
    val fb_dtsg: String = "",
    val rev: String = ""
) {
    val isComplete
        get() = userId > 0 && cookie.isNotEmpty() && fb_dtsg.isNotEmpty() && rev.isNotEmpty()
}

/**
 * Request container with the execution call
 */
class FrostRequest<out T : Any?>(val call: Call, private val invoke: (Call) -> T) {
    fun invoke() = invoke(call)
}

internal inline fun <T : Any?> RequestAuth.frostRequest(
    noinline invoke: (Call) -> T,
    builder: Request.Builder.() -> Request.Builder // to ensure we don't do anything extra at the end
): FrostRequest<T> {
    val request = cookie.requestBuilder()
    request.builder()
    return FrostRequest(request.call(), invoke)
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
        .header("User-Agent", USER_AGENT_DESKTOP)
    if (this != null)
        builder.header("Cookie", this)
//        .cacheControl(CacheControl.FORCE_NETWORK)
    return builder
}

fun Request.Builder.call(): Call = httpClient.newCall(build())

fun String.getAuth(): RequestAuth {
    L.v { "Getting auth for ${hashCode()}" }
    var auth = RequestAuth(cookie = this)
    val id = FB_USER_MATCHER.find(this)[1]?.toLong() ?: return auth
    auth = auth.copy(userId = id)
    val call = this.requestBuilder()
        .url(FB_URL_BASE)
        .get()
        .call()
    call.execute().body()?.charStream()?.useLines { lines ->
        lines.forEach {
            val text = try {
                StringEscapeUtils.unescapeEcmaScript(it)
            } catch (ignore: Exception) {
                return@forEach
            }
            val fb_dtsg = FB_DTSG_MATCHER.find(text)[1]
            if (fb_dtsg != null) {
                auth = auth.copy(fb_dtsg = fb_dtsg)
                if (auth.isComplete) return auth
            }

            val rev = FB_REV_MATCHER.find(text)[1]
            if (rev != null) {
                auth = auth.copy(rev = rev)
                if (auth.isComplete) return auth
            }
        }
    }

    return auth
}

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
