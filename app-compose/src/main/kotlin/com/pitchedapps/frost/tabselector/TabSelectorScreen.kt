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
package com.pitchedapps.frost.tabselector

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pitchedapps.frost.compose.draggable.DragContainer
import com.pitchedapps.frost.compose.draggable.DragTarget
import com.pitchedapps.frost.compose.draggable.DraggableState
import com.pitchedapps.frost.compose.draggable.dropTarget
import com.pitchedapps.frost.compose.draggable.rememberDraggableState
import com.pitchedapps.frost.compose.effects.rememberShakeState
import com.pitchedapps.frost.compose.effects.shake
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.tab

@Composable
fun TabSelectorScreen(modifier: Modifier = Modifier) {

  val context = LocalContext.current

  var selected: List<TabData> by remember {
    mutableStateOf(FbItem.defaults().map { it.tab(context) })
  }

  val options: Map<String, TabData> = remember {
    FbItem.values().associateBy({ it.key }, { it.tab(context) })
  }

  val unselected = remember(selected) { options.values - selected.toSet() }

  TabSelector(
    modifier = modifier,
    selected = selected,
    unselected = unselected,
    onSelect = { selected = it },
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabSelector(
  modifier: Modifier,
  selected: List<TabData>,
  unselected: List<TabData>,
  onSelect: (List<TabData>) -> Unit
) {
  val draggableState = rememberDraggableState<TabData>()

  DragContainer(modifier = modifier, draggableState = draggableState) {
    Column(modifier = Modifier.statusBarsPadding()) {
      LazyVerticalGrid(
        modifier = Modifier.weight(1f),
        columns = GridCells.Fixed(4),
      ) {
        items(unselected, key = { it.key }) { data ->
          DragTarget(key = data.key, data = data, draggableState = draggableState) { isDragging ->
            if (isDragging) {
              // In dragging box
              DraggingTabItem(data = data)
            } else {
              // In LazyVerticalGrid

              val shakeState = rememberShakeState()

              TabItem(
                modifier = Modifier.animateItemPlacement().shake(shakeState),
                data = data,
                onClick = { shakeState.shake() },
              )
            }
          }
        }
      }

      TabBottomBar(
        modifier = Modifier.navigationBarsPadding(),
        draggableState = draggableState,
        items = selected,
      )
    }
  }
}

@Composable
fun TabBottomBar(
  modifier: Modifier = Modifier,
  draggableState: DraggableState<TabData>,
  items: List<TabData>
) {
  NavigationBar(modifier = modifier) {
    items.forEach { item ->
      val dropTargetState = draggableState.rememberDropTarget(item.key)

      val alpha by
        animateFloatAsState(
          targetValue = if (!dropTargetState.isHovered) 1f else 0.3f,
          label = "Nav Item Alpha",
        )

      NavigationBarItem(
        modifier = Modifier.dropTarget(dropTargetState),
        icon = {
          val iconItem = dropTargetState.hoverData ?: item

          Icon(
            modifier = Modifier.size(24.dp).alpha(alpha),
            imageVector = iconItem.icon,
            contentDescription = iconItem.title,
          )
        },
        selected = false,
        onClick = {},
      )
    }
  }
}

/**
 * Our dragging tab item is fairly different from the original, so we'll just make a copy to add the
 * animations.
 *
 * Animations are done as one shot events from a default
 */
@Composable
fun DraggingTabItem(
  data: TabData,
  modifier: Modifier = Modifier,
) {

  var startState by remember { mutableStateOf(true) }

  LaunchedEffect(Unit) { startState = false }

  val transition = updateTransition(targetState = startState, label = "One Shot")

  val scale by transition.animateFloat(label = "Scale") { if (it) 1f else 1.3f }

  // Same color as ripple values
  val color by
    transition.animateColor(label = "Background") {
      if (it)
        MaterialTheme.colorScheme.surfaceVariant.copy(
          LocalRippleTheme.current.rippleAlpha().pressedAlpha
        )
      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
    }

  TabItem(
    modifier =
      modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
      },
    iconBackground = color,
    labelAlpha = 0f,
    data = data,
  )
}

@Composable
fun TabItem(
  data: TabData,
  modifier: Modifier = Modifier,
  iconBackground: Color = Color.Unspecified,
  labelAlpha: Float = 1f,
  onClick: () -> Unit = {}
) {

  val interactionSource = remember { MutableInteractionSource() }

  Column(
    modifier
      .clickable(onClick = onClick, interactionSource = interactionSource, indication = null)
      .fillMaxWidth()
      .padding(horizontal = 8.dp, vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Icon(
      modifier =
        Modifier.clip(CircleShape)
          .background(iconBackground)
          .indication(
            interactionSource,
            rememberRipple(color = MaterialTheme.colorScheme.surfaceVariant),
          )
          .padding(12.dp)
          .size(24.dp),
      imageVector = data.icon,
      contentDescription = data.title,
    )
    Text(
      // Weird offset is to accommodate the icon background, which is only used when the label is
      // hidden
      modifier = Modifier.offset(y = (-4).dp).alpha(labelAlpha),
      text = data.title,
      minLines = 2,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center,
    )
  }
}
