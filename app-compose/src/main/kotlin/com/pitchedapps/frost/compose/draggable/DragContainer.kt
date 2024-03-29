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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import com.pitchedapps.frost.ext.toDpSize
import com.pitchedapps.frost.ext.toIntOffset

/*
 * Resources:
 *
 * https://blog.canopas.com/android-drag-and-drop-ui-element-in-jetpack-compose-14922073b3f1
 */

/**
 * Container for drag interactions.
 *
 * This must hold all drag and drop targets.
 *
 * If a composable cannot be used, any container can add the [Modifier.dragContainer] call, and have
 * a [DraggingContents] positioned in the same window space (ie in a Box, or with the appropriate
 * offsets to match).
 */
@Composable
fun <T> DragContainer(
  modifier: Modifier = Modifier,
  draggableState: DraggableState<T>,
  content: @Composable () -> Unit
) {
  Box(modifier = modifier.dragContainer(draggableState)) {
    content()

    DraggingContents(draggableState = draggableState)
  }
}

/**
 * Drag container modifier.
 *
 * Containers must hold all drag and drop targets, along with [DraggingContents].
 */
fun Modifier.dragContainer(draggableState: DraggableState<*>): Modifier {
  return onGloballyPositioned { draggableState.windowPosition = it.positionInWindow() }
}

/**
 * Drag target modifier.
 *
 * This should be applied to the composable that will be dragged. The modifier will capture
 * positions and hide (alpha 0) the target when dragging.
 */
fun Modifier.dragTarget(dragTargetState: DragTargetState<*>): Modifier {
  return onGloballyPositioned {
      dragTargetState.windowPosition = it.positionInWindow()
      dragTargetState.size = it.size
    }
    .pointerInput(dragTargetState) {
      detectDragGesturesAfterLongPress(
        onDragStart = { dragTargetState.onDragStart() },
        onDrag = { _, offset -> dragTargetState.onDrag(offset) },
        onDragEnd = { dragTargetState.onDragEnd() },
        onDragCancel = { dragTargetState.onDragEnd() },
      )
    }
    // We still need to draw to track size changes
    .alpha(if (dragTargetState.isDragging) 0f else 1f)
}

/**
 * Drop target modifier.
 *
 * This should be applied to targets that capture drag targets. The modifier will listen to
 * composable bounds.
 */
fun <T> Modifier.dropTarget(dropTargetState: DropTargetState<T>): Modifier {
  return onGloballyPositioned { dropTargetState.bounds = it.boundsInWindow() }
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
fun <T> DraggingContents(draggableState: DraggableState<T>) {
  for (target in draggableState.targets) {
    DraggingContent(draggableState = draggableState, target = target)
  }
}

@Composable
private fun <T> DraggingContent(draggableState: DraggableState<T>, target: DragTargetState<T>) {
  val density = LocalDensity.current
  Box(
    modifier =
      Modifier.size(target.size.toDpSize(density)).offset {
        (target.dragPosition - draggableState.windowPosition).toIntOffset()
      },
  ) {
    target.dragComposable()
  }
}
