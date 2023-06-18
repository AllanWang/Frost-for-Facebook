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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.pitchedapps.frost.ext.components
import com.pitchedapps.frost.extension.FrostCoreExtension

@Composable
fun FrostCoreExtensionEffect() {
  val components = LocalContext.current.components

  DisposableEffect(components.core.store) {
    val feature =
      FrostCoreExtension(
        runtime = components.core.engine,
        store = components.core.store,
        converter = components.extensionModelConverter,
      )

    feature.start()

    onDispose { feature.stop() }
  }
}
