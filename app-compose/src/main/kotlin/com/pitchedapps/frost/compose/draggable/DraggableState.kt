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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize

/** Callback when a drag completes on top of a target. */
fun interface OnDrop<T> {
  fun onDrop(dragTarget: String, dragData: T, dropTarget: String)
}

/** Create draggable state, which will store and create all target states. */
@Composable
fun <T> rememberDraggableState(onDrop: OnDrop<T>): DraggableState<T> {
  // State must be remembered without keys, or else updates will clear draggable data
  val state = remember { DraggableStateImpl(onDrop) }

  LaunchedEffect(onDrop) { state.onDrop = onDrop }

  return state
}

/**
 * Parent draggable state.
 *
 * Allows for drag and drop target state creation via [dragTarget] and [dragTarget].
 *
 * All public getters are states, and will trigger recomposition automatically.
 */
interface DraggableState<T> {

  var windowPosition: Offset

  val targets: Collection<DragTargetState<T>>

  @Composable
  fun rememberDragTarget(key: String, data: T, content: @Composable () -> Unit): DragTargetState<T>

  @Composable fun rememberDropTarget(key: String): DropTargetState<T>
}

interface DragTargetState<T> {
  /**
   * Identifier for drag target.
   *
   * This is not necessarily unique, but only unique drag target keys may be dragged at a time. Ie,
   * if a group of drag targets with the same key is dragging, only the first one will activate.
   */
  val key: String

  /** Data associated with this drag target. */
  val data: T

  /** True if target is actively being dragged. */
  val isDragging: Boolean

  /** Window position for target. */
  var windowPosition: Offset

  /** Size bounds for target. */
  var size: IntSize

  /** Drag position relative to window. This is only valid while [isDragging]. */
  val dragPosition: Offset

  /** Composable to use when dragging. */
  val dragComposable: @Composable () -> Unit

  /**
   * Begin drag for target.
   *
   * If there is another target with the same [key] being dragged, we will ignore this request.
   *
   * Returns true if the request is accepted. It is the caller's responsibility to not propagate
   * drag events if the request is denied.
   */
  fun onDragStart()

  fun onDrag(offset: Offset)

  fun onDragEnd()
}

interface DropTargetState<T> {
  /**
   * True if there is a drag target above this target.
   *
   * [hoverData] will be populated when true.
   */
  val isHovered: Boolean

  /** Associated [DragTargetState.data], or null when not [isHovered] */
  val hoverData: T?

  /** Bounds of the drop target composable */
  var bounds: Rect
}

private class DraggableStateImpl<T>(var onDrop: OnDrop<T>) : DraggableState<T> {

  override var windowPosition: Offset by mutableStateOf(Offset.Zero)

  val activeDragTargets = mutableStateMapOf<String, DragTargetStateImpl<T>>()

  private val dropTargets = mutableStateMapOf<String, DropTargetStateImpl<T>>()

  override val targets: Collection<DragTargetState<T>>
    get() = activeDragTargets.values

  fun cleanUpDrag(dragTarget: DragTargetStateImpl<T>) {
    val dragKey = dragTarget.key
    for ((dropKey, dropTarget) in dropTargets) {
      if (dropTarget.hoverKey == dragKey) {
        onDrop.onDrop(dragTarget = dragKey, dragData = dragTarget.data, dropTarget = dropKey)

        setHover(dragKey = null, dropKey)
        // Check other drag targets in case one meets drag requirements
        checkForDrop(dropKey)
      }
    }
  }

  @Composable
  override fun rememberDragTarget(
    key: String,
    data: T,
    content: @Composable () -> Unit,
  ): DragTargetState<T> {
    val target =
      remember(key, data, content, this) {
        DragTargetStateImpl(key = key, data = data, draggableState = this, dragComposable = content)
      }
    DisposableEffect(target) { onDispose { activeDragTargets.remove(key) } }
    return target
  }

  @Composable
  override fun rememberDropTarget(key: String): DropTargetState<T> {
    val target = remember(key, this) { DropTargetStateImpl(key, this) }
    DisposableEffect(target) {
      dropTargets[key] = target

      onDispose { dropTargets.remove(key) }
    }
    return target
  }

  private fun setHover(dragKey: String?, dropKey: String) {
    val dropTarget = dropTargets[dropKey] ?: return
    // Safety check; we only want to register active keys
    val dragTarget = if (dragKey != null) activeDragTargets[dragKey] else null
    dropTarget.hoverKey = dragTarget?.key
    dropTarget.hoverData = dragTarget?.data
  }

  /** Returns true if drag target exists and is within bounds */
  private fun DropTargetStateImpl<T>.hasValidDragTarget(): Boolean {
    val currentKey = hoverKey ?: return false // no target
    val dragTarget = activeDragTargets[currentKey] ?: return false // target not valid
    return dragTarget.within(bounds)
  }

  /** Check if drag target fits in drop */
  fun checkForDrop(dropKey: String) {
    val dropTarget = dropTargets[dropKey] ?: return
    val bounds = dropTarget.bounds
    if (dropTarget.hasValidDragTarget()) return

    // Find first target that matches
    val dragKey = activeDragTargets.entries.firstOrNull { it.value.within(bounds) }?.key
    setHover(dragKey = dragKey, dropKey = dropKey)
  }

  /** Check drops for drag target fit */
  fun checkForDrag(dragKey: String) {
    val dragTarget = activeDragTargets[dragKey] ?: return
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

private fun DragTargetStateImpl<*>?.within(bounds: Rect): Boolean {
  if (this == null) return false
  val center = dragPosition + Offset(size.width * 0.5f, size.height * 0.5f)
  return bounds.contains(center)
}

/** State for individual dragging target. */
private class DragTargetStateImpl<T>(
  override val key: String,
  override val data: T,
  val draggableState: DraggableStateImpl<T>,
  override val dragComposable: @Composable () -> Unit,
) : DragTargetState<T> {
  override var isDragging by mutableStateOf(false)
  override var windowPosition = Offset.Zero
  override var dragPosition by mutableStateOf(Offset.Zero)
  override var size: IntSize by mutableStateOf(IntSize.Zero)

  override fun onDragStart() {
    // Another drag target is being used; ignore
    dragPosition = windowPosition
    if (key in draggableState.activeDragTargets) return
    draggableState.activeDragTargets[key] = this
    isDragging = true
  }

  override fun onDrag(offset: Offset) {
    if (isDragging) {
      dragPosition += offset
      draggableState.checkForDrag(key)
    }
  }

  override fun onDragEnd() {
    if (!isDragging) return
    draggableState.activeDragTargets.remove(key)
    draggableState.cleanUpDrag(this)
    isDragging = false
  }
}

private class DropTargetStateImpl<T>(
  private val key: String,
  private val draggableState: DraggableStateImpl<T>
) : DropTargetState<T> {
  var hoverKey: String? by mutableStateOf(null)
  override var hoverData: T? by mutableStateOf(null)
  override var bounds: Rect = Rect.Zero
    set(value) {
      field = value
      draggableState.checkForDrop(key)
    }

  override val isHovered
    get() = hoverKey != null
}
