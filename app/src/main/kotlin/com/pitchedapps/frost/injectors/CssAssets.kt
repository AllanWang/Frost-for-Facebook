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
import ca.allanwang.kau.kotlin.lazyContext
import ca.allanwang.kau.utils.adjustAlpha
import ca.allanwang.kau.utils.colorToBackground
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.toRgbaString
import ca.allanwang.kau.utils.use
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.util.Locale

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
    private val injector = lazyContext {
        try {
            var content =
                it.assets.open("css/$folder/$file").bufferedReader().use(BufferedReader::readText)
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
                    .replace("\$AT\$", Prefs.iconColor.toRgbaString())
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

    override fun inject(webView: WebView) =
        injector(webView.context).inject(webView)

    fun reset() {
        injector.invalidate()
    }

    companion object {
        // Ensures that all non themes and the selected theme are loaded
        suspend fun load(context: Context) {
            withContext(Dispatchers.IO) {
                val currentTheme = Prefs.t.injector as? CssAssets
                val (themes, others) = CssAssets.values().partition { it.folder == THEME_FOLDER }
                themes.filter { it != currentTheme }.forEach { it.reset() }
                currentTheme?.injector?.invoke(context)
                others.forEach { it.injector.invoke(context) }
            }
        }
    }
}

private const val THEME_FOLDER = "themes"
