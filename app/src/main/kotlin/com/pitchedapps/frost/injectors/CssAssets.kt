package com.pitchedapps.frost.injectors

import android.webkit.WebView
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-05-31.
 */
enum class CssAssets(f: String) {
    BASE("facebook");

    var file = "$f.compact.css"
    var content: String? = null
    var injector: JsInjector? = null

    fun inject(webView: WebView, callback: ((String) -> Unit)?) {
        if (injector == null) {
            if (content == null)
                content = webView.context.assets.open(file).bufferedReader().use { it.readText() }
            injector = JsBuilder().css(content!!).build()
        }
        injector!!.inject(webView, callback)
        L.d("CSS ${injector!!.function}")
    }

}