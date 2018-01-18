package com.pitchedapps.frost.injectors

import android.webkit.WebView
import ca.allanwang.kau.kotlin.lazyContext
import com.pitchedapps.frost.utils.L
import java.io.FileNotFoundException
import java.util.*

/**
 * Created by Allan Wang on 2017-05-31.
 * Mapping of the available assets
 * The enum name must match the css file name
 */
enum class JsAssets : InjectorContract {
    MENU, MENU_DEBUG, CLICK_A, CONTEXT_A, MEDIA, HEADER_BADGES, TEXTAREA_LISTENER, NOTIF_MSG,
    DOCUMENT_WATCHER
    ;

    var file = "${name.toLowerCase(Locale.CANADA)}.js"
    var injector = lazyContext {
        try {
            val content = it.assets.open("js/$file").bufferedReader().use { it.readText() }
            JsBuilder().js(content).single(name).build()
        } catch (e: FileNotFoundException) {
            L.e(e) { "JsAssets file not found" }
            JsInjector(JsActions.EMPTY.function)
        }
    }

    override fun inject(webView: WebView, callback: (() -> Unit)?) {
        injector(webView.context).inject(webView, callback)
    }

}
