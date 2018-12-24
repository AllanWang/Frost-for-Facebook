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
package com.pitchedapps.frost.contracts

import android.view.View
import android.widget.TextView

/**
 * Created by Allan Wang on 2017-11-07.
 *
 * Should be implemented by all views in [com.pitchedapps.frost.activities.MainActivity]
 * to allow for instant view reloading
 */
interface FrostThemable {

    /**
     * Change all necessary view components to the new theme
     * and call whatever other children that also implement [FrostThemable]
     */
    fun reloadTheme()

    fun setTextColors(color: Int, vararg textViews: TextView?) =
        themeViews(color, *textViews) { setTextColor(it) }

    fun setBackgrounds(color: Int, vararg views: View?) =
        themeViews(color, *views) { setBackgroundColor(it) }

    fun <T : View> themeViews(color: Int, vararg views: T?, action: T.(Int) -> Unit) =
        views.filterNotNull().forEach { it.action(color) }
}
