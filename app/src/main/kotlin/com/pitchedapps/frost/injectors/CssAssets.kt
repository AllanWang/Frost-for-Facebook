/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.injectors

import android.content.Context
import android.graphics.Color
import android.webkit.WebView
import androidx.annotation.VisibleForTesting
import ca.allanwang.kau.utils.adjustAlpha
import ca.allanwang.kau.utils.colorToBackground
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.toRgbaString
import ca.allanwang.kau.utils.use
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.L
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Allan Wang on 2017-05-31.
 * Mapping of the available assets
 * The enum name must match the css file name
 */
enum class CssAssets(val folder: String = THEME_FOLDER) : InjectorContract {
    MATERIAL_LIGHT, MATERIAL_DARK, MATERIAL_AMOLED, MATERIAL_GLASS, CUSTOM
    ;

    @VisibleForTesting
    internal val file = "${name.toLowerCase(Locale.CANADA)}.css"

    /**
     * Note that while this can be loaded from any thread, it is typically done through [load]
     */
    private var injector: JsInjector? = null

    private fun injector(context: Context, prefs: Prefs): JsInjector =
        injector ?: createInjector(context, prefs).also { injector = it }

    /**
     * Note that while this can be loaded from any thread, it is typically done through [load]
     */
    private fun createInjector(context: Context, prefs: Prefs): JsInjector =
        try {
            var content =
                context.assets.open("css/$folder/$file").bufferedReader()
                    .use(BufferedReader::readText)
            if (this == CUSTOM) {
                val bt = if (Color.alpha(prefs.bgColor) == 255)
                    prefs.bgColor.toRgbaString()
                else
                    "transparent"

                val bb = prefs.bgColor.colorToForeground(0.35f)

                content = content
                    .replace("\$T\$", prefs.textColor.toRgbaString())
                    .replace("\$TT\$", prefs.textColor.colorToBackground(0.05f).toRgbaString())
                    .replace("\$A\$", prefs.accentColor.toRgbaString())
                    .replace("\$AT\$", prefs.iconColor.toRgbaString())
                    .replace("\$B\$", prefs.bgColor.toRgbaString())
                    .replace("\$BT\$", bt)
                    .replace("\$BBT\$", bb.withAlpha(51).toRgbaString())
                    .replace("\$O\$", prefs.bgColor.withAlpha(255).toRgbaString())
                    .replace("\$OO\$", bb.withAlpha(255).toRgbaString())
                    .replace("\$D\$", prefs.textColor.adjustAlpha(0.3f).toRgbaString())
                    .replace("\$TI\$", bb.withAlpha(60).toRgbaString())
                    .replace("\$C\$", bt)
            }
            JsBuilder().css(content).build()
        } catch (e: FileNotFoundException) {
            L.e(e) { "CssAssets file not found" }
            JsInjector(JsActions.EMPTY.function)
        }

    override fun inject(webView: WebView, prefs: Prefs) =
        injector(webView.context, prefs).inject(webView, prefs)

    fun reset() {
        injector = null
    }

    companion object {

        // Ensures that all non themes and the selected theme are loaded
        suspend fun load(context: Context, prefs: Prefs) {
            withContext(Dispatchers.IO) {
                val currentTheme = prefs.themeInjector as? CssAssets
                val (themes, others) = values().partition { it.folder == THEME_FOLDER }
                themes.filter { it != currentTheme }.forEach { it.reset() }
                currentTheme?.injector(context, prefs)
                others.forEach { it.injector(context, prefs) }
            }
        }
    }
}

private const val THEME_FOLDER = "themes"
