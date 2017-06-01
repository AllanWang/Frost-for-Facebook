package com.pitchedapps.frost.events

import com.pitchedapps.frost.web.FrostWebView

/**
 * Created by Allan Wang on 2017-05-31.
 */
class WebEvent(val key: Int, val urlMatch: String? = null) {

    companion object {
        const val REFRESH = 0
        const val REFRESH_BASE = 1
    }

    fun execute(webView: FrostWebView) {
        if (urlMatch != null && !webView.url.contains(urlMatch)) return
        when (key) {
            REFRESH -> webView.reload()
            REFRESH_BASE -> webView.loadUrl(webView.baseUrl)
        }
    }
}