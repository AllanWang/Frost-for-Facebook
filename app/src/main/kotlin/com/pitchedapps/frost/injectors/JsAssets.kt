package com.pitchedapps.frost.injectors

import android.webkit.WebView

/**
 * Created by Allan Wang on 2017-05-31.
 * Mapping of the available assets
 * The enum name must match the css file name
 */
enum class JsAssets : InjectorContract {
    MENU, CLICK_A, CONTEXT_A, HEADER_BADGES, SEARCH, TEXTAREA_LISTENER
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
}
