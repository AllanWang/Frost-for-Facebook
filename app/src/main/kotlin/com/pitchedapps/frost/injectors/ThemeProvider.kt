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
import ca.allanwang.kau.utils.adjustAlpha
import ca.allanwang.kau.utils.colorToBackground
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.isColorVisibleOn
import ca.allanwang.kau.utils.toRgbaString
import ca.allanwang.kau.utils.use
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.enums.FACEBOOK_BLUE
import com.pitchedapps.frost.enums.Theme
import com.pitchedapps.frost.enums.ThemeCategory
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.L
import java.io.BufferedReader
import java.io.FileNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext

/**
 * Provides [InjectorContract] for each [ThemeCategory].
 * Can be reloaded to take in changes from [Prefs]
 */
class ThemeProvider(private val context: Context, private val prefs: Prefs) {

    private var theme: Theme = Theme.values[prefs.theme]

    private val injectors: MutableMap<ThemeCategory, InjectorContract> = mutableMapOf()

    val textColor: Int
        get() = theme.textColorGetter(prefs)

    val accentColor: Int
        get() = theme.accentColorGetter(prefs)

    val accentColorForWhite: Int
        get() = when {
            accentColor.isColorVisibleOn(Color.WHITE) -> accentColor
            textColor.isColorVisibleOn(Color.WHITE) -> textColor
            else -> FACEBOOK_BLUE
        }

    val nativeBgColor: Int
        get() = bgColor.withAlpha(30)

    fun nativeBgColor(unread: Boolean) = bgColor
        .colorToForeground(if (unread) 0.7f else 0.0f)
        .withAlpha(30)

    val bgColor: Int
        get() = theme.backgroundColorGetter(prefs)

    val headerColor: Int
        get() = theme.headerColorGetter(prefs)

    val iconColor: Int
        get() = theme.iconColorGetter(prefs)

    val isCustomTheme: Boolean
        get() = theme == Theme.CUSTOM

    /**
     * Note that while this can be loaded from any thread, it is typically done through [preload]]
     */
    fun injector(category: ThemeCategory): InjectorContract =
        injectors.getOrPut(category) { createInjector(category) }

    /**
     * Note that while this can be loaded from any thread, it is typically done through [preload]
     */
    private fun createInjector(category: ThemeCategory): InjectorContract {
        val file = theme.file ?: return JsActions.EMPTY
        try {
            var content =
                context.assets.open("css/${category.folder}/themes/$file").bufferedReader()
                    .use(BufferedReader::readText)
            if (theme == Theme.CUSTOM) {
                val bt = if (Color.alpha(bgColor) == 255)
                    bgColor.toRgbaString()
                else
                    "transparent"

                val bb = bgColor.colorToForeground(0.35f)

                content = content
                    .replace("\$T\$", textColor.toRgbaString())
                    .replace("\$TT\$", textColor.colorToBackground(0.05f).toRgbaString())
                    .replace("\$TD\$", textColor.adjustAlpha(0.6f).toRgbaString())
                    .replace("\$A\$", accentColor.toRgbaString())
                    .replace("\$AT\$", iconColor.toRgbaString())
                    .replace("\$B\$", bgColor.toRgbaString())
                    .replace("\$BT\$", bt)
                    .replace("\$BBT\$", bb.withAlpha(51).toRgbaString())
                    .replace("\$O\$", bgColor.withAlpha(255).toRgbaString())
                    .replace("\$OO\$", bb.withAlpha(255).toRgbaString())
                    .replace("\$D\$", textColor.adjustAlpha(0.3f).toRgbaString())
                    .replace("\$TI\$", bb.withAlpha(60).toRgbaString())
                    .replace("\$C\$", bt)
            }
            return JsBuilder().css(content).build()
        } catch (e: FileNotFoundException) {
            L.e(e) { "CssAssets file not found" }
            return JsActions.EMPTY
        }
    }

    fun setTheme(id: Int) {
        theme = Theme.values[id]
        reset()
    }

    fun reset() {
        injectors.clear()
    }

    suspend fun preload() {
        withContext(Dispatchers.IO) {
            reset()
            ThemeCategory.values().forEach { injector(it) }
        }
    }

    companion object {

        fun get(): ThemeProvider = GlobalContext.get().get()

        fun module() = org.koin.dsl.module {
            single { ThemeProvider(get(), get()) }
        }
    }
}
