package com.pitchedapps.frost.web

import android.webkit.WebView
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.injectors.jsInject

/**
 * Created by Allan Wang on 2017-05-31.
 */
class FrostWebViewClientMenu(webCore: FrostWebViewCore) : FrostWebViewClient(webCore) {

    private val String.shouldInjectMenu
    get() = when (removePrefix(FB_URL_BASE)) {
        "settings",
        "settings#",
        "settings#!/settings?soft=bookmarks" -> true
        else -> false
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        if (url.shouldInjectMenu) jsInject(JsAssets.MENU)
    }

    override fun emit(flag: Int) {
        super.emit(flag)
        super.injectAndFinish()
    }

    override fun onPageFinishedActions(url: String) {
        if (!url.shouldInjectMenu) injectAndFinish()
    }

}