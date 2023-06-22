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
package com.pitchedapps.frost.compose.draggable

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

@Composable
fun rememberDraggableState(): DraggableState {
  return remember { DraggableState() }
}

@Stable
class DraggableState {
  val targets = mutableStateMapOf<String?, DraggingTargetState>()
}

/** State for individual dragging target. */
class DraggingTargetState(
  val composable: @Composable BoxScope.(isDragging: Boolean) -> Unit,
  val size: IntSize,
  dragPosition: Offset
) {
  var dragPosition by mutableStateOf(dragPosition)
}
