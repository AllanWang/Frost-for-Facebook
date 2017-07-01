package com.pitchedapps.frost.injectors

import android.webkit.WebView
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-05-31.
 * Mapping of the available assets
 * The enum name must match the css file name
 */
enum class JsAssets : InjectorContract {
    MENU, CLICK_A, CLICK_INTERCEPTOR, HEADER_BADGES, SEARCH
    ;

    var file = "${name.toLowerCase()}.min.js"
    var injector: JsInjector? = null

    override fun inject(webView: WebView, callback: ((String) -> Unit)?) {
        if (injector == null) {
            val content = webView.context.assets.open("js/$file").bufferedReader().use { it.readText() }
            injector = JsBuilder().js(content).build()
        }
        injector!!.inject(webView, callback)
    }

    fun reset() {
        injector = null
    }

}
