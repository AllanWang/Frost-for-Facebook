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

//these hosts will redirect to a blank resource
private val blacklistHost: Set<String> =
        setOf(
                //                "edge-chat.facebook.com" //todo make more specific? This is required for message responses
        )

//these hosts will return null and skip logging
private val whitelistHost: Set<String> =
        setOf(
                "static.xx.fbcdn.net",
                "m.facebook.com",
                "touch.facebook.com"
        )

//these hosts will skip ad inspection
//this list does not have to include anything from the two above
private val adWhitelistHost: Set<String> =
        setOf(
                "scontent-sea1-1.xx.fbcdn.net"
        )

fun WebView.shouldFrostInterceptRequest(request: WebResourceRequest): WebResourceResponse? {
    request.url ?: return null
    val httpUrl = HttpUrl.parse(request.url.toString()) ?: return null
    val host = httpUrl.host()
    val url = httpUrl.toString()
//    if (blacklistHost.contains(host)) return blankResource
    if (whitelistHost.contains(host)) return null
    if (!adWhitelistHost.contains(host) && FrostPglAdBlock.isAdHost(host)) return blankResource
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
fun WebResourceResponse?.filter(request: WebResourceRequest, filter: (url: String) -> Boolean)
        = filter(request.query { filter(it) })

fun WebResourceResponse?.filter(filter: Boolean): WebResourceResponse?
        = this ?: if (filter) blankResource else null

fun WebResourceResponse?.filterCss(request: WebResourceRequest): WebResourceResponse?
        = filter(request) { it.endsWith(".css") }

fun WebResourceResponse?.filterImage(request: WebResourceRequest): WebResourceResponse?
        = filter(request.isImage)

