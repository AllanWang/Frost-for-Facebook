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
package com.pitchedapps.frost.compose.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@DslMarker annotation class SettingsDslMarker

@SettingsDslMarker
interface SettingsDsl {
  /** Entry point to avoid cluttering the global namespace. */
  companion object : SettingsDsl
}

/** Dsl for creating individual entries in a list */
@SettingsDslMarker
interface SettingsListDsl {

  /**
   * Sub list with group title
   *
   * TODO support collapsed and/or shown?
   */
  //  fun group(title: String, enabled: Boolean = true, action: SettingsListDsl.() -> Unit)

  /** Generic item without content */
  fun item(
    title: String,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    description: String? = null,
    onClick: (() -> Unit)? = null
  )

  /** Long, non clickable content */
  fun description(text: String, icon: ImageVector? = null)

  fun checkbox(
    title: String,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    description: String? = null,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
  )

  fun switch(
    title: String,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    description: String? = null,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
  )

  fun custom(
    title: String,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
  )
}
