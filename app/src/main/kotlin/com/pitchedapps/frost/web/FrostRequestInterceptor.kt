package com.pitchedapps.frost.web

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.pitchedapps.frost.utils.FrostPglAdBlock
import com.pitchedapps.frost.utils.L
import okhttp3.HttpUrl
import java.io.ByteArrayInputStream


/**
 * Created by Allan Wang on 2017-07-13.
 *
 * Handler to decide when a request should be done by us
 * This is the crux of Frost's optimizations for the web browser
 */
private val blankResource: WebResourceResponse by lazy { WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray())) }

fun WebView.shouldFrostInterceptRequest(request: WebResourceRequest): WebResourceResponse? {
    val requestUrl = request.url?.toString() ?: return null
    val httpUrl = HttpUrl.parse(requestUrl) ?: return null
    val host = httpUrl.host()
    val url = httpUrl.toString()
    if (host.contains("facebook") || host.contains("fbcdn")) return null
    if (FrostPglAdBlock.isAdHost(host)) return blankResource
//    if (!shouldLoadImages && !Prefs.loadMediaOnMeteredNetwork && request.isMedia) return blankResource
    L.v { "Intercept Request: $host $url" }
    return null
}

/**
 * Wrapper to ensure that null exceptions are not reached
 */
fun WebResourceRequest.query(action: (url: String) -> Boolean): Boolean {
    return action(url?.path ?: return false)
}

val WebResourceRequest.isImage: Boolean
    get() = query { it.contains(".jpg") || it.contains(".png") }

val WebResourceRequest.isMedia: Boolean
    get() = query { it.contains(".jpg") || it.contains(".png") || it.contains("video") }

/**
 * Generic filter passthrough
 * If Resource is already nonnull, pass it, otherwise check if filter is met and override the response accordingly
 */
fun WebResourceResponse?.filter(request: WebResourceRequest, filter: (url: String) -> Boolean) = filter(request.query { filter(it) })

fun WebResourceResponse?.filter(filter: Boolean): WebResourceResponse? = this
        ?: if (filter) blankResource else null

fun WebResourceResponse?.filterCss(request: WebResourceRequest): WebResourceResponse? = filter(request) { it.endsWith(".css") }

fun WebResourceResponse?.filterImage(request: WebResourceRequest): WebResourceResponse? = filter(request.isImage)

