package com.pitchedapps.frost.injectors

import android.webkit.WebView
import com.pitchedapps.frost.utils.Prefs

/**
 * Small misc inline css assets
 */
enum class CssSmallAssets(private val content: String) : InjectorContract {
    FullSizeImage("div._4prr[style*=\"max-width\"][style*=\"max-height\"]{max-width:none !important;max-height:none !important}")
    ;

    val injector: JsInjector by lazy {
        JsBuilder().css(content).single("css-small-assets-$name").build()
    }

    override fun inject(webView: WebView, prefs: Prefs) {
        injector.inject(webView, prefs)
    }
}