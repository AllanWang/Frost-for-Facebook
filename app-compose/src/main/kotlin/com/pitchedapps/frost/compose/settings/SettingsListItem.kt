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
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Checkbox as MaterialCheckbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pitchedapps.frost.ext.optionalCompose
import com.pitchedapps.frost.ext.thenIf

/** Basic building block for settings */
@Composable
fun SettingsListItem(
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  title: String,
  description: String? = null,
  content: (@Composable SettingsContentScope.() -> SettingsContent)? = null
) {
  val settingsContent = content?.invoke(SettingsContentScopeImpl)

  ListItem(
    modifier =
      modifier.thenIf(settingsContent != null) {
        Modifier.clickable(
          onClick = settingsContent!!::onClick,
        )
      },
    leadingContent =
      icon.optionalCompose {
        Icon(
          modifier = Modifier.size(24.dp),
          imageVector = it,
          contentDescription = null,
        )
      },
    headlineContent = { Text(text = title) },
    supportingContent = description.optionalCompose { Text(text = it) },
    trailingContent =
      settingsContent.takeIf { it !is SettingsContentClickOnly }.optionalCompose { it.compose() },
  )
}

@Preview
@Composable
private fun SettingsListItemPreview() {
  val state = remember { mutableStateOf(false) }
  MaterialTheme {
    SettingsListItem(
      icon = Icons.Outlined.Person,
      title = "Test Title",
      description = "Test Description",
    ) {
      checkbox(state.asSettingState())
    }
  }
}

private object SettingsContentScopeImpl : SettingsContentScope

@LayoutScopeMarker
interface SettingsContentScope {
  @Composable
  fun <T, R> T.rememberSetting(getter: T.() -> R, setter: T.(R) -> Unit): SettingState<R> {
    return remember(this) {
      object : SettingState<R> {
        override var value: R
          get() = getter()
          set(value) {
            setter(value)
          }
      }
    }
  }

  @Composable
  fun <T> MutableState<T>.asSettingState(): SettingState<T> {
    return remember(this) {
      object : SettingState<T> {
        override var value: T by this@asSettingState
      }
    }
  }
}

interface SettingsContent {

  fun onClick()

  @Composable fun compose()
}

private class SettingsContentClickOnly(private val action: () -> Unit) : SettingsContent {

  override fun onClick() {
    action()
  }

  @Composable final override fun compose() = Unit
}

@Stable
interface SettingState<T> {
  var value: T
}

fun SettingsContentScope.click(action: () -> Unit): SettingsContent =
  SettingsContentClickOnly(action)

fun SettingsContentScope.checkbox(state: SettingState<Boolean>): SettingsContent =
  object : SettingsContent {

    override fun onClick() {
      state.value = !state.value
    }

    @Composable
    override fun compose() {
      MaterialCheckbox(
        checked = state.value,
        onCheckedChange = { state.value = it },
      )
    }
  }
