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

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/*
 * Resources:
 *
 * https://blog.canopas.com/android-drag-and-drop-ui-element-in-jetpack-compose-14922073b3f1
 */

@Composable
fun DragContainer(modifier: Modifier = Modifier, content: @Composable DragScope.() -> Unit) {
  val draggable = remember { Draggable() }
  val dragScope = remember(draggable) { DragScopeImpl(draggable) }
  Box(modifier = modifier) {
    dragScope.content()

    DraggingContent(draggable = draggable)
  }
}

private class DragScopeImpl(private val draggable: Draggable) : DragScope {
  @Composable
  override fun DragTarget(
    key: String,
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
                isDragging = true
                draggable.composable = content
                draggable.composableSize = size
                draggable.dragPosition = positionInWindow
              },
              onDrag = { _, offset -> draggable.dragPosition += offset },
              onDragEnd = {
                isDragging = false
                draggable.composable = null
              },
            )
          },
    ) {
      if (!isDragging) {
        content(false)
      }
    }
  }
}

private fun IntSize.toDpSize(density: Density): DpSize {
  return with(density) { DpSize(width.toDp(), height.toDp()) }
}

@Composable
private fun DraggingContent(draggable: Draggable) {
  val composable = draggable.composable ?: return

  val density = LocalDensity.current
  val sizeDp =
    remember { derivedStateOf { draggable.composableSize?.toDpSize(density) } }.value ?: return

  Box(
    modifier = Modifier.size(sizeDp).offset { draggable.dragPosition.toIntOffset() },
  ) {
    composable(true)
  }
}

private fun Offset.toIntOffset() = IntOffset(x.roundToInt(), y.roundToInt())

@LayoutScopeMarker
@Immutable
interface DragScope {
  @Composable
  fun DragTarget(key: String, content: @Composable BoxScope.(isDragging: Boolean) -> Unit)
}

class Draggable {
  var composable by mutableStateOf<(@Composable BoxScope.(isDragging: Boolean) -> Unit)?>(null)
  var composableSize by mutableStateOf<IntSize?>(null)
  var dragPosition by mutableStateOf(Offset.Zero)
}

internal val LocalDraggable = compositionLocalOf {}
