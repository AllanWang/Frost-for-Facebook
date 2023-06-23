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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pitchedapps.frost.compose.FrostPreview
import com.pitchedapps.frost.ext.optionalCompose
import com.pitchedapps.frost.ext.thenIf

/** Basic building block for settings */
@Composable
fun SettingsListItem(
  modifier: Modifier = Modifier,
  title: String,
  enabled: Boolean = true,
  icon: ImageVector? = null,
  description: String? = null,
  onClick: (() -> Unit)? = null,
  content: (@Composable () -> Unit)? = null
) {
  val alpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
  ListItem(
    modifier =
      modifier.thenIf(onClick != null) {
        Modifier.clickable(enabled = enabled) { onClick?.invoke() }
      },
    leadingContent =
      icon.optionalCompose {
        Icon(
          modifier = Modifier.size(24.dp).alpha(alpha),
          imageVector = it,
          contentDescription = null,
        )
      },
    headlineContent = { Text(modifier = Modifier.alpha(alpha), text = title) },
    supportingContent =
      description.optionalCompose {
        Text(
          modifier = Modifier.alpha(alpha),
          text = it,
        )
      },
    trailingContent = content,
  )
}

@Preview
@Composable
private fun SettingsListItemPreview() {
  var state by remember { mutableStateOf(false) }

  FrostPreview {
    SettingsListItem(
      icon = Icons.Outlined.Person,
      title = "Test Title",
      description = "Test Description",
    ) {
      Checkbox(
        checked = state,
        onCheckedChange = { state = it },
      )
    }
  }
}
