package com.pitchedapps.frost.injectors

import android.webkit.WebView
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-05-31.
 * Mapping of the available assets
 * The enum name must match the css file name
 * //TODO add folder mapping using Prefs
 */
enum class CssAssets {
    HEADER, LOGIN
    ;

    var file = "${name.toLowerCase()}.compact.css"
    var injector: JsInjector? = null

    fun inject(webView: WebView, callback: ((String) -> Unit)?) {
        if (injector == null) {
            val content = webView.context.assets.open("core/$file").bufferedReader().use { it.readText() }
            injector = JsBuilder().css(content).build()
        }
        injector!!.inject(webView, callback)
        L.v("CSS ${injector!!.function}")
    }

    fun reset() {
        injector = null
    }

}