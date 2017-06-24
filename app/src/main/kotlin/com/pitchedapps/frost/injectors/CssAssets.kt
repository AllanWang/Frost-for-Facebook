package com.pitchedapps.frost.injectors

import android.webkit.WebView
import ca.allanwang.kau.utils.*
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-05-31.
 * Mapping of the available assets
 * The enum name must match the css file name
 */
enum class CssAssets(val folder: String = "themes") : InjectorContract {
    MATERIAL_LIGHT, MATERIAL_DARK, MATERIAL_AMOLED, MATERIAL_GLASS, CUSTOM, ROUND_ICONS("components")
    ;

    var file = "${name.toLowerCase()}.compact.css"
    var injector: JsInjector? = null

    override fun inject(webView: WebView, callback: ((String) -> Unit)?) {
        if (injector == null) {
            var content = webView.context.assets.open("css/$folder/$file").bufferedReader().use { it.readText() }
            if (this == CUSTOM) {
                content = content
                        .replace("\$T\$", Prefs.textColor.toRgbaString())
                        .replace("\$TT\$", Prefs.textColor.colorToBackground(0.05f).toRgbaString())
                        .replace("\$B\$", Prefs.bgColor.toRgbaString())
                        .replace("\$BBT\$", Prefs.bgColor.adjustAlpha(0.2f).colorToForeground(0.05f).toRgbaString())
                        .replace("\$O\$", Prefs.bgColor.withAlpha(255).toRgbaString())
                        .replace("\$D\$", Prefs.textColor.adjustAlpha(0.3f).toRgbaString())
            }
            injector = JsBuilder().css(content).build()
        }
        injector!!.inject(webView, callback)
    }

    fun reset() {
        injector = null
    }

}