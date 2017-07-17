package com.pitchedapps.frost.web

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import ca.allanwang.kau.utils.use
import com.pitchedapps.frost.utils.L
import okhttp3.HttpUrl
import java.io.ByteArrayInputStream


/**
 * Created by Allan Wang on 2017-07-13.
 *
 * Handler to decide when a request should be done by us
 * This is the crux of Frost's optimizations for the web browser
 */
val blankResource: WebResourceResponse by lazy { WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray())) }

//these hosts will redirect to a blank resource
val blacklistHost: Set<String> by lazy {
    setOf(
            "edge-chat.facebook.com"
    )
}

//these hosts will return null and skip logging
val whitelistHost: Set<String> by lazy {
    setOf(
            "static.xx.fbcdn.net",
            "m.facebook.com",
            "touch.facebook.com"
    )
}

//these hosts will skip ad inspection
//this list does not have to include anything from the two above
val adWhitelistHost: Set<String> by lazy {
    setOf(
            "scontent-sea1-1.xx.fbcdn.net"
    )
}

var adblock: Set<String>? = null

fun shouldFrostInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
    val httpUrl = HttpUrl.parse(request.url?.toString() ?: return null) ?: return null
    val host = httpUrl.host()
    val url = httpUrl.toString()
    if (blacklistHost.contains(host)) return blankResource
    if (whitelistHost.contains(host)) return null
    if (!adWhitelistHost.contains(host)) {
        if (adblock == null) adblock = view.context.assets.open("adblock.txt").bufferedReader().use { it.readLines().toSet() }
        if (adblock?.any { url.contains(it) } ?: false) return blankResource
    }
    L.v("Intercept Request ${host} ${url}")
    return null
}

/**
 * Wrapper to ensure that null exceptions are not reached
 */
fun WebResourceRequest.query(action: (url: String) -> Boolean): Boolean {
    return action(url?.path ?: return false)
}

/**
 * Generic filter passthrough
 * If Resource is already nonnull, pass it, otherwise check if filter is met and override the response accordingly
 */
fun WebResourceResponse?.filter(request: WebResourceRequest, filter: (url: String) -> Boolean): WebResourceResponse?
        = this ?: if (request.query { filter(it) }) blankResource else null

fun WebResourceResponse?.filterCss(request: WebResourceRequest): WebResourceResponse?
        = filter(request) { it.endsWith(".css") }

fun WebResourceResponse?.filterImage(request: WebResourceRequest): WebResourceResponse?
        = filter(request) { it.contains(".jpg") || it.contains(".png") }

