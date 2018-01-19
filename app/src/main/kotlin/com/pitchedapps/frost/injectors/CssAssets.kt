package com.pitchedapps.frost.injectors

import android.graphics.Color
import android.webkit.WebView
import ca.allanwang.kau.kotlin.lazyContext
import ca.allanwang.kau.utils.*
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.util.*

/**
 * Created by Allan Wang on 2017-05-31.
 * Mapping of the available assets
 * The enum name must match the css file name
 */
enum class CssAssets(val folder: String = "themes") : InjectorContract {
    MATERIAL_LIGHT, MATERIAL_DARK, MATERIAL_AMOLED, MATERIAL_GLASS, CUSTOM, ROUND_ICONS("components")
    ;

    var file = "${name.toLowerCase(Locale.CANADA)}.css"
    var injector = lazyContext {
        try {
            var content = it.assets.open("css/$folder/$file").bufferedReader().use(BufferedReader::readText)
            if (this == CUSTOM) {
                val bt = if (Color.alpha(Prefs.bgColor) == 255)
                    Prefs.bgColor.toRgbaString()
                else
                    "transparent"

                val bb = Prefs.bgColor.colorToForeground(0.35f)

                content = content
                        .replace("\$T\$", Prefs.textColor.toRgbaString())
                        .replace("\$TT\$", Prefs.textColor.colorToBackground(0.05f).toRgbaString())
                        .replace("\$A\$", Prefs.accentColor.toRgbaString())
                        .replace("\$B\$", Prefs.bgColor.toRgbaString())
                        .replace("\$BT\$", bt)
                        .replace("\$BBT\$", bb.withAlpha(51).toRgbaString())
                        .replace("\$O\$", Prefs.bgColor.withAlpha(255).toRgbaString())
                        .replace("\$OO\$", bb.withAlpha(255).toRgbaString())
                        .replace("\$D\$", Prefs.textColor.adjustAlpha(0.3f).toRgbaString())
                        .replace("\$TI\$", bb.withAlpha(60).toRgbaString())
                        .replace("\$C\$", bt)
            }
            JsBuilder().css(content).build()
        } catch (e: FileNotFoundException) {
            L.e(e) { "CssAssets file not found" }
            JsInjector(JsActions.EMPTY.function)
        }
    }

    override fun inject(webView: WebView, callback: (() -> Unit)?) {
        injector(webView.context).inject(webView, callback)
    }

    fun reset() {
        injector.invalidate()
    }

}
