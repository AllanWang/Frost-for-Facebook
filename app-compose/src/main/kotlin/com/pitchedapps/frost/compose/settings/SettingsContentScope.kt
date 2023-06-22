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

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@LayoutScopeMarker
interface SettingsContentScope {
  @Composable
  fun <T, R> T.rememberSetting(
    enabler: T.() -> Boolean = { true },
    getter: T.() -> R,
    setter: T.(R) -> Unit
  ): SettingState<R> {
    return remember(this) {
      object : SettingState<R> {
        override val enabled: Boolean
          get() = enabler()

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
        override val enabled: Boolean = true
        override var value: T by this@asSettingState
      }
    }
  }
}

interface SettingsContent {

  fun onClick()

  @Composable fun compose()
}
