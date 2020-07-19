/*
 * Copyright 2020 Allan Wang
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
package com.pitchedapps.frost.prefs.sections

import android.graphics.Color
import ca.allanwang.kau.kotlin.lazyResettable
import ca.allanwang.kau.kpref.KPref
import ca.allanwang.kau.kpref.KPrefFactory
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.isColorVisibleOn
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.enums.FACEBOOK_BLUE
import com.pitchedapps.frost.enums.Theme
import com.pitchedapps.frost.injectors.InjectorContract
import com.pitchedapps.frost.prefs.OldPrefs
import com.pitchedapps.frost.prefs.PrefsBase
import org.koin.core.KoinComponent
import org.koin.core.inject

interface ThemePrefs : PrefsBase {
    var theme: Int

    var customTextColor: Int

    var customAccentColor: Int

    var customBackgroundColor: Int

    var customHeaderColor: Int

    var customIconColor: Int

    val textColor: Int

    val accentColor: Int

    val accentColorForWhite: Int

    val nativeBgColor: Int

    fun nativeBgColor(unread: Boolean): Int

    val bgColor: Int

    val headerColor: Int

    val iconColor: Int

    val themeInjector: InjectorContract

    val isCustomTheme: Boolean

    var tintNavBar: Boolean
}

class ThemePrefsImpl(
    factory: KPrefFactory
) : KPref("${BuildConfig.APPLICATION_ID}.prefs.theme", factory),
    ThemePrefs, KoinComponent {

    private val oldPrefs: OldPrefs by inject()

    override var theme: Int by kpref("theme", oldPrefs.theme /* 0 */) { _: Int ->
        loader.invalidate()
    }

    override var customTextColor: Int by kpref(
        "color_text",
        oldPrefs.customTextColor /* 0xffeceff1.toInt() */
    )

    override var customAccentColor: Int by kpref(
        "color_accent",
        oldPrefs.customAccentColor /* 0xff0288d1.toInt() */
    )

    override var customBackgroundColor: Int by kpref(
        "color_bg",
        oldPrefs.customBackgroundColor /* 0xff212121.toInt() */
    )

    override var customHeaderColor: Int by kpref(
        "color_header",
        oldPrefs.customHeaderColor /* 0xff01579b.toInt() */
    )

    override var customIconColor: Int by kpref(
        "color_icons",
        oldPrefs.customIconColor /* 0xffeceff1.toInt() */
    )

    private val loader = lazyResettable { Theme.values[theme] }

    private val t: Theme by loader

    override val textColor: Int
        get() = t.textColorGetter(this)

    override val accentColor: Int
        get() = t.accentColorGetter(this)

    override val accentColorForWhite: Int
        get() = when {
            accentColor.isColorVisibleOn(Color.WHITE) -> accentColor
            textColor.isColorVisibleOn(Color.WHITE) -> textColor
            else -> FACEBOOK_BLUE
        }

    override val nativeBgColor: Int
        get() = bgColor.withAlpha(30)

    override fun nativeBgColor(unread: Boolean) = bgColor
        .colorToForeground(if (unread) 0.7f else 0.0f)
        .withAlpha(30)

    override val bgColor: Int
        get() = t.backgroundColorGetter(this)

    override val headerColor: Int
        get() = t.headerColorGetter(this)

    override val iconColor: Int
        get() = t.iconColorGetter(this)

    override val themeInjector: InjectorContract
        get() = t.injector

    override val isCustomTheme: Boolean
        get() = t == Theme.CUSTOM

    override var tintNavBar: Boolean by kpref("tint_nav_bar", oldPrefs.tintNavBar /* true */)
}
