package com.pitchedapps.frost.injectors

import android.webkit.WebView
import com.pitchedapps.frost.utils.L
import java.io.FileNotFoundException
import java.util.*

/**
 * Created by Allan Wang on 2017-05-31.
 * Mapping of the available assets
 * The enum name must match the css file name
 */
enum class JsAssets : InjectorContract {
    MENU, MENU_DEBUG, CLICK_A, CONTEXT_A, HEADER_BADGES, SEARCH, TEXTAREA_LISTENER, NOTIF_MSG
    ;

    var file = "${name.toLowerCase(Locale.CANADA)}.min.js"
    var injector: JsInjector? = null

    override fun inject(webView: WebView, callback: ((String) -> Unit)?) {
        if (injector == null) {
            try {
                val content = webView.context.assets.open("js/$file").bufferedReader().use { it.readText() }
                injector = JsBuilder().js(content).build()
            } catch (e: FileNotFoundException) {
                L.e(e, "JsAssets file not found")
                injector = JsInjector(JsActions.EMPTY.function)
            }
        }
        injector!!.inject(webView, callback)
    }
}
