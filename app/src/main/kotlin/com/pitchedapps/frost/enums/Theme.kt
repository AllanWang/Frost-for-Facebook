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
package com.pitchedapps.frost.enums

import android.graphics.Color
import androidx.annotation.StringRes
import com.pitchedapps.frost.R
import com.pitchedapps.frost.injectors.CssAssets
import com.pitchedapps.frost.injectors.InjectorContract
import com.pitchedapps.frost.injectors.JsActions
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-06-14.
 */
const val FACEBOOK_BLUE = 0xff3b5998.toInt()
const val BLUE_LIGHT = 0xff5d86dd.toInt()

enum class Theme(
    @StringRes val textRes: Int,
    val injector: InjectorContract,
    private val textColorGetter: () -> Int,
    private val accentColorGetter: () -> Int,
    private val backgroundColorGetter: () -> Int,
    private val headerColorGetter: () -> Int,
    private val iconColorGetter: () -> Int
) {

    DEFAULT(R.string.kau_default,
        JsActions.EMPTY,
        { 0xde000000.toInt() },
        { FACEBOOK_BLUE },
        { 0xfffafafa.toInt() },
        { FACEBOOK_BLUE },
        { Color.WHITE }),

    LIGHT(R.string.kau_light,
        CssAssets.MATERIAL_LIGHT,
        { 0xde000000.toInt() },
        { FACEBOOK_BLUE },
        { 0xfffafafa.toInt() },
        { FACEBOOK_BLUE },
        { Color.WHITE }),

    DARK(R.string.kau_dark,
        CssAssets.MATERIAL_DARK,
        { Color.WHITE },
        { BLUE_LIGHT },
        { 0xff303030.toInt() },
        { 0xff2e4b86.toInt() },
        { Color.WHITE }),

    AMOLED(R.string.kau_amoled,
        CssAssets.MATERIAL_AMOLED,
        { Color.WHITE },
        { BLUE_LIGHT },
        { Color.BLACK },
        { Color.BLACK },
        { Color.WHITE }),

    GLASS(R.string.kau_glass,
        CssAssets.MATERIAL_GLASS,
        { Color.WHITE },
        { BLUE_LIGHT },
        { 0x80000000.toInt() },
        { 0xb3000000.toInt() },
        { Color.WHITE }),

    CUSTOM(R.string.kau_custom,
        CssAssets.CUSTOM,
        { Prefs.customTextColor },
        { Prefs.customAccentColor },
        { Prefs.customBackgroundColor },
        { Prefs.customHeaderColor },
        { Prefs.customIconColor });

    val textColor: Int
        get() = textColorGetter()

    val accentColor: Int
        get() = accentColorGetter()

    val bgColor: Int
        get() = backgroundColorGetter()

    val headerColor: Int
        get() = headerColorGetter()

    val iconColor: Int
        get() = iconColorGetter()

    companion object {
        val values = values() //save one instance
        operator fun invoke(index: Int) = values[index]
    }
}
