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

import ca.allanwang.kau.kpref.KPref
import ca.allanwang.kau.kpref.KPrefFactory
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.prefs.OldPrefs
import com.pitchedapps.frost.prefs.PrefsBase
import javax.inject.Inject

interface ThemePrefs : PrefsBase {
    var theme: Int

    var customTextColor: Int

    var customAccentColor: Int

    var customBackgroundColor: Int

    var customHeaderColor: Int

    var customIconColor: Int

    var tintNavBar: Boolean
}

class ThemePrefsImpl @Inject internal constructor(
    factory: KPrefFactory,
    oldPrefs: OldPrefs,
) : KPref("${BuildConfig.APPLICATION_ID}.prefs.theme", factory), ThemePrefs {

    /**
     * Note that this is purely for the pref storage. Updating themes should use
     * ThemeProvider
     */
    override var theme: Int by kpref("theme", oldPrefs.theme /* 0 */)

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

    override var tintNavBar: Boolean by kpref("tint_nav_bar", oldPrefs.tintNavBar /* true */)
}
