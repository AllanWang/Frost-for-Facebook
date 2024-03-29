/*
 * Copyright 2023 Allan Wang
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
package com.pitchedapps.frost.main

import androidx.compose.ui.graphics.vector.ImageVector
import com.pitchedapps.frost.ext.WebTargetId
import com.pitchedapps.frost.tabselector.TabData

/** Data representation of a single main tab entry. */
data class MainTabItem(
  val id: WebTargetId,
  val title: String,
  val icon: ImageVector,
  val url: String
)

fun MainTabItem.toTab() =
  TabData(
    icon = icon,
    title = title,
    key = id.id,
  )
