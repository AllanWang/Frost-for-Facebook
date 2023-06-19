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
package com.pitchedapps.frost.compose

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

/** Main Frost compose theme. */
@Composable
fun FrostTheme(
  isDarkTheme: Boolean = isSystemInDarkTheme(),
  isDynamicColor: Boolean = true,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val context = LocalContext.current
  val dynamicColor = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
  val colorScheme =
    remember(dynamicColor, isDarkTheme) {
      when {
        dynamicColor && isDarkTheme -> {
          dynamicDarkColorScheme(context)
        }
        dynamicColor && !isDarkTheme -> {
          dynamicLightColorScheme(context)
        }
        isDarkTheme -> darkColorScheme()
        else -> lightColorScheme()
      }
    }

  MaterialTheme(colorScheme = colorScheme) { Surface(modifier = modifier, content = content) }
}
