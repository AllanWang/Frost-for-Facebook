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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.pitchedapps.frost.ext.thenIf
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
  val draggableState = rememberDraggableState()

  DragContainer(modifier = modifier, draggableState = draggableState) {
    Column(modifier = Modifier.statusBarsPadding()) {
      LazyVerticalGrid(
        modifier = Modifier.weight(1f),
        columns = GridCells.Fixed(4),
      ) {
        items(unselected, key = { it.key }) {
          DragTarget(key = it.key, draggableState = draggableState) { isDragging ->
            TabItem(
              modifier =
                Modifier.thenIf(!isDragging) {
                  val shakeState = rememberShakeState()
                  Modifier.animateItemPlacement().shake(shakeState).clickable {
                    shakeState.shake()
                    //            onSelect(listOf(it))
                  }
                },
              data = it,
            )
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
  draggableState: DraggableState,
  items: List<TabData>
) {
  NavigationBar(modifier = modifier) {
    items.forEach { item ->
      val key = item.key

      val hasHoverKey by derivedStateOf { draggableState.dropTarget(key)?.hoverKey != null }

      val alpha by
        animateFloatAsState(
          targetValue = if (!hasHoverKey) 1f else 0f,
          label = "Nav Item Alpha",
        )

      NavigationBarItem(
        modifier = Modifier.dropTarget(key, draggableState),
        icon = {
          //          println(dropTargetState.hoverKey)
          Icon(
            modifier = Modifier.size(24.dp).alpha(alpha),
            imageVector = item.icon,
            contentDescription = item.title,
          )
        },
        selected = false,
        onClick = {},
      )
    }
  }
}

@Composable
fun TabItem(
  data: TabData,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Icon(
      modifier = Modifier.padding(4.dp).size(24.dp),
      imageVector = data.icon,
      contentDescription = data.title,
    )
    Text(
      modifier = Modifier.padding(bottom = 4.dp),
      text = data.title,
      minLines = 2,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center,
    )
  }
}
