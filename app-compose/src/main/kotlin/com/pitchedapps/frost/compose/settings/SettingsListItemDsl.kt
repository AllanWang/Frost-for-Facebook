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

import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

fun SettingsDsl.settingsListDsl(content: SettingsListDsl.() -> Unit): List<@Composable () -> Unit> {
  val data = SettingsListDslData()
  data.content()
  return data.items
}

private class SettingsListDslData : SettingsListDsl {
  val items: MutableList<@Composable () -> Unit> = mutableListOf()

  private fun addCompose(content: @Composable () -> Unit) {
    items.add(content)
  }

  override fun item(
    title: String,
    enabled: Boolean,
    icon: ImageVector?,
    description: String?,
    onClick: (() -> Unit)?
  ) {
    addCompose {}
  }

  override fun description(text: String, icon: ImageVector?) {}

  override fun checkbox(
    title: String,
    enabled: Boolean,
    icon: ImageVector?,
    description: String?,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit
  ) {
    custom(
      icon = icon,
      title = title,
      enabled = enabled,
      description = description,
      onClick = { onCheckedChanged(!checked) },
    ) {
      Checkbox(
        enabled = enabled,
        checked = checked,
        onCheckedChange = onCheckedChanged,
      )
    }
  }

  override fun switch(
    title: String,
    enabled: Boolean,
    icon: ImageVector?,
    description: String?,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit
  ) {
    custom(
      icon = icon,
      title = title,
      enabled = enabled,
      description = description,
      onClick = { onCheckedChanged(!checked) },
    ) {
      Switch(
        enabled = enabled,
        checked = checked,
        onCheckedChange = onCheckedChanged,
      )
    }
  }

  override fun custom(
    title: String,
    enabled: Boolean,
    icon: ImageVector?,
    description: String?,
    onClick: (() -> Unit)?,
    content: @Composable () -> Unit
  ) {
    addCompose {
      SettingsListItem(
        icon = icon,
        title = title,
        enabled = enabled,
        description = description,
        onClick = onClick,
        content = content,
      )
    }
  }
}
