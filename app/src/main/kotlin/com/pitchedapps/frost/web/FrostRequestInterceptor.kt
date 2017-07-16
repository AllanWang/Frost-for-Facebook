package com.pitchedapps.frost.web

import android.graphics.Bitmap.CompressFormat
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import ca.allanwang.kau.utils.use
import com.pitchedapps.frost.utils.GlideApp
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import okhttp3.HttpUrl
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


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
    if (url.contains("sea1") && (url.contains(".jpg") || url.contains(".png"))) return imageRequest(view, httpUrl)
    return null
}

fun WebResourceResponse?.filterCss(request: WebResourceRequest): WebResourceResponse?
        = this ?: if (request.url.path.endsWith(".css")) blankResource else null

fun imageRequest(view: WebView, url: HttpUrl): WebResourceResponse?
        = if (Prefs.customImageCache) FrostImageResponse(view, url.toString()) else null

class FrostImageResponse(view: WebView, url: String) : WebResourceResponse("", "", null) {

    val futureBitmap = GlideApp.with(view).asBitmap().load(url).submit()

    override fun getData(): InputStream {
        val bos = ByteArrayOutputStream()
        futureBitmap.get().compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
        val bitmapdata = bos.toByteArray()
        return ByteArrayInputStream(bitmapdata)
    }
}

