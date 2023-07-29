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

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pitchedapps.frost.compose.FrostPreview

@Composable
fun SettingsList(
  modifier: Modifier = Modifier,
  data: List<SettingsListItemData>,
) {
  LazyColumn(modifier = modifier) { items(data) { SettingsListItem(data = it) } }
}

@Preview
@Composable
fun SettingsListPreview() {

  @Immutable
  data class Model(
    val check1: Boolean = false,
    val switch1: Boolean = false,
    val switch2: Boolean = false,
  )

  var state by remember { mutableStateOf(Model()) }

  val data by remember {
    derivedStateOf {
      listOf(
        SettingsListItemData.Checkbox(
          title = "Check 1",
          checked = state.check1,
          onCheckChanged = { state = state.copy(check1 = it) },
        ),
        SettingsListItemData.Checkbox(
          title = "Check 1",
          description = "Linked again",
          checked = state.check1,
          onCheckChanged = { state = state.copy(check1 = it) },
        ),
        SettingsListItemData.Switch(
          title = "Switch 1",
          checked = state.switch1,
          onCheckChanged = { state = state.copy(switch1 = it) },
        ),
        SettingsListItemData.Switch(
          title = "Switch 2",
          enabled = state.switch1,
          description = "Enabled by switch 1",
          checked = state.switch2,
          onCheckChanged = { state = state.copy(switch2 = it) },
        ),
      )
    }
  }

  FrostPreview {
    SettingsList(modifier = Modifier.systemBarsPadding(), data = data)
  }
}
