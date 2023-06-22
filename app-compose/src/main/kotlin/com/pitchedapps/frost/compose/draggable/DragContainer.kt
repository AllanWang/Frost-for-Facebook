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

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import com.pitchedapps.frost.ext.toDpSize
import com.pitchedapps.frost.ext.toIntOffset

/*
 * Resources:
 *
 * https://blog.canopas.com/android-drag-and-drop-ui-element-in-jetpack-compose-14922073b3f1
 */

@Composable
fun DragContainer(
  modifier: Modifier = Modifier,
  draggableState: DraggableState,
  content: @Composable () -> Unit
) {
  Box(modifier = modifier) {
    content()

    DraggingContents(draggableState = draggableState)
  }
}

/**
 * Drag target.
 *
 * The [content] composable may be composed where [DragTarget] is defined, or in [DraggingContents]
 * depending on drag state. Keep this in mind based on the isDragging flag.
 *
 * [key] is used to distinguish between multiple dragging targets. If only one should be used at a
 * time, this can be nullable. If there is a key conflict, only the first target will be dragged.
 */
@Composable
fun DragTarget(
  key: String? = null,
  draggableState: DraggableState,
  content: @Composable BoxScope.(isDragging: Boolean) -> Unit
) {

  var isDragging by remember { mutableStateOf(false) }

  var positionInWindow by remember { mutableStateOf(Offset.Zero) }

  var size by remember { mutableStateOf(IntSize.Zero) }

  Box(
    modifier =
      Modifier.onGloballyPositioned {
          positionInWindow = it.positionInWindow()
          size = it.size
        }
        .pointerInput(Unit) {
          detectDragGesturesAfterLongPress(
            onDragStart = {
              if (draggableState.targets.containsKey(key)) {
                // We are already dragging an item with the same key, ignore
                isDragging = false
                return@detectDragGesturesAfterLongPress
              }
              isDragging = true

              draggableState.targets[key] =
                DraggingTargetState(
                  composable = content,
                  size = size,
                  dragPosition = positionInWindow,
                )
            },
            onDrag = { _, offset ->
              if (!isDragging) return@detectDragGesturesAfterLongPress
              val target = draggableState.targets[key] ?: return@detectDragGesturesAfterLongPress
              target.dragPosition += offset
            },
            onDragEnd = {
              if (isDragging) {
                draggableState.targets.remove(key)
              }
              isDragging = false
            },
          )
        },
  ) {
    if (!isDragging) {
      content(false)
    }
  }
}

/**
 * Draggable content.
 *
 * Provides composition for all dragging targets.
 *
 * For composing, we take the position provided by the dragging target. We also account for the
 * target size, as we want to maintain the same bounds here. As an example, the target can have
 * fillMaxWidth in a grid, but would have a full parent width here without the sizing constraints.
 */
@Composable
private fun DraggingContents(draggableState: DraggableState) {
  for (target in draggableState.targets.values) {
    DraggingContent(target = target)
  }
}

@Composable
private fun DraggingContent(target: DraggingTargetState) {
  val density = LocalDensity.current
  Box(
    modifier =
      Modifier.size(target.size.toDpSize(density)).offset { target.dragPosition.toIntOffset() },
  ) {
    target.composable(this, true)
  }
}
