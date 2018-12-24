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

import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-08-19.
 */
enum class MainActivityLayout(
    val titleRes: Int,
    val layoutRes: Int,
    val backgroundColor: () -> Int,
    val iconColor: () -> Int
) {

    TOP_BAR(R.string.top_bar,
        R.layout.activity_main,
        { Prefs.headerColor },
        { Prefs.iconColor }),

    BOTTOM_BAR(R.string.bottom_bar,
        R.layout.activity_main_bottom_tabs,
        { Prefs.bgColor },
        { Prefs.textColor });

    companion object {
        val values = values() //save one instance
        operator fun invoke(index: Int) = values[index]
    }
}
