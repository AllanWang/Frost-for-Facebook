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

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@Composable fun SettingsScreen() {}

@Preview
@Composable
fun SettingsScreenPreview() {

  data class Model(val check1: Boolean = false)

  var state by remember { mutableStateOf(Model()) }

  val composables: List<@Composable () -> Unit> = remember {
    listOf(
      {
        SettingsListItem(
          title = "Check 1",
        ) {
          checkbox(
            state.rememberSetting(
              getter = { state.check1 },
              setter = { state = state.copy(check1 = it) },
            ),
          )
        }
      },
      {
        SettingsListItem(
          title = "Check 1",
          description = "Linked again",
        ) {
          checkbox(
            state.rememberSetting(
              getter = { state.check1 },
              setter = { state = state.copy(check1 = it) },
            ),
          )
        }
      },
    )
  }

  MaterialTheme { LazyColumn { items(composables) { compose -> compose() } } }
}
