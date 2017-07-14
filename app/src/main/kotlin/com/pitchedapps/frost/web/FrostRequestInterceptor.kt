package com.pitchedapps.frost.web

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import ca.allanwang.kau.utils.use
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.utils.L
import io.reactivex.Single
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Created by Allan Wang on 2017-07-13.
 *
 * Handler to decide when a request should be done by us
 * This is the crux of Frost's optimizations for the web browser
 */
val blankResource: WebResourceResponse by lazy { WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray())) }

val blacklistHost: Set<String> by lazy {
    setOf(
            "edge-chat.facebook.com"
    )
}

val whitelistHost: Set<String> by lazy {
    setOf(
            "scontent-sea1-1.xx.fbcdn.net",
            "m.facebook.com",
            "touch.facebook.com"
    )
}

var adblock: Set<String>? = null

fun shouldFrostInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
    val url = HttpUrl.parse(request.url?.toString() ?: return null) ?: return null
    if (blacklistHost.contains(url.host())) return blankResource
    if (!whitelistHost.contains(url.host())) {
        if (adblock == null) adblock = view.context.assets.open("adblock.txt").bufferedReader().use { it.readLines().toSet() }
        if (adblock?.any { url.toString().contains(it) } ?: false) return blankResource
    }
    L.v("Intercept Request ${url.host()} ${url}")
    if (url.toString().contains(".jpg") || url.toString().contains(".png")) return imageRequest(view, url)
    return null
}

fun WebResourceResponse?.filterCss(request: WebResourceRequest): WebResourceResponse?
        = this ?: if (request.url.path.endsWith(".css")) blankResource else null

fun imageRequest(view: WebView, url: HttpUrl): WebResourceResponse? {
    if (!BuildConfig.DEBUG) return null //disable for production
    return FrostImageResponse(view, url.toString())
}

class FrostImageResponse(view: WebView, url: String) : WebResourceResponse("", "", null) {

    val futureResponse = Single.create<InputStream> { emitter ->
        L.d("FUTURE 1 ${System.currentTimeMillis()} ${Thread.currentThread().id}")
//        GlideApp.with(view).as().load(url).into()
        val response = OkHttpClient().newCall(Request.Builder().url(url).build()).execute()
        emitter.onSuccess(response.body()!!.byteStream())
//        emitter.onSuccess(ByteArrayInputStream("".toByteArray()))
    }.toFuture()

    override fun getData(): InputStream {
        L.d("FUTURE 2 ${System.currentTimeMillis()}")
        return futureResponse.get()
    }
}

