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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize

@Composable
fun rememberDraggableState(): DraggableState {
  return remember { DraggableStateImpl() }
}

interface DraggableState {

  val targets: Collection<DraggingTargetState>

  /**
   * Being drag for target [key].
   *
   * If there is another target with the same key being dragged, we will ignore this request.
   *
   * Returns true if the request is accepted. It is the caller's responsibility to not propagate
   * drag events if the request is denied.
   */
  fun onDragStart(key: String, targetState: () -> DraggingTargetState): Boolean

  fun onDrag(key: String, offset: Offset)

  fun onDragEnd(key: String)

  fun onDropUpdateBounds(key: String, bounds: Rect)

  fun dropTarget(key: String): DropTargetState?
}

class DraggableStateImpl : DraggableState {
  private val dragTargets = mutableStateMapOf<String, DraggingTargetState>()

  private val dropTargets = mutableStateMapOf<String, DropTargetState>()

  override val targets: Collection<DraggingTargetState>
    get() = dragTargets.values

  override fun onDragStart(key: String, targetState: () -> DraggingTargetState): Boolean {
    if (key in dragTargets) return false
    dragTargets[key] = targetState()
    return true
  }

  override fun onDrag(key: String, offset: Offset) {
    val position = dragTargets[key] ?: return
    position.dragPosition += offset
    checkForDrag(key)
  }

  override fun onDragEnd(key: String) {
    dragTargets.remove(key)
    for ((dropKey, dropTarget) in dropTargets) {
      if (dropTarget.hoverKey == key) {
        setHover(dragKey = null, dropKey)
        // Check other drag targets in case one meets drag requirements
        checkForDrop(dropKey)
      }
    }
  }

  override fun onDropUpdateBounds(key: String, bounds: Rect) {
    dropTargets.getOrPut(key) { DropTargetState() }.bounds = bounds
    checkForDrop(key)
  }

  override fun dropTarget(key: String): DropTargetState? {
    return dropTargets[key]
  }

  private fun setHover(dragKey: String?, dropKey: String) {
    val dropTarget = dropTargets[dropKey] ?: return
    dropTarget.hoverKey = dragKey
    println("asdf update $dragKey $dropKey")
  }

  /** Returns true if drag target exists and is within bounds */
  private fun DropTargetState.hasValidDragTarget(): Boolean {
    val currentKey = hoverKey ?: return false // no target
    val dragTarget = dragTargets[currentKey] ?: return false // target not valid
    return dragTarget.within(bounds)
  }

  /** Check if drag target fits in drop */
  private fun checkForDrop(dropKey: String) {
    val dropTarget = dropTargets[dropKey] ?: return
    val bounds = dropTarget.bounds
    if (dropTarget.hasValidDragTarget()) return

    // Find first target that matches
    val dragKey = dragTargets.entries.firstOrNull { it.value.within(bounds) }?.key
    setHover(dragKey = dragKey, dropKey = dropKey)
  }

  /** Check drops for drag target fit */
  private fun checkForDrag(dragKey: String) {
    val dragTarget = dragTargets[dragKey] ?: return
    for ((dropKey, dropTarget) in dropTargets) {
      // Do not override targets that are valid
      if (dropTarget.hasValidDragTarget()) continue
      if (dragTarget.within(dropTarget.bounds)) {
        setHover(dragKey = dragKey, dropKey = dropKey)
      } else if (dropTarget.hoverKey == dragKey) {
        setHover(dragKey = null, dropKey = dropKey)
      }
    }
  }
}

private fun DraggingTargetState?.within(bounds: Rect): Boolean {
  if (this == null) return false
  val center = dragPosition + Offset(size.width * 0.5f, size.height * 0.5f)
  return bounds.contains(center)
}

/** State for individual dragging target. */
class DraggingTargetState(
  val composable: @Composable BoxScope.(isDragging: Boolean) -> Unit,
  val size: IntSize,
  dragPosition: Offset
) {
  var dragPosition by mutableStateOf(dragPosition)
}

class DropTargetState {
  var hoverKey: String? by mutableStateOf(null)
  var hoverData: String? by mutableStateOf(null)
  var bounds: Rect = Rect.Zero
}
