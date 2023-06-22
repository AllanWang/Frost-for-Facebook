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

fun SettingsContentScope.checkbox(state: SettingState<Boolean>): SettingsContent =
  object : SettingsContent {

    override fun onClick() {
      state.value = !state.value
    }

    @Composable
    override fun compose() {
      Checkbox(
        enabled = state.enabled,
        checked = state.value,
        onCheckedChange = { state.value = it },
      )
    }
  }

fun SettingsContentScope.switch(
  state: SettingState<Boolean>,
): SettingsContent =
  object : SettingsContent {

    override fun onClick() {
      state.value = !state.value
    }

    @Composable
    override fun compose() {
      Switch(
        enabled = state.enabled,
        checked = state.value,
        onCheckedChange = { state.value = it },
      )
    }
  }
